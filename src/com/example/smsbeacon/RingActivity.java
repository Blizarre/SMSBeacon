package com.example.smsbeacon;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class RingActivity extends Activity implements OnSeekBarChangeListener, LocationListener {
	private Camera m_cam = null;
	private boolean m_hasFlash = false;
	private Parameters m_param = null;
	private Ringtone m_rgtone = null;
	private int m_initVolume = 0;
	private String m_ringTime = null;
	private String mCaller;
	private Boolean m_isLocAllowed;
	AudioManager m_audioManager;
	private boolean m_locationSMSSend;;
	private final String TAG = getClass().getName();
	private boolean m_GPSActivated;
	private Location lastLocation;
	
	public static final String DATA_EXTRA_CALLER = "param_intent_caller";
	public static final String DATA_EXTRA_ACTION = "param_intent_action";
	private LocationManager m_LocationManager;
	private Action m_Action;
	public enum Action { RINGTONE, SMS_LOCATION };
	
	protected void prepareRingTone() {
		m_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		m_initVolume = m_audioManager.getStreamVolume(AudioManager.STREAM_RING);
		m_audioManager.setStreamVolume(
				   AudioManager.STREAM_RING,
				   m_audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
				   AudioManager.FLAG_PLAY_SOUND
				);
	}
	
	
	protected void prepareRingToneStopThread() {
		Timer t = new Timer();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, Integer.parseInt(m_ringTime));
		
		t.schedule( new TimerTask() {
			
			@Override
			public void run() {
				RingActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						stopRingTone();
					}
				});
			}
		}, c.getTime());			
	}
	
	protected void stopRingTone() {
		turnOffFlash();
		turnOffRingTone();
		m_audioManager.setStreamVolume(
				   AudioManager.STREAM_RING,
				   m_initVolume,
				   AudioManager.FLAG_PLAY_SOUND
				);		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent origin = getIntent();
		mCaller = origin.getStringExtra(DATA_EXTRA_CALLER);
		m_Action = (Action)origin.getSerializableExtra(DATA_EXTRA_ACTION);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);
		
		prepareInterfaceElements(m_Action, mCaller);
		
		if(m_Action == Action.RINGTONE) {
			if (shallUseFlash()) turnOnFlash();
			prepareRingTone();
			turnOnRingTone();
			prepareRingToneStopThread();
		} else if(m_Action == Action.SMS_LOCATION && m_isLocAllowed) {
			sendLocationSMS(getLastLocation(), mCaller, true);
			prepareLocationThread();
		}

	}


	private void prepareLocationThread() {
		
		m_LocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		
		if(m_LocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.i(TAG, "GPS provider enabled, requesting update");
			m_LocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, Looper.getMainLooper());
			m_GPSActivated = true;
		} else {
			Log.i(TAG, "GPS provider disabled");
		}
		m_LocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper());
		
		Timer t = new Timer();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 3);
		
		t.schedule( new TimerTask() {

			@Override
			public void run() {
				Log.i(TAG, "Timeout occured when waiting for the GPS data");
				if(!m_locationSMSSend)
					sendLocationSMS(lastLocation, mCaller, false);
				m_LocationManager.removeUpdates(RingActivity.this);
			}
		}, c.getTime());			
	}
		


	protected void prepareInterfaceElements(Action act, String callerNumber) {
		SeekBar m_Bar;
		String message = "";
		
		m_Bar = (SeekBar)findViewById(R.id.seekbar);
		m_Bar.setOnSeekBarChangeListener(this);
		TextView m_phoneNumber = (TextView)findViewById(R.id.phone_number);
		if(act == Action.RINGTONE)
			message = getString(R.string.msg_ringtone_num_fmt, callerNumber);
		else if(act == Action.SMS_LOCATION)
			message = getString(R.string.msg_location_sms_num_fmt, callerNumber);
		
		m_phoneNumber.setText(message);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		m_ringTime = prefs.getString(getString(R.string.pref_home_key_time), "default choice");
		m_hasFlash = getApplicationContext().getPackageManager()
		        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		m_isLocAllowed = prefs.getBoolean(getString(R.string.pref_gal_act_key), false);
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
			RingActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(m_Action == Action.RINGTONE) {
						stopRingTone();
					}
					RingActivity.this.finish();
				}
			});
			
		}
		else
		{
			seekBar.setProgress(0);
		}
	}
	 
	protected void turnOnRingTone()
	{
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		m_rgtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
		m_rgtone.play();
	}
	
	protected void turnOffRingTone()
	{
		
		m_rgtone.stop();
	}
	
	protected void turnOnFlash()
	{
		if (m_hasFlash)
		{
			try {
					m_cam = Camera.open();     
					m_param = m_cam.getParameters();
					m_param.setFlashMode(Parameters.FLASH_MODE_TORCH);
					m_cam.setParameters(m_param);
					m_cam.startPreview();
				} catch (RuntimeException e) {
	            Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
	        }
		}
	}
	
	protected void turnOffFlash()
	{
		if (m_cam == null || m_param == null) {
            return;
        }
        m_param.setFlashMode(Parameters.FLASH_MODE_OFF);
        m_cam.setParameters(m_param);
        m_cam.stopPreview();
        m_cam.release();
	}
	
	protected boolean shallUseFlash()
	{
		SharedPreferences preferences = PreferenceManager.
									getDefaultSharedPreferences(this);
		return preferences.getBoolean(getString(R.string.pref_home_key_flash_light_chk_box), false);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(TAG, provider + " status changed : " + status);
		if(status == LocationProvider.TEMPORARILY_UNAVAILABLE ||
				   status == LocationProvider.OUT_OF_SERVICE) {
			if(provider == LocationManager.GPS_PROVIDER)
				m_GPSActivated = false;
		}
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, provider  + " is disabled");
		if(provider == LocationManager.GPS_PROVIDER)
			m_GPSActivated = false;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// No GPS ? just send the location
		if(!m_GPSActivated) {
			Log.i(TAG, "Location changed, No GPS => Send the SMS");
			sendLocationSMS(location,  mCaller, false);
			m_locationSMSSend = true;
		}
		// We are most likely the GPS and already got a locationUpdate from the network
		else if(lastLocation != null) {
			Log.i(TAG, "Location changed, GPS activated and already another location => Send the SMS");
			sendLocationSMS(bestBetween(location, lastLocation),  mCaller, false);
		} else {
			Log.i(TAG, "Location changed, GPS activated but no other location => do nothing");
			lastLocation = location;
		}
	}

	/**
	 * Get Last known location from the GPS or using the cell tower/wifi. 
	 * @return A Location object representing the last known location of the device
	 */
	private Location getLastLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location lastLoc = null;
		
		if(m_isLocAllowed) {
			lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(lastLoc == null)
				lastLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		return lastLoc;
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
		if(a.getAccuracy() < b.getAccuracy()) return a;
		else return b;
	}

}