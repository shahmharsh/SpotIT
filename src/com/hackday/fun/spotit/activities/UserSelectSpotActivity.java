package com.hackday.fun.spotit.activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.hackday.fun.spotit.IHttpRequestCallback;
import com.hackday.fun.spotit.LocationServicesManager;
import com.hackday.fun.spotit.ParkingSpot;
import com.hackday.fun.spotit.R;
import com.hackday.fun.spotit.SpotItNetworkAPI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class UserSelectSpotActivity extends Activity{

	private ListViewAdapter mDataAdapter;
	private Button mBtnManualEntry;
	private JSONArray mJsonArray;
	private EditText spotNo;
	private Spinner building;
	private Spinner floor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_select_spots);
		
		
		
		
		displayListView();
	}
	
	private void displayListView() {
		Location location = LocationServicesManager.getInstance(getApplicationContext()).getLocation();
		
		String latitude = "";
		String longitude = "";
		
		if(location!=null) {
			latitude = String.valueOf(location.getLatitude());
			longitude = String.valueOf(location.getLongitude());
		}
		
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.encodedPath(SpotItNetworkAPI.GUESS_URL)
		  .appendQueryParameter("lat", latitude)
		  .appendQueryParameter("long", longitude);

		SpotItNetworkAPI.getInstance().doGet(uriBuilder.toString(), new IHttpRequestCallback() {
	
			@Override
			public void onSuccess(String response) {
				// create an ArrayAdaptar from the String Array
				mDataAdapter = new ListViewAdapter(UserSelectSpotActivity.this, R.layout.parking_spot_item, getDataForListView(response));
				ListView listView = (ListView) findViewById(R.id.list);
				
				View v = getLayoutInflater().inflate(R.layout.list_view_footer, null);
				listView.addFooterView(v);
				
				// Assign adapter to ListView
				listView.setAdapter(mDataAdapter);

				listView.setOnItemClickListener(new OnItemClickListener() {

					  @Override
					  public void onItemClick(AdapterView<?> parentView, View view, final int postion, long viewID) {
						  try {
							final ParkingSpot parkingSpot = new ParkingSpot(mJsonArray.getJSONObject(postion).getString("spot"), mJsonArray.getJSONObject(postion).getString("pk_lot"), mJsonArray.getJSONObject(postion).getString("floor"));
							parkingSpot.claim(new IHttpRequestCallback() {
								
								@Override
								public void onSuccess(String response) {
									Gson gson = new Gson();
									String json = gson.toJson(parkingSpot);
									FileOutputStream fos;
									try {
										fos = openFileOutput(ParkingSpot.FILENAME, Context.MODE_PRIVATE);
										fos.write(json.getBytes());
										fos.close();
										Toast.makeText(getApplicationContext(), "Success claim",  Toast.LENGTH_SHORT).show();
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
								}
								
								@Override
								public void onFailure() {
									
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
						}
							
					  }
				});
				
				
				
				mBtnManualEntry = (Button) findViewById(R.id.manual_entry);
				mBtnManualEntry.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// get prompts.xml view
						LayoutInflater layoutInflater = LayoutInflater.from(UserSelectSpotActivity.this);
						View promptView = layoutInflater.inflate(R.layout.manual_entry, null);
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserSelectSpotActivity.this);
						// set prompts.xml to be the layout file of the alertdialog builder
						alertDialogBuilder.setView(promptView);
						// setup a dialog window
						alertDialogBuilder.setCancelable(false)
										  .setPositiveButton("Claim", new DialogInterface.OnClickListener() {
											  							public void onClick(DialogInterface dialog, int id) {
											  								//get user input and set it to result
											  								

											  								final ParkingSpot parkingSpot = new ParkingSpot(spotNo.getText().toString(), building.getSelectedItem().toString(), floor.getSelectedItem().toString());
											  								
											  								Uri.Builder uriBuilder = new Uri.Builder();
											  								uriBuilder.encodedPath(SpotItNetworkAPI.FINAL_CLAIM_URL)
											  										  .appendQueryParameter("pk_lot", parkingSpot.getBuilding())
											  										  .appendQueryParameter("floor", parkingSpot.getFloor())
											  										  .appendQueryParameter("spot", parkingSpot.getSpotNo());
											  								
											  								parkingSpot.claim(new IHttpRequestCallback() {
																				
																				@Override
																				public void onSuccess(String response) {
																					Gson gson = new Gson();
																					String json = gson.toJson(parkingSpot);
																					try {
																						FileOutputStream fos = openFileOutput(ParkingSpot.FILENAME, Context.MODE_PRIVATE);
																						fos.write(json.getBytes());
																						fos.close();
																						Toast.makeText(getApplicationContext(), "Success claim",  Toast.LENGTH_SHORT).show();
																						//TODO: go to another activity with hurray message
																					} catch (IOException e) {
																						e.printStackTrace();
																					}			
																				}
																				
																				@Override
																				public void onFailure() {
																					// TODO Auto-generated method stub
																					
																				}
																			});
											  							}
						
						                                	})
						                  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						                	  							public void onClick(DialogInterface dialog, int id) {
						                	  								dialog.cancel();
						                	  							}
						                                	});
						//create an alert dialog
						AlertDialog alertD = alertDialogBuilder.create();
						alertD.show();
						
						building = (Spinner) alertD.findViewById(R.id.manual_building);
						floor = (Spinner) alertD.findViewById(R.id.manual_floor);
						spotNo = (EditText) alertD.findViewById(R.id.manual_spot_no);
					}
				});
				
			}
			
			@Override
			public void onFailure() {
				Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
			}
		});
		
		
	}


	private class ListViewAdapter extends ArrayAdapter<ParkingSpot>{
		
		 private List<ParkingSpot> parkingSpotList ;

		 public ListViewAdapter(Context context, int textViewResourceId, ArrayList<ParkingSpot> parkingSpotList) {
				   super(context, textViewResourceId, parkingSpotList);
				   this.parkingSpotList = new ArrayList<ParkingSpot>();
				   this.parkingSpotList.addAll(parkingSpotList);
		 }
		 
		 private class ViewHolder {
			  TextView spot;
			  TextView building;
			  TextView floor;
		 }
		 
		 @Override
		 public View getView(int position, View convertView, ViewGroup parent) {
			 ViewHolder holder = null;
			 
			 if (convertView == null) {

				    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				    convertView = vi.inflate(R.layout.parking_spot_item, null);

				    holder = new ViewHolder();
				    holder.spot = (TextView) convertView.findViewById(R.id.list_spot_no);
				    holder.building = (TextView) convertView.findViewById(R.id.list_building);
				    holder.floor = (TextView) convertView.findViewById(R.id.list_floor);

				    convertView.setTag(holder);
				   } else {
				    holder = (ViewHolder) convertView.getTag();
				   }
			 
			 ParkingSpot parkingSpot = parkingSpotList.get(position);

			   holder.spot.setText(parkingSpot.getSpotNo());
			   holder.floor.setText(parkingSpot.getFloor());
			   holder.building.setText(parkingSpot.getBuilding());

			   holder.spot.setTag(parkingSpot);

			   return convertView;
		 }
	}
	
	public ArrayList<ParkingSpot> getDataForListView(String response) {
		ArrayList<ParkingSpot> parkingSpotList = new ArrayList<ParkingSpot>();
		try {
			
			JSONObject jsonObj = new JSONObject(response);
			mJsonArray = jsonObj.getJSONArray("spots");
			for(int i=0;i<mJsonArray.length();i++) {
				String building = mJsonArray.getJSONObject(i).getString("pk_lot");
				String floor = mJsonArray.getJSONObject(i).getString("floor");
				String spot = mJsonArray.getJSONObject(i).getString("spot");
		    	ParkingSpot parkingSpot = new ParkingSpot(spot, building, floor);
				parkingSpotList.add(parkingSpot);
		    }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    

	    return parkingSpotList;
	}
}
