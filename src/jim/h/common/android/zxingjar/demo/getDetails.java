package jim.h.common.android.zxingjar.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import jim.h.common.android.zxingjar.demo.getStoreDetails.LoadStore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class getDetails extends Activity implements OnClickListener{
	private JSONParser jsonParser;
	private static String apiCallURL = "http://115.112.70.158/barcode_stats/getdetails.php";
	private static String jsonResult = "success";
	private TextView formatTxt, contentTxt,ProductDet,Results,AddressTxt,LocPriceTxt,LocationDetLbl;
	private LinearLayout linearErrorMsg;
	private EditText PriceTxt,StoreName;
	private Button getDetBtn;
	ProgressDialog progDialog;
	private int progressBarStatus = 0;
	private ScrollView mScrollView;
	private Handler progressBarHandler = new Handler();
	private Spinner priceType,quantity;
	boolean didItWork=false;
	String Price,Barcode,lat,lon,deviceId,Type,Quant,StoreValue;
	Integer screenHeight,screenWidth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.get_details);
		getScreenDimensions();
		 if (screenHeight >480)
		  {
			  getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_title);	
		  }
		  else
		  {
			  getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.small_my_title);	
		  }
		
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_title);	
		findViewById(R.id.logo_btn).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,	KeyEvent.KEYCODE_BACK));
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				}
			});
		jsonParser = new JSONParser();
		contentTxt = (TextView)findViewById(R.id.scan_content);
		ProductDet=(TextView)findViewById(R.id.ProductDet);
		AddressTxt=(TextView)findViewById(R.id.AddressTxt);
		LocPriceTxt=(TextView)findViewById(R.id.LocPriceTxt);
		LocationDetLbl=(TextView)findViewById(R.id.LocationDetLbl);
		Results=(TextView)findViewById(R.id.Results);
		StoreName=(EditText)findViewById(R.id.StoreName);
		PriceTxt =(EditText)findViewById(R.id.txtPrice); 
		getDetBtn =(Button)findViewById(R.id.get_details_btn);
		priceType=(Spinner)findViewById(R.id.priceTypeText);
		quantity=(Spinner)findViewById(R.id.productquaTxt);
		linearErrorMsg=(LinearLayout)findViewById(R.id.linearErrorMsg);
		mScrollView=(ScrollView)findViewById(R.id.scroll_view);
		linearErrorMsg.setVisibility(View.GONE);
		receiveIntentValues();
		getDetBtn.setOnClickListener(this);
		}
		
	 public void getScreenDimensions()
	 {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
//		Toast.makeText(getApplicationContext(), "width:"+screenWidth+",height:"+screenHeight, 500).show();
	 }
	public void receiveIntentValues() {
		// get Intent values
		Intent getDet=getIntent();
        String barcode=getDet.getStringExtra("BarCode");
        if(barcode==null)
        {
        	Toast.makeText(getApplicationContext(), "No scan data received !", 800).show();    
        	Intent Scan = new Intent(this,Scan.class);
			startActivity(Scan);
        }
        lat=getDet.getStringExtra("lat");
        lon=getDet.getStringExtra("lon");
        deviceId=getDet.getStringExtra("device_id");
//        formatTxt.setText(format);
        contentTxt.setText(barcode);    
	}
	public void onClick(View v){
		
		switch(v.getId()) {
		
		case R.id.get_details_btn:
			linearErrorMsg.setVisibility(View.GONE);
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(PriceTxt.getWindowToken(), 0);
			InputMethodManager imm1 = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(StoreName.getWindowToken(), 0);
			Results.setText("");
			StoreValue=StoreName.getText().toString();
			Price = PriceTxt.getText().toString();
			Barcode = contentTxt.getText().toString();
			Type=String.valueOf(priceType.getSelectedItem());	
			Quant=String.valueOf(quantity.getSelectedItem());
			if(Price.length() == 0)
			{
				PriceTxt.setError(Html.fromHtml("<font color='red'>Price is required!</font>"));
				didItWork=false;
			}
			else
			{
				didItWork=true;
			}
			
			if(StoreValue.length()==0)
			{
				StoreName.setError(Html.fromHtml("<font color='red'>Store Name required!</font>"));
				didItWork=false;
			}
			else
			{
				didItWork=true;
			}
			
			if((didItWork))
			{
//				Toast.makeText(getApplicationContext(), "values passed to server:"+ Price +","+ Barcode+","+Type+","+Quant+","+deviceId+","+StoreValue, 1000).show();
				new LoadProducts().execute(" ");				
			}
			else
			{
				Results.setText("Enter the Price & Store Name");
				linearErrorMsg.setVisibility(View.VISIBLE);
				mScrollView.post(new Runnable() { 
			        public void run() { 
			             mScrollView.scrollTo(0, mScrollView.getBottom());
			        } 
			    });
			}
		   break;
		}
	}

