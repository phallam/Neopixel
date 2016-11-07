package uk.me.hallam.neopixel;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements AsyncResponse, SequenceSetter, ColourSetter {

    // Values used to build URL for HTTP GET to set LED sequences
    private String lightSequence;
    private String lightColour;
    private int lightWait;
    private int lightIterations;
    private String lightClear;
    // Flags to indicate which query parms are required in URL
    private boolean needColour = false;
    private boolean needWait = false;
    private boolean needIterations = false;
    private boolean needClear = false;
    private boolean httpResponseReceived = false;
    ArrayList<LEDSequence> sequences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sequences = buildSequences();
        // Set default values
        lightSequence = getResources().getString(R.string.getparms);
        lightColour = getResources().getString(R.string.defColour);
        lightWait = 25;
        lightIterations = 0;
        lightClear = getResources().getString(R.string.defClear);
        // Call getparms to obtain current LED status
        doSetSequence(getResources().getString(R.string.getparms), false);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(0);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SequenceFragment(), getResources().getString(R.string.sequence_title));
        adapter.addFragment(new ColourFragment(), getResources().getString(R.string.colour_title));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    public String getColour() {
        return lightColour;
    }

    public void setColour(String colour) {
        lightColour = colour;
        if (needColour)
            doSetSequence(lightSequence, false);
    }

    public String getSequence() {
        return lightSequence;
    }

    public void setSequence(String sequence) {
        doSetSequence(sequence, true);
    }

    // Set the LED sequence with/without a toast message to choose colot
    public void doSetSequence(String sequence, boolean popup) {

        boolean found = findSequence(sequence);

        if (found) {
            if (lightIterations > 0 && needIterations)
                needClear = true;
            else
                needClear = false;

            if (needColour && popup)
                popupMessage(getString(R.string.select_colour));

            doHttpGet();
        }
    }

    public boolean findSequence(String sequence) {
        Iterator<LEDSequence> list = sequences.iterator();
        boolean found = false;
        while (list.hasNext() && !found) {
            LEDSequence seq = list.next();
            if (seq.getSequence().equals(sequence)) {
                lightSequence = sequence;
                needColour = seq.isNeedColour();
                needWait = seq.isNeedWait();
                needIterations = seq.isNeedIterations();
                found = true;
            }
        }
        return found;
    }
    private void popupMessage(String msg) {
        Toast.makeText(this,
                Html.fromHtml(msg),
                Toast.LENGTH_SHORT).show();
    }

    public int getSpeed() {
        for(int i=9; i>0; i--){
            if (lightWait <= waitTimes[i])
                return i;
        }
        return 0;
    }

    // Wait times that correspond to speed bar values
    final int[] waitTimes = new int[]{5000, 2500, 1000, 500, 200, 100, 50, 25, 10, 5};

    public void setSpeed(int speed) {
        if (speed >= 0 && speed <= 9) {
            lightWait = waitTimes[speed];

            if (needWait)
                doSetSequence(lightSequence, false);
        }
    }

    public static boolean isbetween(int i, int minValueInclusive, int maxValueInclusive) {
        return (i >= minValueInclusive && i <= maxValueInclusive);
    }

    void doHttpGet() {
        URL url = null;
        String path = "http://192.168.1.246:8080/" + lightSequence;
        char delim = '?';
        if (needColour) {
            path += delim + getResources().getString(R.string.queryColour) + '=' + lightColour;
            delim = '&';
        }
        if (needWait) {
            path += delim + getResources().getString(R.string.queryWait) + '=' + lightWait;
            delim = '&';
        }
        if (needIterations) {
            path += delim + getResources().getString(R.string.queryIterations) + '=' + lightIterations;
            delim = '&';
        }
        if (needClear) {
            path += delim + getResources().getString(R.string.queryClear) + '=' + lightClear;
            delim = '&';
        }
        try {
            url = new URL(path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        // Create a new async task for HTTP request
        SendCommandTask asyncTask = new SendCommandTask();
        // Set delegate/listener back to this class
        asyncTask.delegate = this;
        // Execute the request
        httpResponseReceived = false;
        asyncTask.execute(url);
    }

    // Override the implemented method from asyncTask
    @Override
    public void processHttpResponse(String output) {
        // Receive the result from onPostExecute(result) method in async class here
        if (output != null) {
            Pattern pattern = Pattern.compile("SERVER VALUES\\[(.+?)\\]");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                String parms = matcher.group(1);
                pattern = Pattern.compile("(.+?)=(.+?);");
                matcher = pattern.matcher(parms);
                while (matcher.find())
                    if (matcher.groupCount() == 2) {
                        String keyword = matcher.group(1);
                        if (keyword.equals(getResources().getString(R.string.querysequence)))
                            lightSequence = matcher.group(2);
                        else if (keyword.equals(getResources().getString(R.string.queryColour)))
                            lightColour = matcher.group(2);
                        else if (keyword.equals(getResources().getString(R.string.queryWait)))
                            try {
                                lightWait = Integer.parseInt(matcher.group(2));
                            } catch (NumberFormatException nfe) {
                            }
                        else if (keyword.equals(getResources().getString(R.string.queryIterations)))
                            try {
                                lightIterations = Integer.parseInt(matcher.group(2));
                            } catch (NumberFormatException nfe) {
                            }
                        else if (keyword.equals(getResources().getString(R.string.queryClear)))
                            lightClear = matcher.group(2);
                    }
                httpResponseReceived = true;
            }
        }
    }

    // Set up an ArrayList to hold the LED sequences that we support
    // Values should really be in an external file, but...
    ArrayList<LEDSequence> buildSequences() {
        ArrayList<LEDSequence> list = new ArrayList<LEDSequence>();
        // Add sequences - path, needsColour, needsWait, needsIterations
        list.add(new LEDSequence(getResources().getString(R.string.getparms), false, false, false));
        list.add(new LEDSequence(getResources().getString(R.string.reset), false, false, false));
        list.add(new LEDSequence(getResources().getString(R.string.setcolour), true, false, false));
        list.add(new LEDSequence(getResources().getString(R.string.colourwipe), true, true, true));
        list.add(new LEDSequence(getResources().getString(R.string.colourwipeback), true, true, true));
        list.add(new LEDSequence(getResources().getString(R.string.rainbow), false, true, true));
        list.add(new LEDSequence(getResources().getString(R.string.theatrechase), true, true, true));
        return list;
    }
}
