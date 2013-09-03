package jim.h.common.android.zxingjar.demo;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.widget.Toast;
public class Splash extends Activity {
	
    private long ms=0;
    private long splashTime=2000;
    private boolean splashActive = true;
    private boolean paused=false;
    String DeviceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hides the titlebar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        getDeviceId();
        Thread mythread = new Thread() {
            public void run() {
                try {
                    while (splashActive && ms < splashTime) {
                        if(!paused)
                            ms=ms+100;
                        sleep(100);
                    }
                } catch(Exception e) {}
                finally {
                	Intent tab = new Intent(Splash.this,TabHostActivity.class);
                	tab.putExtra("device_id", DeviceId);
        		    startActivity(tab);
        		    finish();
//                	Intent checkDeviceId = new Intent(Splash.this,CheckDeviceId.class);
//                	checkDeviceId.putExtra("device_id", DeviceId);
//					startActivity(checkDeviceId);
                	
//                	Intent Scan = new Intent(Splash.this,Scan.class);
//					startActivity(Scan);
                }
            }
        };
        mythread.start();
    }
    public void getDeviceId()
    {
    	
         TelephonyManager telephonyManager  =  (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    		    
    	 /*
    	* getDeviceId() function Returns the unique device ID.
    	* for example,the IMEI for GSM and the MEID or ESN for CDMA phones.  
    	*/				    
         DeviceId = telephonyManager.getDeviceId();
//         Toast.makeText(getApplicationContext(), "DeviceId:" +DeviceId, 500).show();
     
    }
   
 }
