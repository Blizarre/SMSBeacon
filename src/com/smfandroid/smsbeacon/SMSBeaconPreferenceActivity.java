package com.smfandroid.smsbeacon;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
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
			
		    if( !isLocationActivated() ) {
		        redirectToLocationSettings();
		    }
    
		}
	}
	
	public boolean isLocationActivated() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	public void redirectToLocationSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.loc_not_found_title);  // GPS not found
        builder.setMessage(R.string.loc_not_found_message); // Want to enable?
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                SMSBeaconPreferenceActivity.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
        return;		
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
