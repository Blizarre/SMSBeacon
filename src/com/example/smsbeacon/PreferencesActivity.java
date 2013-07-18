package com.example.smsbeacon;

import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.media.RingtoneManager;
import android.os.Bundle;

public class PreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   addPreferencesFromResource(R.xml.prefs); /*TODO: utiliser PreferenceFragment 
	   											plutot que preference activity pour eviter 
	   											l'obsolescence*/
	}
}


