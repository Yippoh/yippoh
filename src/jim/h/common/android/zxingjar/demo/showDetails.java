package jim.h.common.android.zxingjar.demo;

import jim.h.common.android.zxinglib.integrator.IntentIntegrator;
import jim.h.common.android.zxinglib.integrator.IntentResult;
import android.R.color;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
public class showDetails extends Activity implements OnClickListener{
	private Handler handler = new Handler();
	private TextView productTxt,productNameTxt,productPriceTxt,productManuTxt,productBarcodeTxt,msgLbl,LocationDetlbl;
	private TableLayout table;
	private Button scanButton;
	String ProductId,productName,productPrice,productManu,Barcode,Address,Price,locationValue,lat,lon,DeviceId;
  
	String[] locAddress=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.show_details);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_title);	
		findViewById(R.id.logo_btn).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,	KeyEvent.KEYCODE_BACK));
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				}
			});
		receiveProductDetails();
		productTxt=(TextView)findViewById(R.id.productIdTxt);
		productNameTxt=(TextView)findViewById(R.id.productNameTxt);
		productPriceTxt=(TextView)findViewById(R.id.productPriceTxt);
		productManuTxt=(TextView)findViewById(R.id.productManuTxt);
		productBarcodeTxt=(TextView)findViewById(R.id.productBarcodeTxt);
		LocationDetlbl=(TextView)findViewById(R.id.LocationDetlbl);
		scanButton=(Button)findViewById(R.id.scan_button);
		msgLbl=(TextView)findViewById(R.id.msgLbl);
		table=(TableLayout)findViewById(R.id.LocationDetailsTbl);
		table.setVisibility(View.GONE);
		scanButton.setOnClickListener(this);
		setInfoByView();
		getLocation();
		getDeviceId();
	}
	

	public void receiveProductDetails()
	{
		Intent showDetails=getIntent();
		ProductId=showDetails.getStringExtra("ProductId");
		productName=showDetails.getStringExtra("ProductName");
		productPrice=showDetails.getStringExtra("ProductPrice");
		productManu=showDetails.getStringExtra("Manufac");
		Barcode=showDetails.getStringExtra("BarCode");
		Address= showDetails.getStringExtra("Address");
		Price= showDetails.getStringExtra("Price");
		locationValue=showDetails.getStringExtra("locationValue");
		
	}
	
	public void onClick(View v){
		
		switch(v.getId()) {
		case R.id.scan_button:
			IntentIntegrator.initiateScan(this, R.layout.capture,
	                R.id.viewfinder_view, R.id.preview_view, false);
			break;
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
	                    	ShowDetails(result);
//	                        txtScanResult.setText(result);
	                    }
	                });
	            }
	            break;
	        default:
	    }
	}
	
	public void ShowDetails(String scanFormat)
	{
			Intent getDet = new Intent(this,getStoreDetails.class);
			getDet.putExtra("BarCode", scanFormat);
			getDet.putExtra("lat", lat);
			getDet.putExtra("lon", lon);
			getDet.putExtra("device_id", DeviceId);
			startActivity(getDet);
	}
	public void setInfoByView()
	{
		   productTxt.setText(ProductId);
		   productNameTxt.setText(productName);
		   productPriceTxt.setText(productPrice);
		   productManuTxt.setText(productManu);
		   productBarcodeTxt.setText(Barcode);
		   
//		   Toast.makeText(getApplicationContext(), "Address:"+Address, 800).show();    
		   String AddressVal=Address.toString();
//		   Toast.makeText(getApplicationContext(), "AddressVal:"+AddressVal, 800).show();  
		   if((Address.equalsIgnoreCase("null")==true) && (Price.equalsIgnoreCase("null")==true) )
		   {
			   table.setVisibility(View.GONE);
			   LocationDetlbl.setVisibility(View.VISIBLE);
			   LocationDetlbl.setText("Location Details:");
			   msgLbl.setVisibility(View.VISIBLE);
			   msgLbl.setText("No Data Found");
		   }
		   else
		   {
			   LocationDetlbl.setVisibility(View.VISIBLE);
			   LocationDetlbl.setText("Location Details:");
			   table.setVisibility(View.VISIBLE);
			   msgLbl.setVisibility(View.GONE);
			   TableLayout myTableLayout = null;
			    myTableLayout = (TableLayout)findViewById(R.id.LocationDetailsTbl);
			    locAddress=Address.split("%");
			    String[] locPrice=Price.split("%");
			    int count =0;
			    for(int i=0;i<locAddress.length;i++)
			    {
			    	 	// Add row to table
			    	 myTableLayout.addView(createRow(locAddress[i],locPrice[i],count++));
			    	 myTableLayout.addView(createImageView());
			    }
		   }

	}
	
	public TableRow createRow(String name,String type,int count)
	{
	    // Create new row
	    TableRow myTableRow = new TableRow(this);
	   
	    TableRow.LayoutParams llp = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	    llp.setMargins(10, 10, 10, 10);//2px right-margin
	    myTableRow.setLayoutParams(llp);
//	    myTableRow.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
	    myTableRow.setPadding(5, 5, 5, 5);
	    // Add name
	    myTableRow.addView(createTextView(name));
	    myTableRow.addView(createTextView(type));
	    // Add type
	    if(count%2!=0){
	         myTableRow.setBackgroundColor(Color.parseColor("#F2F2F2"));
	    }
	    else
	    {
	    	myTableRow.setBackgroundColor(Color.parseColor("#D2D2D2"));
	    }

	    return myTableRow;
	}
	
	private View createTextView(String name) {
		// TODO Auto-generated method stub
		TextView tv_name =new TextView(this);
		tv_name.setText(name);
		tv_name.setTextSize(16);
		tv_name.setWidth(200);
		tv_name.setPadding(5, 0, 20, 0);
//	    tv_name.setPadding(left, top, right, bottom)
		tv_name.setGravity(Gravity.LEFT);
//		tv_name.setBackgroundColor(R.layout.cell_shape);
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
	public void getLocation()
	{
	    // GPSTracker class
	    GPSTracker gps;
	    gps = new GPSTracker(showDetails.this);
	    
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
	
}
