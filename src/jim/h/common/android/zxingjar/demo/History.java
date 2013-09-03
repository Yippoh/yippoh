package jim.h.common.android.zxingjar.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class History extends Activity implements OnClickListener{
	
	private static String apiCallURL = "http://115.112.70.158/barcode_stats/history.php";
	ProgressDialog progDialog;
	private int progressBarStatus = 0;
	private LinearLayout linearErrorMsg;
	String lat,lon,DeviceId;
	private TableLayout history;
	private Button load_more;
	private TextView msgLbl;
	String lblLatitude,lblLongitude,lblStoreName;
	JSONArray histroyArray=null;
	
	int current_page = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		msgLbl=(TextView)findViewById(R.id.msgLbl);
		history=(TableLayout)findViewById(R.id.historyDetailsTbl);
		linearErrorMsg=(LinearLayout)findViewById(R.id.linearErrorMsg);
		history.setVisibility(View.GONE);
		getDeviceId();
 		getLocation();
 		ShowHistroy();
// 		load_more.setOnClickListener(this);
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
	        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, 800).show();    
	        lat=String.valueOf(latitude);
	        lon=String.valueOf(longitude);
	        	       
	    }else{
	        // can't get location
	        // GPS or Network is not enabled
	        // Ask user to enable GPS/network in settings
	        gps.showSettingsAlert();
	        Toast.makeText(getApplicationContext(), "Unable to get Location" , Toast.LENGTH_LONG).show();   
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
	}
	

	public void ShowHistroy()
	{
		String Page=String.valueOf("0");
	
		JSONObject json = getHistory(lat,lon,DeviceId,Page);
		if(json!=null)
		{
			try 
			{
				String message = json.getString("message");
				if(message.equalsIgnoreCase("No Data Found")==true)
				{
					 Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_LONG).show();
					 history.setVisibility(View.GONE);
					 linearErrorMsg.setVisibility(View.VISIBLE);
					 msgLbl.setVisibility(View.VISIBLE);
					 msgLbl.setText("No Data Found");
				}
				else
				{
					history.setVisibility(View.VISIBLE);
					linearErrorMsg.setVisibility(View.GONE);
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
				history.addView(createLoadMore());
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else
		{
			msgLbl.setVisibility(View.VISIBLE);
			msgLbl.setText("Check your Internet connection");
			Toast.makeText(getApplicationContext(), "No Values Retrieved...", Toast.LENGTH_LONG).show();
		}
		
	}
	
	public String postData(String lat,String lon,String DeviceId,String page) {
	    // Create a new HttpClient and Post Header
		
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://115.112.70.158/barcode_stats/history.php");
        String res=null;
	    try {
	        // Add your data
	    	
	    	List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("device_id",DeviceId));
	        params.add(new BasicNameValuePair("lat",lat));
	        params.add(new BasicNameValuePair("lon",lon));
	        params.add(new BasicNameValuePair("page",page));
	    	
//	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//	        nameValuePairs.add(new BasicNameValuePair("id", "12345"));
//	        nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
	        httppost.setEntity(new UrlEncodedFormEntity(params));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	        Toast.makeText(getApplicationContext(), "values:"+response, 500).show();
	        res=response.toString();
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	    return res;
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
	private View createLoadMore() {
		// TODO Auto-generated method stub
		load_more=new Button(this);
		load_more.setBackgroundResource(R.layout.green_btn);
		load_more.setWidth(100);
		load_more.setPadding(10, 10, 5, 20);
		load_more.setText("Load More ...");
		load_more.setTextSize(16);
		load_more.setTextColor(Color.WHITE);
		load_more.setGravity(Gravity.CENTER);
		load_more.setOnClickListener(this);
		return load_more;
	}
	
	 public void onClick(View arg0) {
		 new loadMore().execute();
	  }
	
	
	 public class loadMore extends AsyncTask<String,Integer,String>{ 
		 ProgressDialog dialog;
	        protected void onPreExecute(){
	            dialog = new ProgressDialog(History.this);            
	                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            dialog.setMax(100);
	            dialog.setTitle("Please Wait");
	            dialog.setMessage("Loading...");
	            dialog.show();              
	        }
	        @Override
	        protected String doInBackground(String... arg0) {
	                for(int i=0;i<6;i++){
	                publishProgress(5);
	                try {
	                    Thread.sleep(1200);// the timing set to a large value
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	            dialog.dismiss();
	            return null;
	        }
	    protected void  onProgressUpdate(Integer...progress){
	        dialog.incrementProgressBy(progress[0]);
	    }
	        protected void onPostExecute(String result){
	        	TableRow loadMoreRow =null;
//	        	Toast.makeText(getApplicationContext(), "load more", 500).show();
	        	String Page=String.valueOf(current_page);
	        	JSONObject json = getHistory(lat,lon,DeviceId,Page);
	    		if(json!=null)
	    		{
	    			try 
	        		{
	    			String message = json.getString("message");
					if(message.equalsIgnoreCase("No Data Found")==true)
					{
						load_more.setVisibility(View.GONE);
		        		history.addView(createLoadMoreMsg());
					}
					else
					{
		        	if(current_page<=3)
		        	{   		
//	        	Toast.makeText(getApplicationContext(), "page:"+Page, 500).show();
		        		
		        			load_more.setVisibility(View.GONE);
	
//	    				Toast.makeText(getApplicationContext(), "json:"+json, 500).show();
		        			JSONArray loadMoreHistoryArray= new JSONArray(json.getString("history"));
		        			if(loadMoreHistoryArray.length()>0)
		        			{
		        				int count =0;
		        				for (int i=0; i<loadMoreHistoryArray.length(); i++)
		        				{
		        					count=count+1;
		        					JSONObject c= loadMoreHistoryArray.getJSONObject(i);
		        					loadMoreRow= new TableRow(History.this);
		        					loadMoreRow.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
							     // Add name
		        					loadMoreRow.addView(createTextView(c.getString("product_name")));
		        					loadMoreRow.addView(createTextView(c.getString("latitude")));
		        					loadMoreRow.addView(createTextView(c.getString("longitude")));
								
		        					if(count%2!=0){
									 loadMoreRow.setBackgroundColor(Color.parseColor("#F2F2F2"));
		        					}
		        					else
		        					{
							    	loadMoreRow.setBackgroundColor(Color.parseColor("#D2D2D2"));
		        					}
		        					history.addView(loadMoreRow);
		        					history.addView(createImageView());
								
		        				}
//							load_more.setVisibility(View.VISIBLE);
		        			}
		        		
	    			history.addView(createLoadMore());
	    			current_page += 1;
		        	}
		        	else
		        	{
		        		load_more.setVisibility(View.GONE);
		        		history.addView(createLoadMoreMsg());
//	    			Toast.makeText(getApplicationContext(), "No Values Returned", 500).show();
		        	}
					}
	        		} 
	        		catch (JSONException e) {
    				e.printStackTrace();
	        		}
	    		
	    		}
	        	else
	        	{
	    			Toast.makeText(getApplicationContext(), "No Values Returned", 500).show();
	        		load_more.setVisibility(View.GONE);
	        		history.addView(createLoadMoreMsg());
	        	}
	        }
	 }
	 public View createLoadMoreMsg() {
			// TODO Auto-generated method stub
			TextView msg =new TextView(this);
			msg.setText("No more data found ...");
			msg.setTextSize(16);
			msg.setWidth(200);
			msg.setPadding(5, 5, 5, 5);
			msg.setGravity(Gravity.CENTER);
			return msg;
		}
	
	private View createImageView() {
		// TODO Auto-generated method stub
		ImageView img_name =new ImageView(this);
		img_name.setBackgroundResource(R.drawable.pro_line);
		img_name.setMaxWidth(300);
		img_name.setPadding(5, 0, 0, 0);
		return img_name;
	}
//	public JSONObject getLoadMoreHistory(String lat, String lon, String deviceId,String page) {
//		// TODO Auto-generated method stub
//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("device_id",deviceId));
//        params.add(new BasicNameValuePair("lat",lat));
//        params.add(new BasicNameValuePair("lon",lon));
//        params.add(new BasicNameValuePair("page",page));
//        JSONObject json = getJSONFromUrl(apiCallURL, params);
//        return json;
//	}
	
	public JSONObject getHistory(String lat, String lon, String deviceId,String page) {
		// TODO Auto-generated method stub
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id",deviceId));
        params.add(new BasicNameValuePair("lat",lat));
        params.add(new BasicNameValuePair("lon",lon));
        params.add(new BasicNameValuePair("page",page));
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
		        	
		        	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 9) {
		    	        try {
		    	            // StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
		    	            Class<?> strictModeClass = Class.forName("android.os.StrictMode", true, Thread.currentThread()
		    	                    .getContextClassLoader());
		    	            Class<?> threadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy", true, Thread.currentThread()
		    	                    .getContextClassLoader());
		    	            Field laxField = threadPolicyClass.getField("LAX");
		    	            Method setThreadPolicyMethod = strictModeClass.getMethod("setThreadPolicy", threadPolicyClass);
		    	            setThreadPolicyMethod.invoke(strictModeClass, laxField.get(null));
		    	        } catch (Exception e) {
		    	        }
		    	    }
		        	
		        	if(!isConnected(History.this))
		        	{
		        		linearErrorMsg.setVisibility(View.VISIBLE);
		        		msgLbl.setVisibility(View.VISIBLE);
		    			msgLbl.setText("Check your Internet connection");
			        	return null;
			        }
		        	 HttpParams httpParameters = new BasicHttpParams();
			         HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
			         HttpConnectionParams.setSoTimeout(httpParameters, 10000);
			         HttpClient httpClient = new DefaultHttpClient(httpParameters);
		             httpClient.getConnectionManager();
		             HttpPost httpPost = new HttpPost(url);
		             httpPost.setEntity(new UrlEncodedFormEntity(params));
		             HttpResponse httpResponse = httpClient.execute(httpPost);
		             HttpEntity httpEntity = httpResponse.getEntity();
		             is = httpEntity.getContent();
		        }
		        catch (ConnectTimeoutException e) {
		        	linearErrorMsg.setVisibility(View.VISIBLE);
	        		msgLbl.setVisibility(View.VISIBLE);
	    			msgLbl.setText("Connection Timeout");
		        }
		        catch (UnsupportedEncodingException e) {
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
