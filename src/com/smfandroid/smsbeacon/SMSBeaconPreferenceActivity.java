package com.smfandroid.smsbeacon;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SMSBeaconPreferenceActivity extends PreferenceActivity {
	RingtoneManager mRingtoneManager;
	Cursor mcursor;
	String title;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// instanciate the preference fragment and add it as the main content
		if (savedInstanceState == null) {
			Fragment newFragment = new SMSBeaconPreferencesFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(android.R.id.content, newFragment).commit();
			mRingtoneManager = new RingtoneManager(this);
			mcursor = mRingtoneManager.getCursor();
			title = RingtoneManager.EXTRA_RINGTONE_TITLE;
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config, menu);
       	return true;
    }

	/**
	 *  Android 4.0.3 doesn't like AT ALL custom themes and onClick.
	 *  That's why I have to use onOptionsItemSelected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		switch (mi.getItemId()) {
			case R.id.action_about:
				AboutDialog hd = new AboutDialog();
				hd.show(getFragmentManager(), "NoticeDialogFragment");

			default:
				return false;
		}
		
	}
}
