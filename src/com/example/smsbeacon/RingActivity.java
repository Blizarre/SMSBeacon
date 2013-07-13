package com.example.smsbeacon;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

public class RingActivity extends Activity {
	private Camera m_cam = null;
	private boolean m_hasFlash = false;
	private Parameters m_param = null;
	private Ringtone m_rgtone = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);
		Button Bn;
		
		Bn = (Button)findViewById(R.id.stop);		
		Bn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				turnOffFlash();
				turnOffRingTone();
				RingActivity.this.finish();
			}
		});
		m_hasFlash = getApplicationContext().getPackageManager()
		        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(
				   AudioManager.STREAM_RING,
				   audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
				   AudioManager.FLAG_PLAY_SOUND
				);
		turnOnFlash();
		turnOnRingTone();
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

}
