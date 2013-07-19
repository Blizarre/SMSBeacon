package com.example.smsbeacon;

import java.text.DateFormat;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TimeFormatException;

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
		sendCoordinates();
		return Service.START_STICKY; // TODO: look for something better
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "Spy service created, no data... pointless");
	}


	protected void sendCoordinates() {
		double lon, lat, alt, prec;
		long temp;
		String lastFix;
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Location location = getFinePosition(locationManager);
		
		StringBuilder positionStr = new StringBuilder();
		positionStr.append("lost phone position :\n");
		
		// Not found anything ? not a problem, GPS may not be started. Try something less precise
		if (location == null)
			location = getCoarsePosition(locationManager);
		
		// Well, at least we tried ...
		if(location == null) {
			positionStr.append(" no position available");
		} else {
			lat = location.getLatitude();
			lon = location.getLongitude();
			alt = location.getAltitude();
			prec = location.getAccuracy();
			temp = location.getTime();
			
			positionStr.append(String.format("%.5f E, %.5f N (alt: %.1fm)\n", lon, lat, alt));
			lastFix = DateFormat.getDateTimeInstance().format(new Date(temp));
			
			positionStr.append(String.format("accuracy : %.1f m, time : %s" , prec, lastFix));
		}
		Log.i(TAG, "send sms to " + mOriginator + " with content '" + positionStr + "'");
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(mOriginator, null, positionStr.toString(), null, null);
	}

	private Location getFinePosition(LocationManager locationManager) {
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
		Log.i(TAG, "Selected provider for the fine estimation of position : " + bestProvider);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		return location;
	}

	private Location getCoarsePosition(LocationManager locationManager) {
		Criteria critere = new Criteria();
		critere.setAccuracy(Criteria.ACCURACY_COARSE);
		critere.setAltitudeRequired(false);
		critere.setBearingRequired(false);
		critere.setCostAllowed(false);
		// Criteria.POWER_HIGH pour une haute consommation,
		// Criteria.POWER_MEDIUM pour une consommation moyenne
		// et Criteria.POWER_LOW pour une basse consommation
		critere.setPowerRequirement(Criteria.POWER_HIGH);
		critere.setSpeedRequired(false);

		String bestProvider = locationManager.getBestProvider(critere, false);
		Log.i(TAG, "Selected provider for the coarse estimation of position : " + bestProvider);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		return location;
	}
	
}
