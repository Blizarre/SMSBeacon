package com.example.smsbeacon;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class RingActivity extends Activity implements OnSeekBarChangeListener, LocationListener {
	protected FlashLightHandler mFlash;
	protected RingToneHandler mRingTone;

	protected class GPSInformations {
		public Location mBestLocation;
		public boolean mIsLocationSMSSent;
		private int mNumRemainingProviders;
		private boolean mIsLocAllowed;
		private Timer mTimer;
	}
	protected GPSInformations mGPSInfo = new GPSInformations();

	private String mCaller;
	AudioManager m_audioManager;
	private final String TAG = getClass().getName();
	

	private LocationManager mLocationManager;
	
	private boolean mIsRingToneAction;
	private boolean mIsGPSAction;

	public enum Action { RINGTONE, SMS_LOCATION };
	public static final String DATA_EXTRA_CALLER = "param_intent_caller";
	public static final String DATA_EXTRA_ACTION = "param_intent_action";

	protected void stopRingToneActions() {
		mFlash.stopFlicker();
		mRingTone.stopRingTone();
	}

	private void stopGPSAction() {
		sendLocationSMS(mGPSInfo.mBestLocation,  mCaller, false);
		mGPSInfo.mIsLocationSMSSent = true;
		mGPSInfo.mTimer.cancel();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);

		Intent origin = getIntent();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		mCaller = origin.getStringExtra(DATA_EXTRA_CALLER);
		Action action = (Action)origin.getSerializableExtra(DATA_EXTRA_ACTION);

		prepareInterfaceElements(action, mCaller);
		
		if(action == Action.RINGTONE) {
			mFlash = new FlashLightHandler(this);
			mRingTone = new RingToneHandler(this);

			if (shallUseFlash()) mFlash.startFlicker();

			int ringTime = Integer.parseInt(prefs.getString(getString(R.string.pref_home_key_time), "default choice"));
			mRingTone.startRingTone(ringTime * 1000);
			
		} else if(action == Action.SMS_LOCATION && mGPSInfo.mIsLocAllowed) {
			sendLocationSMS(getLastLocation(), mCaller, true);
			prepareLocationThread();
		}

	}


	protected void prepareInterfaceElements(Action act, String callerNumber) {
		SeekBar m_Bar;
		String message = "";
		
		m_Bar = (SeekBar)findViewById(R.id.seekbar);
		m_Bar.setOnSeekBarChangeListener(this);
		TextView txtPhoneNumber = (TextView)findViewById(R.id.phone_number);
		
		if(act == Action.RINGTONE)
			message = getString(R.string.msg_ringtone_num_fmt, callerNumber);
		else if(act == Action.SMS_LOCATION)
			message = getString(R.string.msg_location_sms_num_fmt, callerNumber);
		
		txtPhoneNumber.setText(message);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mGPSInfo.mIsLocAllowed = prefs.getBoolean(getString(R.string.pref_gal_act_key), false);
	}


	@Override
    public void onProgressChanged(SeekBar seekBar, int progress,
    		boolean fromUser) {
		return;
    }
	
	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    	return;
    }
	
	 @Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (90 < seekBar.getProgress())
		{
			if(mIsRingToneAction)
				stopRingToneActions();
			if(mIsGPSAction)
				stopGPSAction();
			this.finish();
		}
		else
		{
			seekBar.setProgress(0);
		}
	}
	 


	protected boolean shallUseFlash()
	{
		SharedPreferences preferences = PreferenceManager.
									getDefaultSharedPreferences(this);
		
		boolean flashAvailable = mFlash.hasFlashLight();
		
		boolean flashEnabled = preferences.getBoolean(
				getString(R.string.pref_home_key_flash_light_chk_box), 
				false); 
		
		return flashEnabled & flashAvailable; 
	}

	private void prepareLocationThread() {
		
		mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		List<String> lProviders = mLocationManager.getProviders(true);

		for(String provider:lProviders) {
			Log.i(TAG, "Available Provider : " + provider);
			mLocationManager.requestSingleUpdate(provider, this, Looper.getMainLooper());
		}
		
		mGPSInfo.mTimer = new Timer();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 3);
		
		mGPSInfo.mTimer.schedule( new TimerTask() {

			@Override
			public void run() {
				Log.i(TAG, "Timeout occured when waiting for the provider location data");
				mLocationManager.removeUpdates(RingActivity.this);
				if(!mGPSInfo.mIsLocationSMSSent)
					stopGPSAction();
			}
		}, c.getTime());
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(TAG, provider + " status changed : " + status);
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Log.i(TAG, provider  + " is enabled");
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, provider  + " is disabled");
	}
	
	@Override
	public void onLocationChanged(Location location) {
		mGPSInfo.mBestLocation = bestBetween(mGPSInfo.mBestLocation,  location);
		mGPSInfo.mNumRemainingProviders -= 1;
		
		Log.i(TAG, "New location received, remains " + mGPSInfo.mNumRemainingProviders);
		
		if(mGPSInfo.mNumRemainingProviders == 0) {
			Log.i(TAG, "Last location received");
			stopGPSAction();
		}
	}

	/**
	 * Get Last known location from the GPS or using the cell tower/wifi. 
	 * @return A Location object representing the last known location of the device
	 */
	private Location getLastLocation() {
		Location loc = null, newLoc = null;
		List<String> lProviders;
		
		if(mGPSInfo.mIsLocAllowed) {
			lProviders = mLocationManager.getProviders(true);
			
			for(String provider:lProviders) {
				Log.i(TAG, "Available Provider : " + provider);
				newLoc = mLocationManager.getLastKnownLocation(provider);
				loc = bestBetween(loc, newLoc);
			}
		}
		
		return loc;
	}
	
	private void sendLocationSMS(Location loc, String receiver, Boolean isLastLocation) {
		double lon, lat, alt, prec;
		long temp;
		String lastFix;
		StringBuilder positionStr = new StringBuilder();
		
		if(isLastLocation)
			positionStr.append(getString(R.string.sms_lastloc_header));
		else 
			positionStr.append(getString(R.string.sms_newloc_header));
		
		if(loc == null) {
			positionStr.append(getString(R.string.sms_noloc));
		} else {
			lat = loc.getLatitude();
			lon = loc.getLongitude();
			alt = loc.getAltitude();
			prec = loc.getAccuracy();
			temp = loc.getTime();

			lastFix = DateFormat.getDateTimeInstance().format(new Date(temp));
			
			positionStr.append(String.format(getString(R.string.sms_loc_fmt), lon, lat, alt, prec, lastFix));
		}
		Log.i(TAG, "send sms to " + receiver + " with content '" + positionStr + "'");
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(receiver, null, positionStr.toString(), null, null);		
	}
	
	public Location bestBetween(Location a, Location b) {
		// If one of the location is at least 60 minute older than the other
		if(Math.abs(a.getTime() - b.getTime()) > 60 * 1000 * 60) {
			if(a.getTime() > b.getTime())
				return a;
			else
				return b;
		// Else return the one with the better accuracy
		} else if(a.getAccuracy() < b.getAccuracy()) 
			return a;
		else 
			return b;
	}

}