package com.example.smsbeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
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
		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Log.i(TAG, "SMS_RECEIVED action received");
			Bundle extras = intent.getExtras();

			if ( extras != null ) {
				// get the SMS data from the "pdus" key. Can hold multiple messages (multi-part ?)  
				Object[] smsextras = (Object[]) extras.get( "pdus" );

				for ( Object o : smsextras) {
					SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])o);
					String strMsgBody = smsmsg.getMessageBody();
					if(strMsgBody.equals(getTriggerCodeSMS())) {
						Log.i(TAG, "SMS trigger detected");
						abortBroadcast(); // Do not dispatch the SMS to anybody else
						Intent newintent = new Intent(context, Ringer.class);
						newintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(newintent);
					}
				}

			}

		}

	}

	private String getTriggerCodeSMS() {
		return "aaa";
	}
}