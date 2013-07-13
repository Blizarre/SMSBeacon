package com.example.smsbeacon;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;
import android.content.pm.PackageManager;

public class RingActivity extends Activity {
	Camera cam = null;
	boolean hasFlash = false;
	Parameters p = null;
	Ringtone r = null;
	
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
			}
		});
		
		hasFlash = getApplicationContext().getPackageManager()
		        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		turnOnFlash();
		turnOnRingTone();
		
	}
	
	protected void turnOnRingTone()
	{
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		r.play();
	}
	
	protected void turnOffRingTone()
	{
		r.stop();
	}
	
	protected void turnOnFlash()
	{
		if (hasFlash)
		{
			try {
					cam = Camera.open();     
					p = cam.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
					cam.setParameters(p);
					cam.startPreview();
				} catch (RuntimeException e) {
	            Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
	        }
		}
		else /*TODO: supprimer else inutile*/
		{
		Toast.makeText(RingActivity.this,
				"Your device do not have flash light",
				Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void turnOffFlash()
	{
		if (cam == null || p == null) {
            return;
        }
        p.setFlashMode(Parameters.FLASH_MODE_OFF);
        cam.setParameters(p);
        cam.stopPreview();
	}

}
