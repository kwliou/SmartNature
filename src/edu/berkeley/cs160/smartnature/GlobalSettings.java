package edu.berkeley.cs160.smartnature;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GlobalSettings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("global");
        addPreferencesFromResource(R.xml.global_preferences);
	}
}