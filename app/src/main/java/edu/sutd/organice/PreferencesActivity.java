package edu.sutd.organice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

/**
 * An {@link android.app.Activity Activity} in which users can configure the app according to their
 * own preferences. The {@link android.app.Activity Activity}'s layout is automatically generated
 * from an XML file with the help of the {@link androidx.preference} package, rather than explicitly
 * declared as a usual UI layout.
 */
public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // all boilerplate code here
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_container, new MainPreferencesFragment())
                .commit();
        setContentView(R.layout.activity_preferences);
    }

    /**
     * This is a boilerplate inner class created to make use of the {@link androidx.preference}
     * package for generating the preference UI layout.
     */
    public static class MainPreferencesFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_main, rootKey);
        }
    }
}
