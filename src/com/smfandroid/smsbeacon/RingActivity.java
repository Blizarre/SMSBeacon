package com.smfandroid.smsbeacon;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class RingActivity extends Activity implements OnSeekBarChangeListener {
	protected RingToneHandler mRingTone;
	protected GPSOverSMSHandler mGPS;


	private String mCaller;
	AudioManager m_audioManager;
	
	private final String TAG = getClass().getName();
	
	private boolean mIsRingToneAction = false;
	private boolean mIsGPSAction = false;;
	private boolean mIsLocAllowed = false;

	public enum Action { RINGTONE, SMS_LOCATION };
	public static final String DATA_EXTRA_CALLER = "param_intent_caller";
	public static final String DATA_EXTRA_ACTION = "param_intent_action";

	protected void stopRingToneActions() {
		Log.i(TAG, "Stop RingTone action");
		mRingTone.stopRingTone();
	}

	private void stopGPSAction() {
		Log.i(TAG, "Stop GPS action");
		mGPS.stop();
	}
	
	@Override
	protected void onNewIntent(Intent newIntent) {
		initNewAction(newIntent);
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);

		initNewAction(getIntent());

	}

	protected void initNewAction(Intent origin) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mIsLocAllowed = prefs.getBoolean(getString(R.string.pref_gal_act_key), false);
		
		mCaller = origin.getStringExtra(DATA_EXTRA_CALLER);
		Action action = (Action)origin.getSerializableExtra(DATA_EXTRA_ACTION);

		prepareInterfaceElements(action, mCaller);
		
		Log.i(TAG, "RingActivity Created, action: " + action.name());
		
		if(action == Action.RINGTONE) {
			if(mIsRingToneAction) 
				mRingTone.stopRingTone();
			else
				mRingTone = new RingToneHandler(this);
			
			mIsRingToneAction = true;

			int ringTime = Integer.parseInt(prefs.getString(getString(R.string.pref_home_key_time), "default choice"));
			mRingTone.startRingTone(ringTime * 1000, shallUseFlash());
			
		} else if(action == Action.SMS_LOCATION && mIsLocAllowed) {
			if(mIsGPSAction)
				mGPS.stop();
			else
				mGPS = new GPSOverSMSHandler(this);

			mGPS.sendLocationSMS(mGPS.getLastLocation(), mCaller, true);
			mGPS.startASyncLocService(mCaller);
			mIsGPSAction = true;
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
		
		boolean flashAvailable = FlashLightHandler.hasFlashLight(this);
		
		boolean flashEnabled = preferences.getBoolean(
				getString(R.string.pref_home_key_flash_light_chk_box), 
				false); 
		
		return flashEnabled & flashAvailable; 
	}


}