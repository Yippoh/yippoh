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
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Rewards extends Activity implements OnClickListener{
	private static String rewardsURL = "http://115.112.70.158/barcode_stats/reward.php";
	private static String emailURL = "http://115.112.70.158/barcode_stats/send_mail.php";
	private static String userURL = "http://115.112.70.158/barcode_stats/user.php";
	String lat,lon,DeviceId;
	private TextView msgLbl,LblRewards;
	private Button redeem;
	String lblLatitude,lblLongitude,lblStoreName;
	Integer screenHeight,screenWidth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rewards);
		msgLbl=(TextView)findViewById(R.id.msgLbl);
		LblRewards=(TextView)findViewById(R.id.LblRewards);
		redeem=(Button)findViewById(R.id.redeem_btn);
		getDeviceId();
		getLocation();
		ShowRewards();
		getScreenDimensions();
		redeem.setOnClickListener(this);
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
	     Toast.makeText(getApplicationContext(), "DeviceId:" +DeviceId, 500).show();   
	}
	
	public void onClick(View v){
		
		switch(v.getId()) {
		case R.id.redeem_btn:
			DatabaseHandler db = new DatabaseHandler(this);
			int checkexists = db.CheckExists(DeviceId);
	    	if(checkexists>0)
	    	{
	    		Contact exists =db.getContact(DeviceId);
	    		if(exists._email_id==null)
    			{
    				Toast.makeText(getApplicationContext(), "Please Provide your Email",5000).show();
    				  LayoutInflater layoutInflater 
				         = (LayoutInflater)getBaseContext()
				             .getSystemService(LAYOUT_INFLATER_SERVICE);  
				      View popupView = layoutInflater.inflate(R.layout.emailpopup, null);  
    				  final PopupWindow popup = new PopupWindow(Rewards.this);
    				  int width= screenWidth - 50;
					  int height= screenHeight - 250;
					  popup.setContentView(popupView);
					  popup.setWidth(width);
					  popup.setHeight(height);
					  popup.setFocusable(true);
					  popup.showAtLocation(popupView, Gravity.CENTER, 0, 20);
					  final EditText txtEmail=(EditText)popupView.findViewById(R.id.txtEmail);
					  Button close = (Button) popupView.findViewById(R.id.Cancel_btn);
					   close.setOnClickListener(new OnClickListener() {
					 
					     @Override
					     public void onClick(View v) {
					       popup.dismiss();
					     }
					   });
					  Button submit = (Button) popupView.findViewById(R.id.Submit);
					  submit.setOnClickListener(new OnClickListener() {
					 
					     @Override
					     public void onClick(View v) {
					    	 boolean didEmail=false;
					    	 String Email=txtEmail.getText().toString();
								if(Email.length() == 0)
								{
//									txtEmail.setError(Html.fromHtml("<font color='red'>Email Id is not valid!</font>"));
									didEmail=false;
								}
								else
								{
									didEmail=true;
								}
								
								if((didEmail))
								{
									JSONObject json = getUsers(Email,DeviceId);
									if(json != null)
									{
										try {
											String message = json.getString("message");
//											Toast.makeText(getApplicationContext(), message, 500).show();
											
											if((message.equalsIgnoreCase("success")==true) || (message.equalsIgnoreCase("Data already exist")==true))
											{
												DatabaseHandler db = new DatabaseHandler(Rewards.this);
												int checkexists = db.CheckExists(DeviceId);
										    	if(checkexists>0)
										    	{
												    Contact exists = db.getContact(DeviceId);
													int count=0;
													count=exists._count+1;
										    		if((exists._device_id.equalsIgnoreCase(DeviceId)== true))
										    		{
										    			db.updateContact(new Contact(DeviceId,Email,count));
										    			JSONObject jsonEmail = sendEmail(DeviceId,Email);
									    				if(jsonEmail!=null)
									    				{
									    					try 
									    					{
									    						String Emailmessage = jsonEmail.getString("message");
									    						Toast.makeText(getApplicationContext(), "message:"+Emailmessage, 500).show();
									    					}
									    					catch (JSONException e) {
									    						e.printStackTrace();
									    					}
									    				}
										    		}
										    	}

											}
										} 
										catch (JSONException e) {
												e.printStackTrace();
										 }
										 popup.dismiss();
								}
								else
								{
									
								}
					    	
								}
								else
								{
									Toast.makeText(getApplicationContext(), "Please Provide Email", 500).show();
								}
					     }
					     });
					     
    			}
    			else
    			{
    				String Email=exists._email_id;
    				JSONObject jsonEmail = sendEmail(DeviceId,Email);
    				if(jsonEmail!=null)
    				{
    					try 
    					{
    						String message = jsonEmail.getString("message");
    						Toast.makeText(getApplicationContext(), "message:"+message, 500).show();
    						 msgLbl.setVisibility(View.VISIBLE);
    	    				 msgLbl.setText("Mail Send");
    					}
    					catch (JSONException e) {
    						e.printStackTrace();
    					}
    				}
	    			else
	    			{
	    				Toast.makeText(getApplicationContext(), "No Values Retrieved...", 1000).show();
	    				 msgLbl.setVisibility(View.VISIBLE);
	    				 msgLbl.setText("Check your Internet connection");
	    			}
    			}
	    	}
			break;
		}
    }
	public void ShowRewards()
	{
		JSONObject json = getHistory(lat,lon,DeviceId);
		if(json!=null)
		{
//			Toast.makeText(getApplicationContext(), "json:"+json, 1000).show();
			try 
			{
				String message = json.getString("message");
				if(message.equalsIgnoreCase("No Data Found")==true)
				{
					Toast.makeText(getApplicationContext(), "No Data Found", 500).show();
					 msgLbl.setVisibility(View.VISIBLE);
					 msgLbl.setText("No Data Found");
				}
				else
				{
					
					msgLbl.setVisibility(View.GONE);
					if(json.getString("reward")!="null")
					{
						LblRewards.setVisibility(View.VISIBLE);
						LblRewards.setText(json.getString("reward"));
					}
					else
					{
						 
						 msgLbl.setVisibility(View.VISIBLE);
						 msgLbl.setText("No Data Found");
					}
					
				}
			  
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "No Values Retrieved...", 1000).show();
			 msgLbl.setVisibility(View.VISIBLE);
			 msgLbl.setText("Check your Internet connection");
		}
		
	}
	public JSONObject getUsers(String Email,String deviceId){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("email_id", Email));
        params.add(new BasicNameValuePair("device_id",deviceId));
        JSONObject json = getJSONFromUrl(userURL, params);
        return json;
    }
	public JSONObject sendEmail(String deviceId,String Email) {
		// TODO Auto-generated method stub
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("deviceid",deviceId));
		params.add(new BasicNameValuePair("emailid",Email));
        JSONObject jsonEmail = getJSONFromUrl(emailURL, params);
        return jsonEmail;
	}
	
	public JSONObject getHistory(String lat, String lon, String deviceId) {
		// TODO Auto-generated method stub
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id",deviceId));
        JSONObject json = getJSONFromUrl(rewardsURL, params);
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
		        	if(!isConnected(Rewards.this))
		        	{
		        		Toast.makeText(getApplicationContext(), "Check your Internet connection", 1000).show();
//						ProductDet.postDelayed(new Runnable() {
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
		            Toast.makeText(getApplicationContext(), "Error converting result " + e.toString(),500).show();
		            }
		        try {
		            json = new JSONObject(outPut);
		        } catch (JSONException e) {
		        	Toast.makeText(getApplicationContext(), "Error converting result " + e.toString(),500).show();
//		            Log.e("JSON Parser", "Error parsing data " + e.toString());
		        }
		        // return JSON String
		        return json;
	 }
	 
	public void getScreenDimensions()
	{
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
//		Toast.makeText(getApplicationContext(), "width:"+screenWidth+"height:"+screenHeight, 500).show();
	}
}
