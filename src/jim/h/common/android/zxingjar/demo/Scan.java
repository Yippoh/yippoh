package jim.h.common.android.zxingjar.demo;
import jim.h.common.android.zxinglib.integrator.IntentIntegrator;
import jim.h.common.android.zxinglib.integrator.IntentResult;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;                                           
import android.content.Context;                                                       


public class Scan extends Activity implements OnClickListener {
	private Handler  handler = new Handler();
	private Button scanBtn;
	String lat;
	String lon;
	String DeviceId = null;      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan);
	    scanBtn = (Button)findViewById(R.id.scan_button);
	    scanBtn.setOnClickListener(this);
	    getLocation();
	    getDeviceId();
	}


public void onClick(View v){
	
	switch(v.getId()) {
	case R.id.scan_button:
		IntentIntegrator.initiateScan(this, R.layout.capture,
                R.id.viewfinder_view, R.id.preview_view, false);
//		IntentIntegrator scanIntegrator = new IntentIntegrator(this);
//		scanIntegrator.initiateScan();
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
//                        txtScanResult.setText(result);
                    }
                });
            }
            break;
        default:
    }
}

//public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//	//retrieve result of scanning - instantiate ZXing object
//	IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//	//check we have a valid result
//	if (scanningResult != null) {
//		//get content from Intent Result
//		String scanContent = scanningResult.getContents();
//		//get format name of data scanned
//		String scanFormat = scanningResult.getFormatName();
//		
//		ShowDetails(scanContent,scanFormat);
//	}
//	else{
//		//invalid scan data or scan cancelled
//		Toast.makeText(getApplicationContext(), 
//				"No scan data received!", 500).show();
//		
//		return;
//	}
//}

public void ShowDetails(String scanFormat)
{
		Intent getDet = new Intent(this,getDetails.class);
		getDet.putExtra("BarCode", scanFormat);
		getDet.putExtra("lat", lat);
		getDet.putExtra("lon", lon);
		getDet.putExtra("device_id", DeviceId);
		startActivity(getDet);
}

public void getLocation()
{
    // GPSTracker class
    GPSTracker gps;
    gps = new GPSTracker(Scan.this);
    
    // check if GPS enabled     
    if(gps.canGetLocation()){
         
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
     // \n is for new line
//        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, 800).show();    
        lat=String.valueOf(latitude);
        lon=String.valueOf(longitude);
//        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lat + "\nLong: " + lon , 500).show();   
        
       
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
//     Toast.makeText(getApplicationContext(), "DeviceId:" +DeviceId, 500).show();   
}
}