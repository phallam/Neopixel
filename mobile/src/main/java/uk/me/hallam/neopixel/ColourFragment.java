package uk.me.hallam.neopixel;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.slider.LightnessSlider;
import com.flask.colorpicker.slider.OnValueChangedListener;

/**
 * Created by phallam on 01/11/16.
 */

interface ColourSetter {
    void setColour(String colour);
    String getColour();

}

public class ColourFragment extends Fragment {

    private ColourSetter colourSetter;
    ColorPickerView colorPickerView;
    LightnessSlider lightnessSlider;

    public ColourFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            colourSetter = (ColourSetter) context;
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
        // Inflate the layout for this fragment
        View colour = inflater.inflate(R.layout.fragment_colour, container, false);

        // Find the colour picker components
        colorPickerView = (ColorPickerView) colour.findViewById(R.id.color_picker_view);
        lightnessSlider = (LightnessSlider) colour.findViewById(R.id.v_lightness_slider);

        colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                // Convert colour to string
                String colour = Integer.toHexString(selectedColor);
                // Pad with 0s or remove leading ff as appropriate
                colour = ("ff000000" + colour).substring(colour.length() + 2);
                colourSetter.setColour(colour);
            }
        });

        lightnessSlider.setOnValueChangedListener(new OnValueChangedListener() {
            @Override
            public void onValueChanged(float value) {
                // Get selected colour as a string
                String selectedColor = Integer.toHexString(colorPickerView.getSelectedColor());
                // Pad with 0s or remove leading ff as appropriate
                selectedColor = ("ff000000" + selectedColor).substring(selectedColor.length() + 2);
                colourSetter.setColour(selectedColor);
            }
        });

        return colour;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Done in a runnable after 0.5 second delay to allow getparms GET to return current values on server (
        // Uses defaults if no (or slow) response
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                int c = (int) Long.parseLong("ff" + colourSetter.getColour(), 16);
                colorPickerView.setInitialColor(c, false);
                colorPickerView.invalidate();
            }
        }, 500);
    }
}
