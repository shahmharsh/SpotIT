package com.hackday.fun.spotit.activities;

import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.hackday.fun.spotit.IHttpRequestCallback;
import com.hackday.fun.spotit.ParkingSpot;
import com.hackday.fun.spotit.R;
import com.hackday.fun.spotit.SpotItNetworkAPI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class BuildingParkingActivity extends Activity implements OnClickListener{

	private Spinner mBuilding;
	private Button mBtnClaim;
	private Button mBtnOwnSpot;
	private TextView mTxtViewSpotNo;
	private TextView mTxtViewFloor;
	private TextView mTxtViewBuilding;
	
	private ParkingSpot mParkingSpot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_building_spots);
		
		mTxtViewSpotNo = (TextView) findViewById(R.id.building_spot_no);
		mTxtViewBuilding = (TextView) findViewById(R.id.building_building);
		mTxtViewFloor = (TextView) findViewById(R.id.building_floor);
		
		mBuilding = (Spinner) findViewById(R.id.select_building);
		mBuilding.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
				if(mParkingSpot!=null)
					rejectSpot();
				String building = parentView.getItemAtPosition(position).toString();
				String url = SpotItNetworkAPI.BUILDING_BASED_SPOTS_URL + "/" + building;
				SpotItNetworkAPI.getInstance().doGet(url, new IHttpRequestCallback() {
					
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

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				Toast.makeText(getApplicationContext(), "nothing selected", Toast.LENGTH_SHORT).show();
			}
		});
		
		mBtnClaim = (Button) findViewById(R.id.claim_building_spot);
		mBtnClaim.setOnClickListener(this);
		
		mBtnOwnSpot = (Button) findViewById(R.id.own_spot);
		mBtnOwnSpot.setOnClickListener(this);	
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
			case R.id.claim_building_spot:
				if(mParkingSpot!=null)
					claimSpot();
				break;
			
			case R.id.own_spot:
				if(mParkingSpot!=null)
					rejectSpot();
				Intent userSelectSpotActivityIntent = new Intent(this, UserSelectSpotActivity.class);
				startActivity(userSelectSpotActivityIntent);
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
