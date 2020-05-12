//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static String PREFERENCE_SUBJECT_ORDER = "pref_subject_order";
    public static String PREFERENCE_UNIT = "pref_unit";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Access the default shared prefs
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        setPrefSummaryTypeOrder(sharedPrefs);
        setPrefSummaryUnits(sharedPrefs);
    }

    // Set the summary to the currently selected subject order
    private void setPrefSummaryTypeOrder(SharedPreferences sharedPrefs) {
        String order = sharedPrefs.getString(PREFERENCE_SUBJECT_ORDER, "1");
        String[] typeOrders = getResources().getStringArray(R.array.pref_subject_order);
        Preference typeOrderPref = findPreference(PREFERENCE_SUBJECT_ORDER);
        typeOrderPref.setSummary(typeOrders[Integer.parseInt(order)]);
    }

    // Set the summary to the currently selected units
    private void setPrefSummaryUnits(SharedPreferences sharedPrefs) {
        String order = sharedPrefs.getString(PREFERENCE_UNIT, "0");
        String[] units = getResources().getStringArray(R.array.pref_unit);
        Preference unitPref = findPreference(PREFERENCE_UNIT);
        unitPref.setSummary(units[Integer.parseInt(order)]);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREFERENCE_SUBJECT_ORDER)) {
            setPrefSummaryTypeOrder(sharedPreferences);
        }
        else if (key.equals(PREFERENCE_UNIT)) {
            setPrefSummaryUnits(sharedPreferences);
        }
    }

}