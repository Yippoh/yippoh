package jim.h.common.android.zxingjar.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class History extends Activity {
	
	private static String apiCallURL = "http://115.112.70.158/barcode_stats/history.php";
	ProgressDialog progDialog;
	private int progressBarStatus = 0;
	String lat,lon,DeviceId;
	private TableLayout history;
	private TextView msgLbl;
	String lblLatitude,lblLongitude,lblStoreName;
	JSONArray histroyArray=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		msgLbl=(TextView)findViewById(R.id.msgLbl);
//		lblLatitude=(TextView)findViewById(R.id.lblLatitude);
//		lblLongitude=(TextView)findViewById(R.id.lblLongitude);
//		lblStoreName=(TextView)findViewById(R.id.lblStoreName);
		
		history=(TableLayout)findViewById(R.id.historyDetailsTbl);
		history.setVisibility(View.GONE);
		 getDeviceId();
 		 getLocation();
 		 ShowHistroy();
	}
	
//	public void HideProgressBar()
//	{
//		 new Thread(new Runnable() {
//			  public void run() {
//						// sleep 5 seconds, so that you can see the 100%
//						try {
//							Thread.sleep(100);
//							// close the progress bar dialog
//							progDialog.dismiss();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//						}
//			  }).start();
//	}
	public void getLocation()
	{
	    // GPSTracker class
	    GPSTracker gps;
	    gps = new GPSTracker(this);
	    
	    // check if GPS enabled     
	    if(gps.canGetLocation()){
	         
	        double latitude = gps.getLatitude();
	        double longitude = gps.getLongitude();
	     // \n is for new line
//	        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, 800).show();    
	        lat=String.valueOf(latitude);
	        lon=String.valueOf(longitude);
	        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lat + "\nLong: " + lon , 500).show();   
	        
	       
	    }else{
	        // can't get location
	        // GPS or Network is not enabled
	        // Ask user to enable GPS/network in settings
	        gps.showSettingsAlert();
	        Toast.makeText(getApplicationContext(), "Unable to get Location" , 500).show();   
	    }
	    
	}
	public void getDeviceId()
	{
		
	     TelephonyManager telephonyManager  =  (TelephonyManager)getSystemService( Context.TELEPHONY_SERVICE );
			    
		   /*
		* getDeviceId() function Returns the unique device ID.
		* for example,the IMEI for GSM and the MEID or ESN for CDMA phones.  
		*/				    
	     DeviceId = telephonyManager.getDeviceId();
//	     Toast.makeText(getApplicationContext(), "DeviceId:" +DeviceId, 500).show();   
	}
	

	public void ShowHistroy()
	{
//		HideProgressBar();
		JSONObject json = getHistory(lat,lon,DeviceId);
		if(json!=null)
		{
//			HideProgressBar();
//			Toast.makeText(getApplicationContext(), "json:"+json, 1000).show();
			try 
			{
				String message = json.getString("message");
				if(message.equalsIgnoreCase("No Data Found")==true)
				{
					 Toast.makeText(getApplicationContext(), "No Data Found", 500).show();
					 history.setVisibility(View.GONE);
					 msgLbl.setVisibility(View.VISIBLE);
					 msgLbl.setText("No Data Found");
				}
				else
				{
					history.setVisibility(View.VISIBLE);
					msgLbl.setVisibility(View.GONE);
					if(json.getString("history")!="null")
					{
						histroyArray= new JSONArray(json.getString("history"));
						if(histroyArray.length()>0)
						{
							int count =0;
							for (int i=0; i<histroyArray.length(); i++)
						    {
								 count=count+1;
								 JSONObject c= histroyArray.getJSONObject(i);
								 TableRow myTableRow = new TableRow(this);
								 myTableRow.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
							     // Add name
								 myTableRow.addView(createTextView(c.getString("product_name")));
								 myTableRow.addView(createTextView(c.getString("latitude")));
								 myTableRow.addView(createTextView(c.getString("longitude")));
								
								 if(count%2!=0){
							         myTableRow.setBackgroundColor(Color.parseColor("#F2F2F2"));
							    }
							    else
							    {
							    	myTableRow.setBackgroundColor(Color.parseColor("#D2D2D2"));
							    }
								 history.addView(myTableRow);
								 history.addView(createImageView());
						    }
						}
					}
					else
					{
						 history.setVisibility(View.GONE);
						 msgLbl.setVisibility(View.VISIBLE);
						 msgLbl.setText("Check your Internet connection");
					}
					
				}
			  
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else
		{
			msgLbl.setVisibility(View.VISIBLE);
			msgLbl.setText("Check your Internet connection");
			Toast.makeText(getApplicationContext(), "No Values Retrieved...", 1000).show();
		}
		
	}
	public TableRow createRow(String name)
	{
	    // Create new row
	    TableRow myTableRow = new TableRow(this);
	   
	    myTableRow.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//	    myTableRow.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
//	    myTableRow.setPadding(5, 15, 5, 5);
	    // Add name
	    myTableRow.addView(createTextView(name));
	   
	    // Add type
//	    myTableRow.addView(createTextView(type));
	    for(int i=0;i<histroyArray.length();i++)
	    {
	    	if(i%2==0)
	    	{
	    		  myTableRow.setBackgroundColor(Color.parseColor("#c2c2c2"));
	    	}
	    	else
	    	{
	    		  myTableRow.setBackgroundColor(Color.parseColor("#f3f1f1"));
	    	}
	    }
	    return myTableRow;
	    
	}
	public View createTextView(String name) {
		// TODO Auto-generated method stub
		TextView tv_name =new TextView(this);
		tv_name.setText(name);
		tv_name.setTextSize(16);
		tv_name.setWidth(110);
		tv_name.setPadding(5, 5, 5, 5);
		tv_name.setGravity(Gravity.LEFT);
		return tv_name;
	}
	private View createImageView() {
		// TODO Auto-generated method stub
		ImageView img_name =new ImageView(this);
		img_name.setBackgroundResource(R.drawable.pro_line);
		img_name.setMaxWidth(300);
		img_name.setPadding(5, 0, 0, 0);
		return img_name;
	}
	public JSONObject getHistory(String lat, String lon, String deviceId) {
		// TODO Auto-generated method stub
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id",deviceId));
        params.add(new BasicNameValuePair("lat",lat));
        params.add(new BasicNameValuePair("lon",lon));
        JSONObject json = getJSONFromUrl(apiCallURL, params);
        return json;
	}
	 static InputStream is = null;
	 static JSONObject json = null;
	 static String outPut = "";
	 public boolean isConnected(Context context) {
		    ConnectivityManager connMgr = (ConnectivityManager) 
		            context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		    if (networkInfo != null && networkInfo.isConnected()) {
		    	return true;

		    } else {
		        return false;
		    }
		}
		public JSONObject getJSONFromUrl(String url, List<NameValuePair> params) {
	        // Making the HTTP request
		        
		        try {
		        	if(!isConnected(History.this))
		        	{
		        		Toast.makeText(getApplicationContext(), "Check your Internet connection", 1000).show();
		        		msgLbl.setVisibility(View.VISIBLE);
		    			msgLbl.setText("Check your Internet connection");
		        		
//		        		ProductDet.postDelayed(new Runnable() {
//						    public void run() {
//						    	ProductDet.setText("Product Details:");
//						    }
//						}, 2000);
//						
//						Results.postDelayed(new Runnable() {
//						    public void run() {
//						    	Results.setText( "Check your Internet connection");
//						    }
//						}, 2000);
			        	return null;
			        }
		        	HttpClient httpClient = new DefaultHttpClient();
		            httpClient.getConnectionManager();
		            HttpPost httpPost = new HttpPost(url);
		            httpPost.setEntity(new UrlEncodedFormEntity(params));
		            HttpResponse httpResponse = httpClient.execute(httpPost);
		            HttpEntity httpEntity = httpResponse.getEntity();
		            is = httpEntity.getContent();
		        } catch (UnsupportedEncodingException e) {
		            e.printStackTrace();
		        } catch (ClientProtocolException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        try {
		            BufferedReader in = new BufferedReader(new InputStreamReader(
		                    is, "iso-8859-1"), 8);
		            StringBuilder sb = new StringBuilder();
		            String line = null;
		            while ((line = in.readLine()) != null) {
		                sb.append(line + "\n");
		            }
		            is.close();
		            outPut = sb.toString();
		            Log.e("JSON", outPut);
		        } catch (Exception e) {
		        	Log.e("JSON Parser", "Error parsing data " + e.toString());
//		            Toast.makeText(getApplicationContext(), "Error converting result " + e.toString(),500).show();
		            }
		        try {
		            json = new JSONObject(outPut);
		        } catch (JSONException e) {
//		        	Toast.makeText(getApplicationContext(), "Error converting result " + e.toString(),500).show();
		        	Log.e("JSON Parser", "Error parsing data " + e.toString());
		        }
		        // return JSON String
		        return json;
	 }
}
