package jim.h.common.android.zxingjar.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class getDetails extends Activity implements OnClickListener{
	private JSONParser jsonParser;
	private static String apiCallURL = "http://115.112.70.158/barcode_stats/ajax.php";
	private static String jsonResult = "success";
	private TextView formatTxt, contentTxt,ProductDet,Results,AddressTxt,LocPriceTxt,LocationDetLbl;
	private EditText PriceTxt;
	private Button getDetBtn;
	ProgressDialog progDialog;
	private int progressBarStatus = 0;
	private Handler progressBarHandler = new Handler();
	private Spinner priceType,quantity;
	boolean didItWork=false;
	String Price,Barcode,lat,lon,deviceId,Type,Quant;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_details);
		
		jsonParser = new JSONParser();
		contentTxt = (TextView)findViewById(R.id.scan_content);
		ProductDet=(TextView)findViewById(R.id.ProductDet);
		AddressTxt=(TextView)findViewById(R.id.AddressTxt);
		LocPriceTxt=(TextView)findViewById(R.id.LocPriceTxt);
		LocationDetLbl=(TextView)findViewById(R.id.LocationDetLbl);
		Results=(TextView)findViewById(R.id.Results);
		PriceTxt =(EditText)findViewById(R.id.txtPrice); 
		getDetBtn =(Button)findViewById(R.id.get_details_btn);
		priceType=(Spinner)findViewById(R.id.priceTypeText);
		quantity=(Spinner)findViewById(R.id.productquaTxt);
		receiveIntentValues();
		getDetBtn.setOnClickListener(this);
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
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(PriceTxt.getWindowToken(), 0);
			Results.setText("");
			Price = PriceTxt.getText().toString();
			Barcode = contentTxt.getText().toString();
			Type=String.valueOf(priceType.getSelectedItem());	
			Quant=String.valueOf(quantity.getSelectedItem());
			if(Price.length() == 0)
			{
				PriceTxt.setError(Html.fromHtml("<font color='red'>Price is required!</font>"));
//				setColorText();   
				didItWork=false;
			}
			else
			{
				didItWork=true;
			}
			if((didItWork))
			{
				ShowProgressBar();
				getDetailsDelay();
				Toast.makeText(getApplicationContext(), "values passed to server:"+ Price +","+ Barcode+","+Type+","+Quant+","+deviceId, 300).show();
				JSONObject json = getProductDetails(Price,Barcode,lat,lon,deviceId,Type,Quant);
//				Toast.makeText(getApplicationContext(), "json:"+json, 500).show();
				if(json!=null)
				{
					HideProgressBar();
				  try 
				  {
						String message = json.getString("message");
						ProductDet.setText("");
						Results.setText("");
//						Toast.makeText(getApplicationContext(), "message:"+message, 500).show();
						if(message.equalsIgnoreCase("Barcode Not Exist")==true)
						{
							Toast.makeText(getApplicationContext(), "No Data Found ...", 1000).show();
							ProductDet.postDelayed(new Runnable() {
							    public void run() {
							    	ProductDet.setText("Product Details:");
							    }
							}, 2000);
							
							Results.postDelayed(new Runnable() {
							    public void run() {
							    	Results.setText("We do not have any price data for that item within 50 miles");
							    }
							}, 2000);
//							ProductDet.setText("Product Details:");
//							Results.setText("We do not have any price data for that item within 50 miles");
							return;
						}
						else
						{
							String ProductId="",ProductName="",ProductPrice="",ProductManu="",ProductBarCode="",locationValue="",locationAddress="",locationPrice="";	
							ProductId=json.getString("id");
							ProductName=json.getString("name");
							ProductPrice=json.getString("price");
							ProductManu=json.getString("manufacture");
							ProductBarCode= json.getString("barcode");
							Toast.makeText(getApplicationContext(), "ProductId:"+ProductId, 1000).show();
							if(ProductId!="null")
							{
//								ProductDet.setText("Product Details:");
//								Results.setText("Product Id:"+ProductId+",\n"+"ProductName:"+ProductName+",\n"+"Manufacturer:"+ProductManu+",\n"+"BarCode:"+ProductBarCode+",\n"+"Price:"+ProductPrice);
								if(json.getString("other_locations")!="null")
								{
									
								JSONArray productArray = new JSONArray(json.getString("other_locations"));
								if(productArray.length()>0)
								{
									for (int i=0; i<productArray.length(); i++)
								        {
											JSONObject c= productArray.getJSONObject(i);
//											Toast.makeText(getApplicationContext(),"Address:"+c.getString("address"),400).show();
											
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
//									        	Toast.makeText(getApplicationContext(), "We do not have any price data for that item within 50 miles", 2000).show();
//												ProductDet.postDelayed(new Runnable() {
//												    public void run() {
//												    	ProductDet.setText("Product Details:");
//												    }
//												}, 2000);
//												
//												Results.postDelayed(new Runnable() {
//												    public void run() {
//												    	Results.setText("We do not have any price data for that item within 50 miles");
//												    }
//												}, 2000);
//												locationValue+="No Data Found";
									        	
									        }
				
										}
								}
								
								
								}
								else
						        {
									locationValue="null";
									locationAddress="null";
						        	locationPrice="null";
//									Toast.makeText(getApplicationContext(), "We do not have any price data for that item within 50 miles", 2000).show();
//									ProductDet.postDelayed(new Runnable() {
//									    public void run() {
//									    	ProductDet.setText("Product Details:");
//									    }
//									}, 2000);
//									
//									Results.postDelayed(new Runnable() {
//									    public void run() {
//									    	Results.setText("We do not have any price data for that item within 50 miles");
//									    }
//									}, 2000);
//									locationValue+="No Data Found";
						        }							
//								LocationDetLbl.setText("Location Details:");
								AddressTxt.setText(locationAddress);
								LocPriceTxt.setText(locationPrice);
								Intent showDetails=new Intent(this,showDetails.class);
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
			else
			{
				 Toast.makeText(getApplicationContext(), "Enter the Price", 300).show();
			}
		   break;
		}
	}

//	public void setColorText() {
//		// TODO Auto-generated method stub
//		PriceTxt.setTextColor(Color.RED);
//	}

	public void ShowProgressBar()
	{
		 progDialog = new ProgressDialog(this);
         progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         progDialog.setMessage("Loading...");
         progDialog.setProgress(0);
         progDialog.setMax(100);
         progDialog.show();
         progressBarStatus = 0;
	}
	
	public void getDetailsDelay()
	{
		new Thread(new Runnable() {
			  public void run() {
					  // your computer is too fast, sleep 5 second
					  try {
						Thread.sleep(5000);
		
					  } catch (InterruptedException e) {
						e.printStackTrace();
					  }
					  }
			       }).start();
	}
	
	public void HideProgressBar()
	{
		 new Thread(new Runnable() {
			  public void run() {
						// sleep 5 seconds, so that you can see the 100%
						try {
							Thread.sleep(100);
							// close the progress bar dialog
							progDialog.dismiss();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						}
			  }).start();
	}
	
	public JSONObject getProductDetails(String Price,String Barcode,String lat,String lon,String deviceId,String Type,String Quant){
		 
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("price", Price));
        params.add(new BasicNameValuePair("barcode", Barcode));
        params.add(new BasicNameValuePair("lat",lat));
        params.add(new BasicNameValuePair("lon",lon));
        params.add(new BasicNameValuePair("device_id",deviceId));
        params.add(new BasicNameValuePair("type",Type));
        params.add(new BasicNameValuePair("quant",Quant));
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
	        		HideProgressBar();
		        	Toast.makeText(getApplicationContext(), "Check your internet connection", 1000).show();
//		        	ProductDet.setText("Product Details:");
//					Results.setText("Check your Internet connection");
					ProductDet.postDelayed(new Runnable() {
					    public void run() {
					    	ProductDet.setText("Product Details:");
					    }
					}, 2000);
					
					Results.postDelayed(new Runnable() {
					    public void run() {
					    	Results.setText("Check your internet connection");
					    }
					}, 2000);
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