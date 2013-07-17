package com.example.smsbeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

	private final String TAG = this.getClass().getSimpleName();

	@Override
	/**
	 * Called when a SMS is received and check the content of the SMS for a special code.
	 * If the code is found, it start a new Intent. 
	 */
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "action received : " + intent.getAction());
		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Log.i(TAG, "SMS_RECEIVED action received");
			Bundle extras = intent.getExtras();

			if ( extras != null ) {
				// get the SMS data from the "pdus" key. Can hold multiple messages (multi-part ?)  
				Object[] smsextras = (Object[]) extras.get( "pdus" );

				for ( Object o : smsextras) {
					SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])o);
					String strMsgBody = smsmsg.getMessageBody();
					if(strMsgBody.equals(getTriggerCodeSMS(context))) {
						lostCodeDetected(context);
						break;
					} else if(strMsgBody.equals(getTheftCodeSMS(context))) {
						theftCodeDetected();
						break;
					}
				}

			}

		} else if(intent.getAction().equals("android.intent.action.PHONE_STATE")) {
			Log.i(TAG, "PHONE_STATE action received");
			Bundle extra = intent.getExtras();
			
			if(extra != null) {
				String newState = extra.getString(TelephonyManager.EXTRA_STATE);
				if(newState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
					String phoneNumber = extra.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
					Log.i(TAG, "Phone ringing, a call from '" + phoneNumber + "'");
					if(phoneNumber.equals(getRingPhoneNumber(context))) {
						specificCallerDetected(context);
					}
					
				}
			}
		}

	}



	private Object getRingPhoneNumber(Context c) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		return preferences.getString(c.getString(R.string.pref_key_specific_caller), "");
	}

	private String getTriggerCodeSMS(Context c) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		return preferences.getString(c.getString(R.string.pref_key_lost_passwd), "");
	}

	private String getTheftCodeSMS(Context c) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		return preferences.getString(c.getString(R.string.pref_key_theft_passwd), "");
	}

	private void specificCallerDetected(Context context) {
		Log.i(TAG, "Ringing volume is set to its maximum");		
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
	}
	
	private void  lostCodeDetected(Context context) {
		Log.i(TAG, "SMS lost code trigger detected");
		abortBroadcast(); // Do not dispatch the SMS to anybody else
		Intent newintent = new Intent(context, RingActivity.class);
		newintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(newintent);		
	}

	private void theftCodeDetected() {
		// TODO Auto-generated method stub
		Log.i(TAG, "SMS lost code trigger detected");
		abortBroadcast(); // Do not dispatch the SMS to anybody else		
	}	
}