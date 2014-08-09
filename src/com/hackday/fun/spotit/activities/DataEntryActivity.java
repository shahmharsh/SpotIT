package com.hackday.fun.spotit.activities;

import com.hackday.fun.spotit.IHttpRequestCallback;
import com.hackday.fun.spotit.LocationServicesManager;
import com.hackday.fun.spotit.R;
import com.hackday.fun.spotit.SpotItNetworkAPI;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DataEntryActivity extends Activity implements OnClickListener{
	
	private EditText spotNo;
	private Button send;
	private Spinner building;
	private Spinner floor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_entry);
		
		building = (Spinner) findViewById(R.id.building);
		floor = (Spinner) findViewById(R.id.floor);
		spotNo = (EditText) findViewById(R.id.spot_no);
		send = (Button) findViewById(R.id.populate_db_on_server);
		send.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		LocationServicesManager locationServiceManager = LocationServicesManager.getInstance(getApplicationContext());
		Location location = locationServiceManager.getLocation();
		String latitude = "";
		String longitude = "";
		
		if(location!=null) {
			latitude = String.valueOf(location.getLatitude());
			longitude = String.valueOf(location.getLongitude());
		}
			
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.encodedPath(SpotItNetworkAPI.DATA_ENTRY_URL)
				  .appendQueryParameter("lat", latitude)
				  .appendQueryParameter("long", longitude)
				  .appendQueryParameter("pk_lot", building.getSelectedItem().toString())
				  .appendQueryParameter("floor", floor.getSelectedItem().toString())
				  .appendQueryParameter("spot", spotNo.getText().toString());
		
		SpotItNetworkAPI.getInstance().doPost(uriBuilder.toString(), new IHttpRequestCallback() {
			
			@Override
			public void onSuccess(String response) {
				Toast.makeText(getApplicationContext(), "Entry Successful", Toast.LENGTH_SHORT).show();;
			}
			
			@Override
			public void onFailure() {
				Toast.makeText(getApplicationContext(), "Entry failed", Toast.LENGTH_SHORT).show();
			}
		});
		
	}

}
