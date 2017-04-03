package net.learn2develop.Services;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	IntentFilter intentFilter;
	private MyService serviceBinder;
	Intent i;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //---intent to filter for file downloaded intent---
        intentFilter = new IntentFilter();
        intentFilter.addAction("FILE_DOWNLOADED_ACTION");

        //---register the receiver---
        registerReceiver(intentReceiver, intentFilter);
        
        Button btnStart = (Button) findViewById(R.id.btnStartService);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// DEMO 1
            	Intent intent = new Intent(getBaseContext(), MyService.class);
				try {					
					URL[] urls = new URL[] {
							new URL("http://www.amazon.com/somefiles.pdf"), 
							new URL("http://www.wrox.com/somefiles.pdf"),
							new URL("http://www.google.com/somefiles.pdf"),
							new URL("http://www.learn2develop.net/somefiles.pdf")};
					intent.putExtra("URLs", urls);
					
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}            	
            	startService(intent);

            	            	            	
            	// startService(new Intent(getBaseContext(), MyService.class));
            	// startService(new Intent("net.learn2develop.MyService"));            	
             // startService(new Intent(getBaseContext(), MyIntentService.class));
            	
           
            	// Demo 3
            	
           /*	i = new Intent(getBaseContext(), MyIntentService.class);
            	
            	try {					
					URL[] urls = new URL[] {
							new URL("http://hanuinnotech.com/whitepapers/MuraiDataAnalytics(MDAT)-WhitePaper.pdf"), 
							new URL("http://www.sanjeevani-ehr.com/marketing/Sanjeevani-CloudStorage_Whitepaper.pdf"),
							new URL("http://www.sanjeevani-ehr.com/marketing/Sanjeevani-CloudStorage_Whitepaper.pdf"),
							new URL("http://www.learn2develop.net/somefiles.pdf")};
					i.putExtra("URLs", urls);
					
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
            	
            	bindService(i, connection, Context.BIND_ADJUST_WITH_ACTIVITY);
            	*/
            }
        });
        
        Button btnStop = (Button) findViewById(R.id.btnStopService);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                
            	stopService(new Intent(getBaseContext(), MyService.class));            	
            }
        });
    }    
    
    private ServiceConnection connection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		//---called when the connection is made---
    		serviceBinder = ((MyService.MyBinder)service).getService(); 
    		
    		 try {
					URL[] urls = new URL[] {
							new URL("http://www.amazon.com/somefiles.pdf"), 
							new URL("http://www.wrox.com/somefiles.pdf"),
							new URL("http://www.google.com/somefiles.pdf"),
							new URL("http://www.learn2develop.net/somefiles.pdf")};
					serviceBinder.urls = urls;
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
				startService(i);
    	}
    	public void onServiceDisconnected(ComponentName className) {
    	    //---called when the service disconnects---
    		serviceBinder = null;    		
    	}
    };
    
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {  
	        Toast.makeText(getBaseContext(), "File downloaded!", 
	        		Toast.LENGTH_LONG).show();	  
	    }
	};    
}