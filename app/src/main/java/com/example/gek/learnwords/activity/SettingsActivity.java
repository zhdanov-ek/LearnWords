package com.example.gek.learnwords.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.gek.learnwords.R;



//todo нормально разобраться с настройками

public class SettingsActivity extends AppCompatPreferenceActivity
    implements Preference.OnPreferenceChangeListener{

    private final static String TAG = "Preferences: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Подгружаем описанные в ХМЛ опции
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_theme_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_delay_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_direction_key)));


        // Получаем вибратор и если его нет то блокируем опцию в меню
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!v.hasVibrator()) {
            getPreferenceScreen().findPreference(getResources().getString(R.string.pref_vibration_key)).setEnabled(false);
            getPreferenceScreen().findPreference(getResources().getString(R.string.pref_vibration_key)).setDefaultValue(false);
            Log.i(TAG, "No vibrator on device ");
        } else {
            getPreferenceScreen().findPreference(getResources().getString(R.string.pref_vibration_key)).setEnabled(true);
        }

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }



    // Реализуем интерфейс Preference.OnPreferenceChangeListener
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }
}