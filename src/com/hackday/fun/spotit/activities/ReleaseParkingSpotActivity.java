package com.hackday.fun.spotit.activities;

import com.hackday.fun.spotit.IHttpRequestCallback;
import com.hackday.fun.spotit.ParkingSpot;
import com.hackday.fun.spotit.R;
import com.hackday.fun.spotit.SpotItNetworkAPI;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ReleaseParkingSpotActivity extends Activity {
	public static ParkingSpot parkingSpot;

	private TextView mTxtViewSpotNo;
	private TextView mTxtViewFloor;
	private TextView mTxtViewBuilding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_release_parking);
	
		mTxtViewSpotNo = (TextView) findViewById(R.id.release_spot_no);
		mTxtViewBuilding = (TextView) findViewById(R.id.release_building);
		mTxtViewFloor = (TextView) findViewById(R.id.release_floor);
		
		mTxtViewBuilding.setText(parkingSpot.getBuilding());
		mTxtViewFloor.setText(parkingSpot.getFloor());
		mTxtViewSpotNo.setText(parkingSpot.getSpotNo());
	
	}
	
	public void releaseParking(View v) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.encodedPath(SpotItNetworkAPI.REJECT_URL)
				  .appendQueryParameter("pk_lot", parkingSpot.getBuilding())
				  .appendQueryParameter("floor", parkingSpot.getFloor())
				  .appendQueryParameter("spot", parkingSpot.getSpotNo());
		
		SpotItNetworkAPI.getInstance().doGet(uriBuilder.toString(), new IHttpRequestCallback() {
			
			@Override
			public void onSuccess(String response) {
				deleteFile(ParkingSpot.FILENAME);		
				Toast.makeText(getApplicationContext(), "Spot Removed", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onFailure() {
				Toast.makeText(getApplicationContext(), "Removal failed", Toast.LENGTH_SHORT).show();
			}
		});
	}

	
}
