package com.example.smsbeacon;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SMSBeaconPreferenceActivity extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// instanciate the preference fragment and add it as the main content
		if (savedInstanceState == null) {
			Fragment newFragment = new SMSBeaconPreferencesFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(android.R.id.content, newFragment).commit();
		}
	}
}
