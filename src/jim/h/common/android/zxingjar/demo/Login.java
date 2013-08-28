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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Login	extends Activity implements OnClickListener{
	private JSONParser jsonParser;
	private static String UserURL = "http://115.112.70.158/barcode_stats/user.php";
	private TextView lblMsg;
	private EditText txtEmail;
	private Button Submit;
	String Email,DeviceId,lat,lon;
	Integer screenHeight,screenWidth;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		jsonParser = new JSONParser();
		getDeviceId();
		getLocation();
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);	
		setContentView(R.layout.login);
		getScreenDimensions();
		if(screenHeight >480)
		  {
			  getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_title);	
		  }
		  else
		  {
			  getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.small_my_title);	
		  }
		  findViewById(R.id.logo_btn).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,	KeyEvent.KEYCODE_BACK));
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				}
			});
		txtEmail=(EditText)findViewById(R.id.txtEmail); 
		lblMsg=(TextView)findViewById(R.id.lblMsg); 
		Submit=(Button)findViewById(R.id.Submit); 
		Submit.setOnClickListener(this);
	}
	public void getScreenDimensions()
	 {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
//		Toast.makeText(getApplicationContext(), "width:"+screenWidth+",height:"+screenHeight, 500).show();
	 }
	public void getDeviceId()
    {
    	
         TelephonyManager telephonyManager  =  (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    		    
    	 /*
    	* getDeviceId() function Returns the unique device ID.
    	* for example,the IMEI for GSM and the MEID or ESN for CDMA phones.  
    	*/				    
         DeviceId = telephonyManager.getDeviceId();
         Toast.makeText(getApplicationContext(), "DeviceId:" +DeviceId, 500).show();  
                
    }
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
//	        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lat + "\nLong: " + lon , 500).show();   
	        
	       
	    }else{
	        // can't get location
	        // GPS or Network is not enabled
	        // Ask user to enable GPS/network in settings
	        gps.showSettingsAlert();
	        Toast.makeText(getApplicationContext(), "Unable to get Location" , 500).show();   
	    }
	    
	}
	public void onClick(View v){
		switch(v.getId()) {
		case R.id.Submit:
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(txtEmail.getWindowToken(), 0);
			boolean didItWork=false;
			Email = txtEmail.getText().toString();
			String regex="^(.+)@([^@]+[^.]).([^.])$";
			if (txtEmail.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+") && txtEmail.getText().length() > 0)
            {
				didItWork=true;
			}
			else
			{
				txtEmail.setError(Html.fromHtml("<font color='red'>Email Id is not valid!</font>"));
				didItWork=false;
			}
			if((didItWork))
			{
				JSONObject json = getUsers(Email,DeviceId);
				if(json != null)
				{
					try {
						String message = json.getString("message");
						Toast.makeText(getApplicationContext(), message, 500).show();
						
						if((message.equalsIgnoreCase("success")==true) || (message.equalsIgnoreCase("Data already exist")==true))
						{
							DatabaseHandler db = new DatabaseHandler(this);
//					    	db.deleteContact(new Contact(deviceId, 1));
					    	int count=0;
					    	int checkexists = db.CheckExists(DeviceId);
					    	if(checkexists>0)
					    	{
					    		Contact exists = db.getContact(DeviceId);
					    		count=exists._count+1;
					    		if((exists._count == 10) && (exists._device_id.equalsIgnoreCase(DeviceId)== true))
					    		{
					    			db.updateContact(new Contact(DeviceId,Email,count));
					    		}
					    	}
					    	 
					    	List<Contact> contacts = db.getAllContacts();       
					   	 	String log =null;
					       for (Contact cn : contacts) {
					       	log= "\nId: "+cn.getID()+" ,\nName: " + cn.getDeviceID() + " ,\nEmail:"+cn.getEmailID()+"\nCount: " + cn.getCount();
//					       	Toast.makeText(getApplicationContext(), "details saved:" +log, 1000).show();
					       }
//							Intent getDet = new Intent(this,getDetails.class);
//							getDet.putExtra("barcode", "W1LS2M");
//							getDet.putExtra("format", "QR_CODE");
//							getDet.putExtra("lat", lat);
//							getDet.putExtra("lon", lon);
//							getDet.putExtra("device_id", DeviceId);
//							startActivity(getDet);
//							Intent scan = new Intent(this,Scan.class);
//							startActivity(scan);
					       	Intent tab = new Intent(this,TabHostActivity.class);
						    startActivity(tab);
						}
						else 
						{
//							Intent getDet = new Intent(this,getDetails.class);
//							getDet.putExtra("barcode", "W1LS2M");
//							getDet.putExtra("format", "QR_CODE");
//							getDet.putExtra("lat", lat);
//							getDet.putExtra("lon", lon);
//							getDet.putExtra("device_id", DeviceId);
//							startActivity(getDet);
//							Intent scan = new Intent(this,Scan.class);
//							startActivity(scan);
							Intent tab = new Intent(this,TabHostActivity.class);
						    startActivity(tab);
						}
					 } 
					catch (JSONException e) {
							e.printStackTrace();
					 }
				}
				else
				{
					Toast.makeText(getApplicationContext(), "No values are returned from server ...", 500).show();
				}
			}
			else
			{
				 Toast.makeText(getApplicationContext(), "Clear the error", 300).show();
			}
			break;
		}
	}
	
	public JSONObject getUsers(String Email,String deviceId){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("email_id", Email));
        params.add(new BasicNameValuePair("device_id",deviceId));
        JSONObject json = getJSONFromUrl(UserURL, params);
        return json;
    }
	
	static InputStream is = null;
	 static JSONObject json = null;
	 static String outPut = "";
		public JSONObject getJSONFromUrl(String url, List<NameValuePair> params) {
	        // Making the HTTP request
		        
		        try {
		        	lblMsg.setText("");
		        	if(!isConnected(Login.this)){
		        		Toast.makeText(getApplicationContext(), "Check your internet connection", 1000).show();
		        		lblMsg.setText("Check your internet connection");
			        	return null;
			        }
		            DefaultHttpClient httpClient = new DefaultHttpClient();
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
		            Log.e("Buffer Error", "Error converting result " + e.toString());
		        }
		        try {
		            json = new JSONObject(outPut);
		        } catch (JSONException e) {
		            Log.e("JSON Parser", "Error parsing data " + e.toString());
		        }
		        // return JSON String
		        return json;
	 }
	    
		
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
		
}
