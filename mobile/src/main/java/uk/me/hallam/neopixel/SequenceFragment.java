package uk.me.hallam.neopixel;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;

import pl.droidsonroids.gif.GifImageButton;

//import com.bumptech.glide.Glide;
//import com.koushikdutta.ion.Ion;

/**
 * Created by phallam on 01/11/16.
 */

interface AsyncResponse {
    void processHttpResponse(String output);
}

interface SequenceSetter {
    void setSequence(String sequence);
    String getSequence();

    void setSpeed(int speed);
    int getSpeed();

    boolean findSequence(String sequence);
}

public class SequenceFragment extends Fragment implements OnClickListener, SeekBar.OnSeekBarChangeListener {

    private SequenceSetter sequenceSetter;

    private RadioButton offButton = null;
    ImageButton offImage = null;
    RadioButton setButton = null;
    ImageButton setImage = null;
    RadioButton wipeButton = null;
    GifImageButton wipeImage = null;
    RadioButton wipebackButton = null;
    GifImageButton wipebackImage = null;
    RadioButton rainbowButton = null;
    GifImageButton rainbowImage = null;
    RadioButton theatrechaseButton = null;
    GifImageButton theatrechaseImage = null;
    SeekBar speed = null;

    public SequenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            sequenceSetter = (SequenceSetter) context;
        } catch (ClassCastException e) {
            /** The activity does not implement the listener */
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and find buttons
        View sequence = inflater.inflate(R.layout.fragment_sequence, container, false);
        offButton = (RadioButton) sequence.findViewById(R.id.offButton);
        offImage = (ImageButton) sequence.findViewById(R.id.offImage);
        setButton = (RadioButton) sequence.findViewById(R.id.setButton);
        setImage = (ImageButton) sequence.findViewById(R.id.setImage);
        wipeButton = (RadioButton) sequence.findViewById(R.id.colourwipeButton);
        wipeImage = (GifImageButton) sequence.findViewById(R.id.colourwipeImage);
        wipebackButton = (RadioButton) sequence.findViewById(R.id.cwBackButton);
        wipebackImage = (GifImageButton) sequence.findViewById(R.id.cwBackImage);
        rainbowButton = (RadioButton) sequence.findViewById(R.id.rainbowButton);
        rainbowImage = (GifImageButton) sequence.findViewById(R.id.rainbowImage);
        theatrechaseButton = (RadioButton) sequence.findViewById(R.id.theatrechaseButton);
        theatrechaseImage = (GifImageButton) sequence.findViewById(R.id.theatrechaseImage);
        speed = (SeekBar) sequence.findViewById(R.id.speedBar);

        offButton.setOnClickListener(this);
        offImage.setOnClickListener(this);
        setButton.setOnClickListener(this);
        setImage.setOnClickListener(this);
        wipeButton.setOnClickListener(this);
        wipeImage.setOnClickListener(this);
        wipebackButton.setOnClickListener(this);
        wipebackImage.setOnClickListener(this);
        rainbowButton.setOnClickListener(this);
        rainbowImage.setOnClickListener(this);
        theatrechaseButton.setOnClickListener(this);
        theatrechaseImage.setOnClickListener(this);
        speed.setOnSeekBarChangeListener(this);

        //Ion.with(v)
        //    .load(Uri.parse("android.resource://uk.me.hallam.neopixel/drawable/theatrechase").toString());
        //Glide
        //        .with(this)
        //        .load(R.drawable.theatrechase)
        //        //.load(Uri.parse("android.resource://uk.me.hallam.neopixel/drawable/theatrechase").toString())
        //        .asGif()
        //        .fitCenter()
        //        .into(v);

        return sequence;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Done in a runnable after 0.5 second delay to allow getparms GET to return current values on server (
        // Uses defaults if no (or slow) response
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                String seq = sequenceSetter.getSequence();
                setRadioButtons(getButtonFromSequence(seq));
                speed.setProgress(sequenceSetter.getSpeed());
                speed.invalidate();
                sequenceSetter.findSequence(seq);
            }
        }, 500);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        sequenceSetter.setSpeed(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Set the shade of the previous value.
        seekBar.setSecondaryProgress(seekBar.getProgress());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.offButton:
            case R.id.offImage:
                setRadioButtons(offButton);
                sequenceSetter.setSequence(getResources().getString(R.string.reset));
                break;
            case R.id.setButton:
            case R.id.setImage:
                setRadioButtons(setButton);
                sequenceSetter.setSequence(getResources().getString(R.string.setcolour));
                break;
            case R.id.colourwipeButton:
            case R.id.colourwipeImage:
                setRadioButtons(wipeButton);
                sequenceSetter.setSequence(getResources().getString(R.string.colourwipe));
                break;
            case R.id.cwBackButton:
            case R.id.cwBackImage:
                setRadioButtons(wipebackButton);
                sequenceSetter.setSequence(getResources().getString(R.string.colourwipeback));
                break;
            case R.id.rainbowButton:
            case R.id.rainbowImage:
                setRadioButtons(rainbowButton);
                sequenceSetter.setSequence(getResources().getString(R.string.rainbow));
                break;
            case R.id.theatrechaseButton:
            case R.id.theatrechaseImage:
                setRadioButtons(theatrechaseButton);
                sequenceSetter.setSequence(getResources().getString(R.string.theatrechase));
                break;
        }
    }

    // Set the selected radio button and unset the rest
    void setRadioButtons(RadioButton selected) {
        // First reset them all
        offButton.setChecked(false);
        setButton.setChecked(false);
        wipeButton.setChecked(false);
        wipebackButton.setChecked(false);
        rainbowButton.setChecked(false);
        theatrechaseButton.setChecked(false);
        // Then set the selected one
        selected.setChecked(true);
    }

    // Get the button resource ID for the sequence string passed
    RadioButton getButtonFromSequence(String sequence) {
        if (sequence.equals(getResources().getString(R.string.reset)))
            return offButton;
        else if (sequence.equals(getResources().getString(R.string.setcolour)))
            return setButton;
        else if (sequence.equals(getResources().getString(R.string.colourwipe)))
            return wipeButton;
        else if (sequence.equals(getResources().getString(R.string.colourwipeback)))
            return wipebackButton;
        else if (sequence.equals(getResources().getString(R.string.rainbow)))
            return rainbowButton;
        else if (sequence.equals(getResources().getString(R.string.theatrechase)))
            return theatrechaseButton;
        else return offButton;
    }
}
