package com.hackday.fun.spotit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class SpotItNetworkAPI {
	
	public static final String DATA_ENTRY_URL = "http://10.73.221.73:8080/v1/dataentry/location";
	public static final String LOCATION_BASED_SPOTS_URL = "http://10.73.221.73:8080/v1/spot";
	public static final String FINAL_CLAIM_URL = "http://10.73.221.73:8080/v1/spot/claim";
	public static final String REJECT_URL = "http://10.73.221.73:8080/v1/spot/reject";
	public static final String BUILDING_BASED_SPOTS_URL = "http://10.73.221.73:8080/v1/spot/bldg";
	public static final String GUESS_URL = "http://10.73.221.73:8080/v1/spot/guess";
	
	
	private static SpotItNetworkAPI mInstance;
	
	private HttpClient mHttpClient;
	
	private SpotItNetworkAPI() {
		init();
	}
	
	private void init() {
		mHttpClient = new DefaultHttpClient();
	}
	
	public static synchronized SpotItNetworkAPI getInstance() {
		if(mInstance == null) {
			mInstance = new SpotItNetworkAPI();
		}
		return mInstance;
	}
	
	public void doPost(String url, IHttpRequestCallback callback) {
		new HttpRequestTask(callback).doPost(url);
	}
	
	public void doGet(String url, IHttpRequestCallback callback) {
		new HttpRequestTask(callback).doGet(url);
	}
	
	private class HttpRequestTask extends AsyncTask<String, Void, String> {

		private IHttpRequestCallback mCallback;
		private boolean doPost;
		
		public HttpRequestTask(IHttpRequestCallback callback) {
			mCallback = callback;
		}
		
		public void doGet(String url) {
			doPost = false;
			execute(url);
		}
		
		public void doPost(String url) {
			doPost = true;
			execute(url);
		}

		@Override
		protected String doInBackground(String... uri) {
			HttpResponse httpResponse;
			String responseString = null;
			try {
				if(doPost)
					httpResponse = mHttpClient.execute(new HttpPost(uri[0]));
				else
					httpResponse = mHttpClient.execute(new HttpGet(uri[0]));
				
				StatusLine statusLine = httpResponse.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                httpResponse.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                httpResponse.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return responseString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result!=null)
				mCallback.onSuccess(result);
			else
				mCallback.onFailure();
		}
		
	}
}

	
