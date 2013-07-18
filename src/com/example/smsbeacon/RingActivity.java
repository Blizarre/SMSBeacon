package com.example.smsbeacon;

import java.util.ArrayList;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

public class RingActivity extends Activity {
	private Camera m_cam = null;
	private boolean m_hasFlash = false;
	private Parameters m_param = null;
	private Ringtone m_rgtone = null;
	private int m_initVolume = 0;
	AudioManager m_audioManager;
	
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
				m_audioManager.setStreamVolume(
						   AudioManager.STREAM_RING,
						   m_initVolume,
						   AudioManager.FLAG_PLAY_SOUND
						);
				RingActivity.this.finish();
			}
		});
		m_hasFlash = getApplicationContext().getPackageManager()
		        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		m_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		m_initVolume = m_audioManager.getStreamVolume(AudioManager.STREAM_RING);
		m_audioManager.setStreamVolume(
				   AudioManager.STREAM_RING,
				   m_audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
				   AudioManager.FLAG_PLAY_SOUND
				);
		turnOnFlash();
		turnOnRingTone();
		displayCoordinates();
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

	//fonction à déplacer dans le service
	//une fois que t'auras fait ton boulot ;p
	protected void displayCoordinates()
	{
		double lon, lat, alt;
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		ArrayList<LocationProvider> providers = new ArrayList<LocationProvider>();
		ArrayList<String> names = (ArrayList<String>) locationManager.getProviders(true);
		 
		for(String name : names)
		  providers.add(locationManager.getProvider(name));
		
		Criteria critere = new Criteria();
		critere.setAccuracy(Criteria.ACCURACY_FINE);
		critere.setAltitudeRequired(false);
		critere.setBearingRequired(false);
		critere.setCostAllowed(false);
		// Criteria.POWER_HIGH pour une haute consommation, 
		// Criteria.POWER_MEDIUM pour une consommation moyenne 
		// et Criteria.POWER_LOW pour une basse consommation
		critere.setPowerRequirement(Criteria.POWER_HIGH);
		critere.setSpeedRequired(true);
		
		String bestProvider = locationManager.getBestProvider(critere, false);
		Location location = locationManager.getLastKnownLocation(bestProvider);
	    try {
	        lat = location.getLatitude();
	        lon = location.getLongitude();
	        alt = location.getAltitude();
	    } catch (NullPointerException e) {
	        lat = -1.0;
	        lon = -1.0;
	        alt = -1.0;
	    }
	    String positionStr = "longitude: " + lon + 
	    					 "\n latitude: " + lat +
	    					 "\n altitude:" + alt; 
	    Toast.makeText(this, positionStr, Toast.LENGTH_LONG).show();
	}
	    
}
