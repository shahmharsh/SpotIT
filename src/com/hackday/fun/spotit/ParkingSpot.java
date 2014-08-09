package com.hackday.fun.spotit;

import android.net.Uri;

public class ParkingSpot {
	private String building;
	private String floor;
	private String spotNo;
	public static final String FILENAME = "myspot";
	
	public ParkingSpot(String spotNo, String building, String floor) {
		this.building = building;
		this.floor = floor;
		this.spotNo = spotNo;
	}
	
	public String getBuilding() {
		return building;
	}
	
	public String getFloor() {
		return floor;
	}
	
	public String getSpotNo() {
		return spotNo;
	}
	
	
	public void claim(IHttpRequestCallback callback) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.encodedPath(SpotItNetworkAPI.FINAL_CLAIM_URL)
				  .appendQueryParameter("pk_lot", building)
				  .appendQueryParameter("floor", floor)
				  .appendQueryParameter("spot", spotNo);
		SpotItNetworkAPI.getInstance().doGet(uriBuilder.toString(), callback);
	}
	
	public void reject(IHttpRequestCallback callback) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.encodedPath(SpotItNetworkAPI.REJECT_URL)
				  .appendQueryParameter("pk_lot", building)
				  .appendQueryParameter("floor", floor)
				  .appendQueryParameter("spot", spotNo);
		SpotItNetworkAPI.getInstance().doGet(uriBuilder.toString(), callback);
	}
}
