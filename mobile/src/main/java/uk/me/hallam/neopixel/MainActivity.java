package uk.me.hallam.neopixel;

import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default values
        lightSequence = getResources().getString(R.string.getparms);
        lightColour = getResources().getString(R.string.defColour);
        lightWait = 25;
        lightIterations = 0;
        lightClear = getResources().getString(R.string.defClear);
        // Call getparms to obtain current LED status
        setSequence(getResources().getString(R.string.getparms));

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
        setSequence(lightSequence);
    }

    public String getSequence() {
        return lightSequence;
    }

    public void setSequence(String sequence) {
        // Reset all the query parm requirements
        needColour = false;
        needWait = false;
        needIterations = false;
        needClear = false;

        if (sequence.equals(getResources().getString(R.string.getparms)))
            lightSequence = sequence;
        else if (sequence.equals(getResources().getString(R.string.reset)))
            lightSequence = sequence;
        else if (sequence.equals(getResources().getString(R.string.setcolour))) {
            popupMessage(getString(R.string.select_colour));
            lightSequence = sequence;
            needColour = true;
        } else if (sequence.equals(getResources().getString(R.string.colourwipe))) {
            popupMessage(getString(R.string.select_colour));
            lightSequence = sequence;
            needColour = true;
            needWait = true;
            needIterations = true;
        } else if (sequence.equals(getResources().getString(R.string.colourwipeback))) {
            popupMessage(getString(R.string.select_colour));
            lightSequence = sequence;
            needColour = true;
            needWait = true;
            needIterations = true;
        } else if (sequence.equals(getResources().getString(R.string.rainbow))) {
            lightSequence = sequence;
            needWait = true;
            needIterations = true;
        } else if (sequence.equals(getResources().getString(R.string.theatrechase))) {
            popupMessage(getString(R.string.select_colour));
            lightSequence = sequence;
            needColour = true;
            needWait = true;
            needIterations = true;
        }

        if (lightIterations > 0 && needIterations)
            needClear = true;
        doHttpGet();
    }

    private void popupMessage(String msg) {
        Toast.makeText(this,
                Html.fromHtml(msg),
                Toast.LENGTH_SHORT).show();
    }

    public int getSpeed() {
        if (isbetween(lightWait, 1, 5))
            return 9;
        else if (isbetween(lightWait, 6, 10))
            return 8;
        else if (isbetween(lightWait, 11, 25))
            return 7;
        else if (isbetween(lightWait, 26, 50))
            return 6;
        else if (isbetween(lightWait, 51, 100))
            return 5;
        else if (isbetween(lightWait, 101, 200))
            return 4;
        else if (isbetween(lightWait, 201, 500))
            return 3;
        else if (isbetween(lightWait, 501, 1000))
            return 2;
        else if (isbetween(lightWait, 1001, 2500))
            return 1;
        else return 0;
    }

    public void setSpeed(int speed) {
        switch (speed) {
            case 9:
                lightWait = 5;
                break;
            case 8:
                lightWait = 10;
                break;
            case 7:
                lightWait = 25;
                break;
            case 6:
                lightWait = 50;
                break;
            case 5:
                lightWait = 100;
                break;
            case 4:
                lightWait = 200;
                break;
            case 3:
                lightWait = 500;
                break;
            case 2:
                lightWait = 1000;
                break;
            case 1:
                lightWait = 2500;
                break;
            case 0:
                lightWait = 5000;
                break;
        }
        setSequence(lightSequence);
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
}
