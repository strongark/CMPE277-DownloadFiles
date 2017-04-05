package com.cmpe277.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

//use it as unbound service
public class PreDownloadService extends Service {
    static final String TAG = "PreDownloadService";

    int counter = 0;
    public URL[] urls;

    static final int UPDATE_INTERVAL = 1000;
    private Timer timer = new Timer();


    public PreDownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    //do some long running task here
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Service stated",Toast.LENGTH_LONG).show();
        Log.d(TAG,"service started");

        doSomethingRepeatedly();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void doSomethingRepeatedly() {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Log.d("MyService", String.valueOf(++counter));
                try {
                    Thread.sleep(4000);
                    Log.d("MyService", counter + " Finished");

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 0, UPDATE_INTERVAL);
    }
}

/*Learning objective
- Unbound service
- Bound service
- Intent service
* */