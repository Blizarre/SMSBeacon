package com.example.smsbeacon;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class SMSBeaconPreferencesFragment extends PreferenceFragment {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   addPreferencesFromResource(R.xml.prefs);/*TODO: utiliser PreferenceFragment 
	   											plutot que preference activity pour eviter 
	   											l'obsolescence*/
	}
}
