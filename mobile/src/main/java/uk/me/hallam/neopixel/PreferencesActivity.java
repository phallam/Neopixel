package uk.me.hallam.neopixel;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by phallam on 01/11/16.
 */

public class PreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
    }
}