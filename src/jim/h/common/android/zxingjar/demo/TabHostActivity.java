package jim.h.common.android.zxingjar.demo;



import java.util.List;

import jim.h.common.android.zxingjar.demo.getStoreDetails.LoadStore;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.view.KeyEvent;

public class TabHostActivity extends TabActivity implements OnTabChangeListener {
	ProgressDialog progDialog;
	private int progressBarStatus = 0;
	String DeviceId,lat,lon;
	 @Override
	public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);	
		  setContentView(R.layout.tab_host);
		  getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_title);	
//		  ((TextView)findViewById(R.id.title)).setText("gradient shadow");
		  findViewById(R.id.back).setVisibility(View.GONE);
		  findViewById(R.id.logo_btn).setEnabled(false);
//		  findViewById(R.id.back).setOnClickListener(new OnClickListener() {
//				public void onClick(View v) {
//					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,	KeyEvent.KEYCODE_BACK));
//					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
//				}
//			});

		  getLocation();
		  receiveDeviceValues();
	  
		  Resources res = getResources(); // Resource object to get Drawables
		  TabHost tabHost = getTabHost(); // The activity TabHost
//		// Inbox Tab
		 
	        TabSpec inboxSpec = tabHost.newTabSpec("HOME");
	        // Tab Icon
	        inboxSpec.setIndicator("HOME", null);
	        Intent inboxIntent = new Intent(this, Scan.class);
	        // Tab Content
	        inboxSpec.setContent(inboxIntent);
	         
	        // Outbox Tab
	        TabSpec outboxSpec = tabHost.newTabSpec("HISTORY");
//	        outboxSpec.setIndicator("HISTORY", getResources().getDrawable(R.drawable.icon));
	        outboxSpec.setIndicator("HISTORY", null);
//	        ShowProgressBar();
	       
	        Intent outboxIntent = new Intent(this, History.class);
	        
	        outboxSpec.setContent(outboxIntent);
//	        HideProgressBar();
	        // Profile Tab
	        TabSpec profileSpec = tabHost.newTabSpec("REWARDS");
	        profileSpec.setIndicator("REWARDS", null);
//	        ShowProgressBar();
	        Intent profileIntent = new Intent(this, Rewards.class);
	        profileSpec.setContent(profileIntent);
//	        HideProgressBar(); 
	        // Adding all TabSpec to TabHost
	        tabHost.addTab(inboxSpec); // Adding Inbox tab
	        tabHost.addTab(outboxSpec); // Adding Outbox tab
	        tabHost.addTab(profileSpec); // Adding Profile tab
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
	    			tabHost.setCurrentTab(0);
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
	    				tabHost.setCurrentTab(0);
	    			}
	    		}
	    	
	    	}
	    	else
	    	{
	    		db.addContact(new Contact(DeviceId,null, 1));
	    		tabHost.setCurrentTab(0);
	    	}
	    	List<Contact> contacts = db.getAllContacts();       
	   	 	String log =null;
	       for (Contact cn : contacts) {
	       	log= "\nId: "+cn.getID()+" ,\nName: " + cn.getDeviceID() + " ,\nEmail:"+cn.getEmailID()+"\nCount: " + cn.getCount();
	       	Toast.makeText(getApplicationContext(), "details saved:" +log, 1000).show();
	       }
	    		
	        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
            {
                TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                tv.setTextColor(Color.WHITE);
                tv.setPadding(0, 0, 0,30);
                tv.setGravity(Gravity.CENTER);
               
            } 
	        tabHost.setOnTabChangedListener(this);
	        getTabWidget().getChildAt(1).setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	for(int i=0;i<getTabWidget().getChildCount();i++)
	    		    {
	    		        getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.button_normal); //unselected
	    		    }
	            	getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.button_clicked);
	            	new LoadTab().execute(" ");
	            	
//	            	 ShowProgressBar();
	            	
	            	
//	            	 HideProgressBar();
	            }
	            	
	            });
	        getTabWidget().getChildAt(2).setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	for(int i=0;i<getTabWidget().getChildCount();i++)
	    		    {
	    		        getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.button_normal); //unselected
	    		    }
	            	getTabWidget().getChildAt(2).setBackgroundResource(R.drawable.button_clicked);
	            	new LoadTab1().execute(" ");
	            }
	            });

	        for(int i=0;i<getTabWidget().getChildCount();i++)
		    {
		        getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.button_normal); //unselected
		    }
	        getTabWidget().getChildAt(getTabHost().getCurrentTab()).setBackgroundResource(R.drawable.button_clicked); // selected
		 }
	
	 public class LoadTab extends AsyncTask<String,Integer,String>{
	        ProgressDialog dialog;
	        protected void onPreExecute(){
	            dialog = new ProgressDialog(TabHostActivity.this);            
	                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            dialog.setMax(100);
	            dialog.setMessage("Loading...");
	            dialog.show();              
	        }
	        @Override
	        protected String doInBackground(String...arg0) {
	                for(int i=0;i<6;i++){
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
	    protected void onProgressUpdate(Integer...progress){
	        dialog.incrementProgressBy(progress[0]);
	    }
	        protected void onPostExecute(String result){
	        	 getTabHost().setCurrentTab(1);
	        }
	    }
	 public class LoadTab1 extends AsyncTask<String,Integer,String>{
	        ProgressDialog dialog;
	        protected void onPreExecute(){
	            dialog = new ProgressDialog(TabHostActivity.this);            
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
	        	 getTabHost().setCurrentTab(2);
	        }
	    }
	 public void receiveDeviceValues()
		{
			Intent tab=getIntent();
			DeviceId=tab.getStringExtra("device_id");
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
		        lat=String.valueOf(latitude);
		        lon=String.valueOf(longitude);
//		        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lat + "\nLong: " + lon , 500).show();   
		        
		       
		    }
		    else{
		        // can't get location
		        // GPS or Network is not enabled
		        // Ask user to enable GPS/network in settings
		        gps.showSettingsAlert();
		        Toast.makeText(getApplicationContext(), "Unable to get Location" , 500).show();   
		    }
		    
		}
	 
	 public void onTabChanged(String tabId) {
		 for(int i=0;i<getTabWidget().getChildCount();i++)
		    {
		        getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.button_normal); //unselected
		    }
		    getTabWidget().getChildAt(getTabHost().getCurrentTab()).setBackgroundResource(R.drawable.button_clicked); // selected
	 }
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
	 public void HideProgressBar()
	 {
		 new Thread(new Runnable() {
			  public void run() {
						// sleep 5 seconds, so that you can see the 100%
						try {
							Thread.sleep(1000);
							progDialog.dismiss();
							// close the progress bar dialog
//							progDialog.dismiss();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						}
			  }).start();	
	 }
	
}
