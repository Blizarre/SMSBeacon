package com.example.smsbeacon;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SMSBeaconPreferencesFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   addPreferencesFromResource(R.layout.prefs);
	}
}
