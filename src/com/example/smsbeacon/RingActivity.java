package com.example.smsbeacon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class RingActivity extends Activity implements OnSeekBarChangeListener {
	private Camera m_cam = null;
	private boolean m_hasFlash = false;
	private Parameters m_param = null;
	private Ringtone m_rgtone = null;
	private int m_initVolume = 0;
	private  TextView m_phoneNumber = null;
	private String m_ringTime = null;
	AudioManager m_audioManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);
		SeekBar m_Bar;
		m_Bar = (SeekBar)findViewById(R.id.seekbar);
		m_Bar.setOnSeekBarChangeListener(this);
		m_phoneNumber = (TextView)findViewById(R.id.phone_number);
		m_phoneNumber.setText("06.86.65.37.93 tried to locate you");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		m_ringTime = prefs.getString(getBaseContext().getString(R.string.pref_home_key_time), "default choice");
		m_hasFlash = getApplicationContext().getPackageManager()
		        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		m_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		m_initVolume = m_audioManager.getStreamVolume(AudioManager.STREAM_RING);
		m_audioManager.setStreamVolume(
				   AudioManager.STREAM_RING,
				   m_audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
				   AudioManager.FLAG_PLAY_SOUND
				);
		if (shallUseFlash())
		{
			turnOnFlash();
		}
		turnOnRingTone();
		new Thread(new Runnable(){
			public void run(){
			try {
				if (m_ringTime.equals("time_elapse_30_sec"))
				{
					Thread.sleep(30000);
				}
				else if (m_ringTime.equals("time_elapse_1_min"))
				{
					Thread.sleep(60000);
				}
				else if (m_ringTime.equals("time_elapse_2_min"))
				{
					Thread.sleep(120000);
				}
				turnOffFlash();
				turnOffRingTone();
				m_audioManager.setStreamVolume(
						   AudioManager.STREAM_RING,
						   m_initVolume,
						   AudioManager.FLAG_PLAY_SOUND
						);
				RingActivity.this.finish();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
			}).start();
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
			turnOffFlash();
			turnOffRingTone();
			m_audioManager.setStreamVolume(
					   AudioManager.STREAM_RING,
					   m_initVolume,
					   AudioManager.FLAG_PLAY_SOUND
					);
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
