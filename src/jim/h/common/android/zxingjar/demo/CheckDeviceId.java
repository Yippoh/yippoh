package jim.h.common.android.zxingjar.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class CheckDeviceId extends Activity {
//	private JSONParser jsonParser;
//	private static String UserURL = "http://115.112.70.158/barcode_stats/user.php";
	String DeviceId,lat,lon;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		jsonParser = new JSONParser();
		getLocation();
		receiveDeviceValues();
//		InsertDeviceID(DeviceId);
		ShowScan();
	}

	public void ShowScan() {
		// TODO Auto-generated method stub
		DatabaseHandler db = new DatabaseHandler(this);
		int checkexists = db.CheckExists(DeviceId);
    	if(checkexists>0)
    	{
    		Contact exists =db.getContact(DeviceId);
    		int count=0;
    		if((exists._count >= 1) && (exists._count<=9) && (exists._device_id.equalsIgnoreCase(DeviceId)== true))
    		{
    			count=exists._count+1;
    			if(count == 10)
    			{
    				Toast.makeText(getApplicationContext(), "Already used Scanner for: "+10+" times\nNext Time you will be asked for Email ID",5000).show();
    			}
    			db.updateContact(new Contact(DeviceId,null, count));
//    			Intent Scan = new Intent(this,Scan.class);
//    		    startActivity(Scan);
    			Intent getDet = new Intent(this,getDetails.class);
				getDet.putExtra("BarCode", "W1LS2M");
				getDet.putExtra("format", "QR_CODE");
				getDet.putExtra("lat", lat);
				getDet.putExtra("lon", lon);
				getDet.putExtra("device_id", DeviceId);
				startActivity(getDet);
//    			Intent tab = new Intent(this,TabHostActivity.class);
//    		    startActivity(tab);
    		}
    		else
    		{
    			if(exists._email_id==null)
    			{
    				Toast.makeText(getApplicationContext(), "Please Provide your Email",5000).show();
    				Intent Login = new Intent(this,Login.class);
    				startActivity(Login);
    			}
    			else
    			{
    				count=exists._count+1;
    				db.updateContact(new Contact(DeviceId,exists._email_id, count));
//    				Intent Scan = new Intent(this,Scan.class);
//        		    startActivity(Scan);
    				Intent getDet = new Intent(this,getDetails.class);
    				getDet.putExtra("BarCode", "W1LS2M");
    				getDet.putExtra("format", "QR_CODE");
    				getDet.putExtra("lat", lat);
    				getDet.putExtra("lon", lon);
    				getDet.putExtra("device_id", DeviceId);
    				startActivity(getDet);
//    				Intent tab = new Intent(this,TabHostActivity.class);
//        		    startActivity(tab);
    			}
    		}
    	
    	}
    	else
    	{
    		db.addContact(new Contact(DeviceId,null, 1));  
//    		Intent Scan = new Intent(this,Scan.class);
//		    startActivity(Scan);
    		Intent getDet = new Intent(this,getDetails.class);
			getDet.putExtra("BarCode", "W1LS2M");
			getDet.putExtra("format", "QR_CODE");
			getDet.putExtra("lat", lat);
			getDet.putExtra("lon", lon);
			getDet.putExtra("device_id", DeviceId);
			startActivity(getDet);
//    		Intent tab = new Intent(this,TabHostActivity.class);
//		    startActivity(tab);
    	}
    	List<Contact> contacts = db.getAllContacts();       
   	 	String log =null;
       for (Contact cn : contacts) {
       	log= "\nId: "+cn.getID()+" ,\nName: " + cn.getDeviceID() + " ,\nEmail:"+cn.getEmailID()+"\nCount: " + cn.getCount();
//       	Toast.makeText(getApplicationContext(), "details saved:" +log, 1000).show();
       }

	}

	public void receiveDeviceValues()
	{
		Intent checkDeviceId=getIntent();
		DeviceId=checkDeviceId.getStringExtra("device_id");
//		Toast.makeText(getApplicationContext(), "DeviceId:" +DeviceId, 500).show(); 
	}
	
	
	
    public void InsertDeviceID(String deviceId)
    {
    	DatabaseHandler db = new DatabaseHandler(this);
//    	db.deleteContact(new Contact(deviceId, 1));
    	int count=0;
    	int checkexists = db.CheckExists(deviceId);
    	if(checkexists>0)
    	{
    		Contact exists = db.getContact(deviceId);
    		if((exists._count >= 1) && (exists._device_id.equalsIgnoreCase(deviceId)== true))
    		{
    			count=exists._count+1;
    			if(count == 9)
    			{
    				Toast.makeText(getApplicationContext(), "Already used Scanner for: "+count+" times\nNext Time you will be asked for Email ID",5000).show();
    			}
    			db.updateContact(new Contact(deviceId,null, count));
    		}
    	}
    	else
    	{
    		db.addContact(new Contact(deviceId,null, 1));  
    	}
    	

    	List<Contact> contacts = db.getAllContacts();       
    	 String log =null;
        for (Contact cn : contacts) {
        	log= "\nId: "+cn.getID()+" ,\nName: " + cn.getDeviceID() + " ,\nEmail:"+cn.getEmailID()+"\nCount: " + cn.getCount();
//        	Toast.makeText(getApplicationContext(), "details saved:" +log, 1000).show();
        	
        }
        
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
}
