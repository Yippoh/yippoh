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

import jim.h.common.android.zxinglib.integrator.IntentIntegrator;
import jim.h.common.android.zxinglib.integrator.IntentResult;

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
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class getStoreDetails extends Activity implements OnClickListener{
	private Handler handler = new Handler();
	private JSONParser jsonParser;
	private static String storeURL = "http://115.112.70.158/barcode_stats/getdetails.php";
	private static String productURL = "http://115.112.70.158/barcode_stats/show.php";
	private static String jsonResult = "success";
	private TextView formatTxt, contentTxt,ProductDet,Results,AddressTxt,LocPriceTxt,LocationDetLbl,TxtStoreDetails,StoreName;
	private EditText PriceTxt;
	private Button getDetBtn,scanButton,homeButton;
	ProgressDialog progDialog;
	private int progressBarStatus = 0;
	private Handler progressBarHandler = new Handler();
	private Spinner priceType,quantity;
	boolean didItWork=false;
	boolean didStore=false;
	boolean didStoreWork=false;
	boolean storeTextValue=false;
	String Price,Barcode,lat,lon,deviceId,Type,Quant,storeValue,resultid,Storeid,StreBtnId,Locid,LocBtnId,LocValue,StatId;
	RadioGroup rdStrGroup=null;
	RadioButton rdStrBtn=null;
	RadioGroup rdLocGroup=null;
	RadioButton rdLocBtn=null;
	String StoreDet =null;
	Integer screenWidth,screenHeight;
	JSONArray storeArray=null,locArray=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.get_details);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_title);	
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
//		TxtStoreDetails=(TextView)findViewById(R.id.TxtStoreDetails);
		PriceTxt =(EditText)findViewById(R.id.txtPrice); 
		getDetBtn =(Button)findViewById(R.id.get_details_btn);
		scanButton=(Button)findViewById(R.id.scan_button);
		homeButton=(Button)findViewById(R.id.home_button);
		priceType=(Spinner)findViewById(R.id.priceTypeText);
		quantity=(Spinner)findViewById(R.id.productquaTxt);
		receiveIntentValues();
		getDetBtn.setOnClickListener(this);
		scanButton.setOnClickListener(this);
		homeButton.setOnClickListener(this);
		getScreenDimensions();
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
			storeValue=StoreName.getText().toString();
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
			
			if(storeValue.length()==0)
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
				new LoadStore().execute(" ");
				
			}
			else
			{
				 Toast.makeText(getApplicationContext(), "Enter the Price", 300).show();
			}
		   break;
		   
		case R.id.scan_button:
			IntentIntegrator.initiateScan(this, R.layout.capture,
	                R.id.viewfinder_view, R.id.preview_view, false);
			break;
		
		case R.id.home_button:
			Intent tab = new Intent(getStoreDetails.this,TabHostActivity.class);
        	tab.putExtra("device_id", deviceId);
		    startActivity(tab);
			break;	
		}
	}
	
	
	
	 public class LoadStore extends AsyncTask<String,Integer,String>{
	        ProgressDialog dialog;
	        protected void onPreExecute(){
	            dialog = new ProgressDialog(getStoreDetails.this);            
	                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            dialog.setMax(100);
	            dialog.setTitle("Please Wait");
	            dialog.setMessage("Loading...");
	            dialog.show();              
	        }
	        @Override
	        protected String doInBackground(String... arg0) {
	                for(int i=0;i<20;i++){
	                publishProgress(5);
	                try {
	                    Thread.sleep(100);// the timing set to a large value
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
	        	JSONObject jsonStre = getStore(Price,storeValue,Barcode,lat,lon,deviceId,Type,Quant);
				 if(jsonStre!=null)
	 			  {
	               try 
	                {
	            	   	String message = jsonStre.getString("message");
	     				TxtStoreDetails.setText("");
	     				Results.setText("");
	     				if(message.equalsIgnoreCase("Barcode Not Exist")==true)
	     				{
	     					TxtStoreDetails.postDelayed(new Runnable() {
	     					    public void run() {
	     					    	TxtStoreDetails.setText("Store Details:");
	     					    }
	     					}, 2000);
	     					
	     					Results.postDelayed(new Runnable() {
	     					    public void run() {
	     					    	Results.setText("We do not have any price data for that item within 50 miles");
	     					    }
	     					}, 2000);
	     					return;
	     				}
	     				
	     				if(message.equalsIgnoreCase("No Store Found")==true)
	     				{
	     					   StatId=jsonStre.getString("stats_id");
	     					   resultid=jsonStre.getString("result_id");
	     					   LayoutInflater layoutInflater 
	     					     = (LayoutInflater)getBaseContext()
	     					      .getSystemService(LAYOUT_INFLATER_SERVICE);  
	     					    View popupView = layoutInflater.inflate(R.layout.popup, null);  
	     					   
	     					
	     					   final PopupWindow popup = new PopupWindow(getStoreDetails.this);
	     					   int width= screenWidth - 50;
	     					   int height= screenHeight - 50;
	     					   popup.setContentView(popupView);
	     					   popup.setWidth(width);
	     					   popup.setHeight(height);
	     					   popup.setFocusable(true);
	     					   popup.showAtLocation(popupView, Gravity.CENTER, 0, 20);
	     					   final LinearLayout LinStoreView=(LinearLayout) popupView.findViewById(R.id.linearLayout1);
	     					   LinStoreView.setVisibility(View.VISIBLE);
	     					   final EditText TxtStoreNameView=(EditText)popupView.findViewById(R.id.StoreName);
	     					   final TextView Lblpopmsg=(TextView)popupView.findViewById(R.id.Lblpopmsg);
	     					   
	     					   Button submit = (Button) popupView.findViewById(R.id.Submit_btn);
	     					   submit.setOnClickListener(new OnClickListener() {
	     					 
	     					     @Override
	     					     public void onClick(View v) {
	     					    	   String storeText=TxtStoreNameView.getText().toString();
	     								if(storeText.length() == 0)
	     								{
//				                         TxtStoreNameView.setError("Store Name required!");
	     									didStore=false;
	     								}
	     								else
	     								{
	     									
	     									didStore=true;
	     								}
	     								StoreDet=TxtStoreNameView.getText().toString();
	     								StreBtnId="0";
	     					
	     								if((didStore))
	     								{
	     									 new LoadProductDetails().execute(" ");
	     									 popup.dismiss();
	     								}
	     					    	 	else
	     					    	 	{
	     					    	 		Toast.makeText(getApplicationContext(), "Please enter the store name...", 100).show();
	     					    	 	}
	     					     }
	     					     
	     					     
	     					     
	     					   });
	     					   
	     					   Button close = (Button) popupView.findViewById(R.id.Cancel_btn);
	     					   close.setOnClickListener(new OnClickListener() {
	     					 
	     					     @Override
	     					     public void onClick(View v) {
	     					       popup.dismiss();
	     					     }
	     					   });
	     				}
	     				
	     				if(message.equalsIgnoreCase("Store Found")==true)
	     				{

	     					  resultid=jsonStre.getString("result_id");
	     					  StatId=jsonStre.getString("stats_id");
	     					   LayoutInflater layoutInflater 
	     					     = (LayoutInflater)getBaseContext()
	     					      .getSystemService(LAYOUT_INFLATER_SERVICE);  
	     					    View popupView = layoutInflater.inflate(R.layout.popup, null);  
	     					             
	     					   
	     					   // Creating the PopupWindow
	     					   final PopupWindow popup = new PopupWindow(getStoreDetails.this);
	     					   int width= screenWidth - 50;
	     					   int height= screenHeight - 50;
	     					   popup.setContentView(popupView);
	     					   popup.setWidth(width);
	     					   popup.setHeight(height);
	     					   popup.setFocusable(true);
	     					   popup.showAtLocation(popupView, Gravity.CENTER, 0, 20);
	     					   
	     					   rdStrGroup = (RadioGroup)popupView.findViewById(R.id.rd_Store_Group);
	     					   if(jsonStre.getString("store_details")!="null")
	     						{
	     							storeArray= new JSONArray(jsonStre.getString("store_details"));
	     							if(storeArray.length()>0)
	     							{
	     								
	     								for (int i=0; i<storeArray.length(); i++)
	     							        {
	     										JSONObject c= storeArray.getJSONObject(i);
//				                         		Toast.makeText(getApplicationContext(), "store Name:"+c.getString("store_name"), 500).show();											
	     										rdStrBtn = new RadioButton(getStoreDetails.this);
	     										Storeid=c.getString("store_id");
	     										rdStrBtn.setId(Integer.parseInt(Storeid));
	     										rdStrBtn.setText(c.getString("store_name"));
	     										rdStrGroup.addView(rdStrBtn);
	     										
	     							        }
	     							}
	     							rdStrBtn = new RadioButton(getStoreDetails.this);
	     							rdStrBtn.setId(0);
	     							rdStrBtn.setText("Other");
	     							rdStrGroup.addView(rdStrBtn);
	     							didStoreWork=false;
	     						}
	     					   
	     					  
	     					   final LinearLayout LinStoreView=(LinearLayout) popupView.findViewById(R.id.linearLayout1);
	     					   final ScrollView ScrolpropView =(ScrollView) popupView.findViewById(R.id.Scrol_PopUp);
	     					   final TextView Lblpopmsg=(TextView)popupView.findViewById(R.id.Lblpopmsg);
	     				       final EditText StoreNameView=(EditText)popupView.findViewById(R.id.StoreName);
	     					   RadioButton checkedRadioButton = (RadioButton)rdStrGroup.findViewById(rdStrGroup.getCheckedRadioButtonId());
	     					   					   
	     					   rdStrGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	     					   {
	     					       public void onCheckedChanged(RadioGroup rdStrGroup, int checkedId)
	     					       {
	     					           // This will get the radiobutton that has changed in its check state
	     					           RadioButton checkedRadioButton = (RadioButton)rdStrGroup.findViewById(checkedId);
	   
	     					           // This puts the value (true/false) into the variable
	     					           boolean isChecked = checkedRadioButton.isChecked();
	     					           // If the radiobutton that has changed in check state is now checked...
	     					           if (isChecked)
	     					           {
	     					        	   StreBtnId=String.valueOf(checkedRadioButton.getId());
	     					        	   storeValue = (String) checkedRadioButton.getText();
	     					        	   if(storeValue.equalsIgnoreCase("Other")==true)
	     									{
	     					        		   LinStoreView.setVisibility(View.VISIBLE);
	     					        		   didStoreWork=false;
	     					        		}
	     					        	   else
	     									{
	     					        		   	LinStoreView.setVisibility(View.GONE);
	     					        		    didStoreWork=true; 
	     									}
	     					           }
	     					       }
	     					   });
	     					   
	     					   Button close = (Button) popupView.findViewById(R.id.Cancel_btn);
	     					   close.setOnClickListener(new OnClickListener() {
	     					 
	     					     @Override
	     					     public void onClick(View v) {
	     					       popup.dismiss();
	     					     }
	     					   });
	     					
	     					   Button submit = (Button) popupView.findViewById(R.id.Submit_btn);
	     					   submit.setOnClickListener(new OnClickListener() {
	     						   String storeText=null;
	     					     @Override
	     					     public void onClick(View v) {
    	  
	     					    	 
	     					    		  	storeText=StoreNameView.getText().toString();
	     									if(storeText.length() == 0)
	     									{
	     										storeTextValue=false;
	     									}
	     									else
	     									{
	     										storeValue=storeText;
	     										storeTextValue=true;
	     										didStoreWork=true;
	     									}
	     								
	     					    	 	if((didStoreWork) || (storeTextValue))
	     								{

	     					    	 		 new LoadProductDetails1().execute(" ");
	     					    	 		 popup.dismiss();
	     								}
	     					    	 	else
	     					    	 	{
	     					    	 		Toast.makeText(getApplicationContext(), "Please select any options...", 1000).show();
	    	 				    	 	}
	     					      
	     					     }
	     					   });
	     											
	     				}
	     		  }
	     		  catch (JSONException e) {
	     				e.printStackTrace();
	     			}
	     		}	
	        }
	    }
	 
	 
	 public class LoadProductDetails extends AsyncTask<String,Integer,String>{
	        ProgressDialog dialog;
	        protected void onPreExecute(){
	            dialog = new ProgressDialog(getStoreDetails.this);            
	                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            dialog.setMax(100);
	            dialog.setMessage("Loading...");
	            dialog.show();              
	        }
	        @Override
	        protected String doInBackground(String... arg0) {
	                for(int i=0;i<5;i++){
	                publishProgress(5);
	                try {
	                    Thread.sleep(1000);// the timing set to a large value
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
	        	JSONObject jsonPro = getProductDetails(StatId,Barcode,lat,lon,deviceId,Type,Quant,resultid,StoreDet,StreBtnId);

				if(jsonPro!=null)
				{
				try 
				{
				
				String message = jsonPro.getString("message");
				if(message.equalsIgnoreCase("Barcode Not Exist")==true)
				{
				Toast.makeText(getApplicationContext(), "No Data Found ...", 1000).show();
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
					final Intent showDetails=new Intent(getStoreDetails.this,showDetails.class);
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
				didStore=false;
	        }
	    }
	 public class LoadProductDetails1 extends AsyncTask<String,Integer,String>{
	        ProgressDialog dialog;
	        protected void onPreExecute(){
	            dialog = new ProgressDialog(getStoreDetails.this);            
	                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            dialog.setMax(100);
	            dialog.setMessage("Loading...");
	            dialog.show();              
	        }
	        @Override
	        protected String doInBackground(String... arg0) {
	                for(int i=0;i<5;i++){
	                publishProgress(5);
	                try {
	                    Thread.sleep(1000);// the timing set to a large value
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

		    	 JSONObject jsonPro = getProductDetails(StatId,Barcode,lat,lon,deviceId,Type,Quant,resultid,storeValue,StreBtnId);

				if(jsonPro!=null)
				{
				  try 
				  {
					
					  String message = jsonPro.getString("message");
					  if(message.equalsIgnoreCase("Barcode Not Exist")==true)
						{
							Toast.makeText(getApplicationContext(), "No Data Found ...", 1000).show();
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
//            												Toast.makeText(getApplicationContext(), "ProductId:"+ProductId, 1000).show();
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
								final Intent showDetails=new Intent(getStoreDetails.this,showDetails.class);
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
				didStoreWork=false;
    	 		storeTextValue=false;
				
	        }
	    }
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    switch (requestCode) {
	        case IntentIntegrator.REQUEST_CODE:
	            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
	                    resultCode, data);
	            if (scanResult == null) {
	                return;
	            }
	            final String result = scanResult.getContents();
	            if (result != null) {
	                handler.post(new Runnable() {
	                    @Override
	                    public void run() {
	                    	contentTxt.setText(result);    
	                    }
	                });
	            }
	            break;
	        default:
	    }
	}

	public JSONObject getProductDetails(String StatId,String Barcode,String lat,String lon,String deviceId,String Type,String Quant,String resultid,String storeName,String Storeid)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("statsid", StatId));
        params.add(new BasicNameValuePair("barcode", Barcode));
        params.add(new BasicNameValuePair("lat",lat));
        params.add(new BasicNameValuePair("lon",lon));
        params.add(new BasicNameValuePair("device_id",deviceId));
        params.add(new BasicNameValuePair("type",Type));
        params.add(new BasicNameValuePair("quant",Quant));
        params.add(new BasicNameValuePair("resultid",resultid));
        params.add(new BasicNameValuePair("storename",storeName));
        params.add(new BasicNameValuePair("storeid",Storeid));
        JSONObject jsonPro = getJSONFromUrl(productURL, params);
        return jsonPro;
	}
	
	public JSONObject getStore(String Price,String storeName,String Barcode,String lat,String lon,String deviceId,String Type,String Quant){
		 
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("price", Price));
        params.add(new BasicNameValuePair("storename", storeName));
        params.add(new BasicNameValuePair("barcode", Barcode));
        params.add(new BasicNameValuePair("lat",lat));
        params.add(new BasicNameValuePair("lon",lon));
        params.add(new BasicNameValuePair("device_id",deviceId));
        params.add(new BasicNameValuePair("type",Type));
        params.add(new BasicNameValuePair("quant",Quant));
        JSONObject jsonStre = getJSONFromUrl(storeURL, params);
        return jsonStre;
    }
 static InputStream is = null;
 static JSONObject json = null;
 static String outPut = "";
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
	        	
	        	TxtStoreDetails.setText("");
	        	Results.setText("");
	        	if(!isConnected(getStoreDetails.this)){
	        		Toast.makeText(getApplicationContext(), "Check your internet connection", 1000).show();
		        	TxtStoreDetails.postDelayed(new Runnable() {
					    public void run() {
					    	TxtStoreDetails.setText("Store Details:");
					    }
					}, 2000);
					
					Results.postDelayed(new Runnable() {
					    public void run() {
					    	Results.setText("Check your internet connection");
					    }
					}, 2000);
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
	        	Results.setText("Connection Timeout");
				
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
	
	public void getScreenDimensions()
	{
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
	}
}