//	public void setColorText() {
//		// TODO Auto-generated method stub
//		PriceTxt.setTextColor(Color.RED);
//	}

	
	 public class LoadProducts extends AsyncTask<String,Integer,String>{
	        ProgressDialog dialog;
	        protected void onPreExecute(){
	            dialog = new ProgressDialog(getDetails.this);            
	                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            dialog.setMax(100);
	            dialog.setMessage("Loading...");
	            dialog.show();              
	        }
	        @Override
	        protected String doInBackground(String... arg0) {
	                for(int i=0;i<6;i++){
	                publishProgress(5);
	                try {
	                    Thread.sleep(500);// the timing set to a large value
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
	        	JSONObject jsonPro = getProductDetails(Price,Barcode,lat,lon,deviceId,Type,Quant,StoreValue);
	        	if(jsonPro!=null)
				{
				try 
				{
				String message = jsonPro.getString("message");
				if(message.equalsIgnoreCase("Barcode Not Exist")==true)
				{
					  	ProductDet.setText("Product Details:");
				    	Results.setText("We do not have any price data for that item within 50 miles");
 					    linearErrorMsg.setVisibility(View.VISIBLE);
 						return;
				}
				else
				{
				String ProductId="",ProductName="",ProductPrice="",ProductManu="",ProductBarCode="",locationValue="",locationAddress="",locationPrice="";	
				ProductId=jsonPro.getString("id");
				ProductName=jsonPro.getString("name");
				ProductPrice=jsonPro.getString("price");
				ProductManu=jsonPro.getString("manufacture");
				ProductBarCode= jsonPro.getString("barcode");
				
				if(ProductId!="null")
				{
					if(jsonPro.getString("other_locations")!="null")
				{
					
				JSONArray productArray = new JSONArray(jsonPro.getString("other_locations"));
				if(productArray.length()>0)
				{
					for (int i=0; i<productArray.length(); i++)
				        {
							JSONObject c= productArray.getJSONObject(i);
//							Toast.makeText(getApplicationContext(), "address:"+ c.getString("address"), 500).show();
							 if(c.getString("address")!="null" && c.getString("price")!="null")
					        {
					        	locationAddress+= c.getString("address")+"%";
					        	locationPrice+=c.getString("price")+"%";
					        	locationValue+="Address:"+c.getString("address")+",\nPrice:"+c.getString("price")+"\n\n";
					        }
					        else
					        {
					        	locationValue="";
					        	locationAddress="";
					        	locationPrice="";
							    }
									
							}
					}
					
					
					}
					else
					{
						locationValue="null";
						locationAddress="null";
						locationPrice="null";
					}
					AddressTxt.setText(locationAddress);
					LocPriceTxt.setText(locationPrice);
					final Intent showDetails=new Intent(getDetails.this,showDetails.class);
					showDetails.putExtra("ProductId",ProductId);
					showDetails.putExtra("ProductName", ProductName);
					showDetails.putExtra("Manufac", ProductManu);
					showDetails.putExtra("BarCode", ProductBarCode);
					showDetails.putExtra("ProductPrice", ProductPrice);
					showDetails.putExtra("locationValue", locationValue);
					showDetails.putExtra("Address", locationAddress);
					showDetails.putExtra("Price", locationPrice);
					startActivity(showDetails);
					
				}
				}
				}
				catch (JSONException e) {
				e.printStackTrace();
				}
				}
				
	        }
	    }

	public JSONObject getProductDetails(String Price,String Barcode,String lat,String lon,String deviceId,String Type,String Quant,String StoreValue){
		 
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("price", Price));
        params.add(new BasicNameValuePair("barcode", Barcode));
        params.add(new BasicNameValuePair("lat",lat));
        params.add(new BasicNameValuePair("lon",lon));
        params.add(new BasicNameValuePair("device_id",deviceId));
        params.add(new BasicNameValuePair("type",Type));
        params.add(new BasicNameValuePair("quant",Quant));
        params.add(new BasicNameValuePair("storename",StoreValue));
        JSONObject json = getJSONFromUrl(apiCallURL, params);
        return json;
    }
 static InputStream is = null;
 static JSONObject json = null;
 static String outPut = "";
	public JSONObject getJSONFromUrl(String url, List<NameValuePair> params) {
        // Making the HTTP request
	        
	        try {
	        	ProductDet.setText("");
	        	Results.setText("");
	        	if(!isConnected(getDetails.this)){
	        		ProductDet.setText("Product Details:");
					Results.setText("Check your internet connection");
					linearErrorMsg.setVisibility(View.VISIBLE);
					mScrollView.post(new Runnable() { 
				        public void run() { 
				             mScrollView.scrollTo(0, mScrollView.getBottom());
				        } 
				    });
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