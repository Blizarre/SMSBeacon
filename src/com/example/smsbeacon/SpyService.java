package com.example.smsbeacon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SpyService extends Service {

	public final static String DATA_EXTRA_CALLER = "caller";
	
	private final String TAG = this.getClass().getSimpleName();
	
	
	private String mOriginator;
	
	
	@Override
	public IBinder onBind(Intent intent) {
        mOriginator = intent.getStringExtra(DATA_EXTRA_CALLER);
    	Log.i(TAG, "Spy service binded, sms sent from " + mOriginator);
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        mOriginator = intent.getStringExtra(DATA_EXTRA_CALLER);
    	Log.i(TAG, "Spy service started, sms sent from " + mOriginator);
    	return Service.START_STICKY; // TODO: look for something better
	}
	
    @Override
    public void onCreate() {
    	Log.i(TAG, "Spy service created, no data... pointless");
    }

}
