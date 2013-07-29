package com.example.smsbeacon;

import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class RingActivity extends Activity implements OnSeekBarChangeListener {
	private Camera m_cam = null;
	private boolean m_hasFlash = false;
	private Parameters m_param = null;
	private Ringtone m_rgtone = null;
	private int m_initVolume = 0;
	private  TextView m_phoneNumber = null;
	private String m_ringTime = null;
	AudioManager m_audioManager;
	
	public static final String DATA_EXTRA_CALLER = "param_intent_caller";
	public static final String DATA_EXTRA_ACTION = "param_intent_action";
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
		Dictionary<String, Integer> waitTime = new Hashtable<String, Integer>();
		
		waitTime.put("time_elapse_30_sec", 30);
		waitTime.put("time_elapse_1_min" , 60);
		waitTime.put("time_elapse_2_min" ,120);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, waitTime.get(m_ringTime));
		
		t.schedule( new TimerTask() {
			
			@Override
			public void run() {
				stopRingTone();
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
		String callerNumber = origin.getStringExtra(DATA_EXTRA_CALLER);
		Action act = (Action)origin.getSerializableExtra(DATA_EXTRA_ACTION);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);
		
		prepareInterfaceElements(act, callerNumber);
		
		if (shallUseFlash())
		{
			turnOnFlash();
		}
		
		if(act == Action.RINGTONE) {
			prepareRingTone();
			turnOnRingTone();
			prepareRingToneStopThread();
		} else if(act == Action.SMS_LOCATION) {
			sendLocationSMS(getLastLocation());
			prepareLocationThread();
		}

	}


	private void prepareLocationThread() {
		// TODO Auto-generated method stub
		
	}


	private void sendLocationSMS(Object lastLocation) {
		// TODO Auto-generated method stub
		
	}


	private Object getLastLocation() {
		// TODO Auto-generated method stub
		return null;
	}


	protected void prepareInterfaceElements(Action act, String callerNumber) {
		SeekBar m_Bar;
		String message;
		
		m_Bar = (SeekBar)findViewById(R.id.seekbar);
		m_Bar.setOnSeekBarChangeListener(this);
		m_phoneNumber = (TextView)findViewById(R.id.phone_number);
		if(act == Action.RINGTONE)
			message = getString(R.string.msg_ringtone_num_fmt, callerNumber);
		else if(act == Action.SMS_LOCATION)
			message = getString(R.string.msg_location_sms_num_fmt, callerNumber);
		
		m_phoneNumber.setText(message);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		m_ringTime = prefs.getString(getBaseContext().getString(R.string.pref_home_key_time), "default choice");
		m_hasFlash = getApplicationContext().getPackageManager()
		        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
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
			stopRingTone();
			RingActivity.this.finish();
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
		Context c = getApplicationContext();
		SharedPreferences preferences = PreferenceManager.
									getDefaultSharedPreferences(c);
		return preferences.getBoolean(c.getString(R.string.pref_home_key_flash_light_chk_box), false);
	}
}
