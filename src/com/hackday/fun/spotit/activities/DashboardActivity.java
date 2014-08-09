package com.hackday.fun.spotit.activities;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.hackday.fun.spotit.IHttpRequestCallback;
import com.hackday.fun.spotit.LocationServicesManager;
import com.hackday.fun.spotit.ParkingSpot;
import com.hackday.fun.spotit.R;
import com.hackday.fun.spotit.SpotItNetworkAPI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class DashboardActivity extends Activity implements OnClickListener {
	
	private Button mBtnClaimSpot;
	private Button mBtnDataEntry;
	private Button mBtnOtherSpots;
	private TextView mTxtViewSpotNo;
	private TextView mTxtViewFloor;
	private TextView mTxtViewBuilding;
	
	private ParkingSpot mParkingSpot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);

		mTxtViewSpotNo = (TextView) findViewById(R.id.spot_no);
		mTxtViewBuilding = (TextView) findViewById(R.id.building);
		mTxtViewFloor = (TextView) findViewById(R.id.floor);
		
		mBtnClaimSpot = (Button) findViewById(R.id.claim_spot);
		mBtnClaimSpot.setOnClickListener(this);
		mBtnOtherSpots = (Button) findViewById(R.id.other_spot);
		mBtnOtherSpots.setOnClickListener(this);
		mBtnDataEntry = (Button) findViewById(R.id.data_entry);
		mBtnDataEntry.setOnClickListener(this);		
		
		//initLayout();
	}

	@Override
	protected void onStart() {
		super.onStart();
		String json = null;
		 try {
			FileInputStream fis = openFileInput(ParkingSpot.FILENAME);
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
		        sb.append(line);
		    }
		    inputStreamReader.close();
		    json = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		 if(!"".equals(json) && json != null) {
			 Gson gson = new Gson();
			 ParkingSpot obj = gson.fromJson(json, ParkingSpot.class);
			 ReleaseParkingSpotActivity.parkingSpot = obj;
			 Intent releaseParkingSpotIntent = new Intent(this, ReleaseParkingSpotActivity.class);
			 startActivity(releaseParkingSpotIntent);
		 }
		 else {
			 if(mParkingSpot == null) {
				 LocationServicesManager locationServiceManager = LocationServicesManager.getInstance(getApplicationContext());
					if(locationServiceManager.isLocationServiceEnabled()) {
						Location location = locationServiceManager.getLocation();
						if(location!=null) {
							
							Uri.Builder uriBuilder = new Uri.Builder();
							uriBuilder.encodedPath(SpotItNetworkAPI.LOCATION_BASED_SPOTS_URL)
									  .appendQueryParameter("lat", String.valueOf(location.getLatitude()))
									  .appendQueryParameter("long", String.valueOf(location.getLongitude()));
							
							SpotItNetworkAPI.getInstance().doGet(uriBuilder.toString(), new IHttpRequestCallback() {
								
								@Override
								public void onSuccess(String response) {
									Toast.makeText(getApplicationContext(), "Success",  Toast.LENGTH_SHORT).show();
									displayParkingSpot(response);
								}
	
								@Override
								public void onFailure() {
									Toast.makeText(getApplicationContext(), "Server Error",  Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
					else {
						//TODO: prompt user if he wants to enable location and then send him to settings
						  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						  startActivity(intent);
					}
			 }
		 }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(mParkingSpot != null)
			rejectSpot();
	}


	private void displayParkingSpot(String response) {
		try {
			
			JSONObject jsonObj = new JSONObject(response);
			String building = jsonObj.getString("pk_lot");
			String floor = jsonObj.getString("floor");
			String spot = jsonObj.getString("spot");
			mParkingSpot = new ParkingSpot(spot, building, floor);
			mTxtViewSpotNo.setText(spot);
			mTxtViewBuilding.setText(building);
			mTxtViewFloor.setText(floor);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.claim_spot:
				if(mParkingSpot != null)
					claimSpot();
				break;
				
			case R.id.other_spot:
				if(mParkingSpot != null)
					rejectSpot();
				Intent buildingParkingActivityIntent = new Intent(this, BuildingParkingActivity.class);
				startActivity(buildingParkingActivityIntent);
				break;
			
			case R.id.data_entry:
				Intent dataEntryActivityIntent = new Intent(this, DataEntryActivity.class);
				startActivity(dataEntryActivityIntent);
				break;
				
			default:
				break;
		}
	}
	
	private void claimSpot() {
		mParkingSpot.claim(new IHttpRequestCallback() {
			
			@Override
			public void onSuccess(String response) {
				Gson gson = new Gson();
				String json = gson.toJson(mParkingSpot);
				try {
					FileOutputStream fos = openFileOutput(ParkingSpot.FILENAME, Context.MODE_PRIVATE);
					fos.write(json.getBytes());
					fos.close();
					Toast.makeText(getApplicationContext(), "Success claim",  Toast.LENGTH_SHORT).show();
					mParkingSpot = null;
					//TODO: go to another activity with hurray message
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
			
			@Override
			public void onFailure() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "failure claim",  Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void rejectSpot() {
		mParkingSpot.reject(new IHttpRequestCallback() {
			
			@Override
			public void onSuccess(String response) {
				mParkingSpot = null;
				Toast.makeText(getApplicationContext(), "success reject",  Toast.LENGTH_SHORT).show();
				
			}
			
			@Override
			public void onFailure() {
				//TODO:
				Toast.makeText(getApplicationContext(), "failure reject",  Toast.LENGTH_SHORT).show();
				
			}
		});
	}
}
