package com.hackday.fun.spotit;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationServicesManager implements LocationListener  {
	private static LocationServicesManager mInstance;
	private LocationManager mLocationManager;
	private String provider;
	private Context mContext;
	
	private LocationServicesManager(Context context) {
		mContext = context;
		init();
	}
	
	private void init() {
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		provider = mLocationManager.getBestProvider(criteria, true);
		mLocationManager.requestLocationUpdates(provider, 0, 5, this);
	}
	
	public static synchronized LocationServicesManager getInstance(Context context) {
		if(mInstance == null)
			mInstance = new LocationServicesManager(context);
		
		return mInstance;
	}

	public boolean isLocationServiceEnabled() {
		return mLocationManager.isProviderEnabled(provider);
	}

	public Location getLocation() {
		return mLocationManager.getLastKnownLocation(provider);
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}
