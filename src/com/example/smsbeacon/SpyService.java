package com.example.smsbeacon;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

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

	// j'ai que des -1, à ton tour de faire ton boulot :p
	protected void sendCoordinates() {
		double lon, lat, alt;
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		ArrayList<LocationProvider> providers = new ArrayList<LocationProvider>();
		ArrayList<String> names = (ArrayList<String>) locationManager
				.getProviders(true);

		for (String name : names)
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

		if (location != null) {
			lat = location.getLatitude();
			lon = location.getLongitude();
			alt = location.getAltitude();
		} else {
			lat = -1.0;
			lon = -1.0;
			alt = -1.0;
		}
		String positionStr = "lost phone : longitude: " + lon + "\n latitude: " + lat
				+ "\n altitude:" + alt;
		Log.i(TAG, "send sms to " + mOriginator + " with content '" + positionStr + "'");
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(mOriginator, null, positionStr, null, null);
	}
}
