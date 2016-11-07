package uk.me.hallam.neopixel;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        colorPickerView = (ColorPickerView) colour.findViewById(R.id.color_picker_view);
        lightnessSlider = (LightnessSlider) colour.findViewById(R.id.v_lightness_slider);

        colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                //MainActivity ourActivity = ((MainActivity) getActivity());
                //ourActivity.setColour(Integer.toHexString(selectedColor).substring(2));
                String colour = Integer.toHexString(selectedColor);
                while (colour.length() < 8)
                    colour = '0'+colour;
                colour = colour.substring(2);
                colourSetter.setColour(colour);
                Toast.makeText(
                        getActivity(),
                        //"selectedColor: " + Integer.toHexString(selectedColor).toUpperCase(),
                        "selectedColor: " + colour.toUpperCase(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        lightnessSlider.setOnValueChangedListener(new OnValueChangedListener() {
            @Override
            public void onValueChanged(float value) {
                //MainActivity ourActivity = ((MainActivity) getActivity());
                //ourActivity.setColour(Integer.toHexString(selectedColor).substring(2));
                String selectedColor = Integer.toHexString(colorPickerView.getSelectedColor());
                while (selectedColor.length() < 8)
                    selectedColor = '0'+selectedColor;
                selectedColor = selectedColor.substring(2);
                colourSetter.setColour(selectedColor);
                Toast.makeText(
                        getActivity(),
                        "selectedColor: " + selectedColor.toUpperCase(),
                        Toast.LENGTH_SHORT).show();
            }
        });


        return colour;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int c = (int) Long.parseLong("ff"+colourSetter.getColour(),16);
        //colorPickerView.setInitialColor(c, false);

        // Done in a runnable after 0.5 second delay to allow getparms GET to return current values on server (uses defaults if no (or slow) response
        new Handler().postDelayed(new Runnable() {
            public void run() {
                int c = (int) Long.parseLong("ff"+colourSetter.getColour(),16);
                ColourFragment.this.colorPickerView.setInitialColor(c, false);
                //colorPickerView.setInitialColors(new Integer[]{0x00d7d7}, 0);
                //lightnessSlider.setColor(255);
            }
        }, 500);
    }
}
