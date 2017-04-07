package com.cmpe277.downloadmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    static final String TAG="MyMainActivity";
    Handler handler = new Handler();
    DownloadBoundService downloadBoundService =null;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDownloadProgress(intent.getStringExtra("message"));
        }
    };

    ServiceConnection boundServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBoundService = ((DownloadBoundService.LocalBinder)service).getService();
            URL[] urls = getUrls();
            downloadBoundService.DownloadFile(urls);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView logView= ((TextView)findViewById(R.id.txt_log));
        logView.setText("");
        logView.setMovementMethod(new ScrollingMovementMethod());
        Log.d(TAG, "onCreate");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DOWNLOAD_INTENT_MSG);
        registerReceiver(broadcastReceiver,intentFilter);
    }

    public void onDownload(View view) {
        Log.d(TAG, "Download file");
        //Intent intent = new Intent(this,DownloadStartedService.class);
        Intent intent = new Intent(this,DownloadBoundService.class);
        //put url to intent

        URL[] urls = getUrls();
        intent.putExtra("URLs",urls);
        //startService(intent);
        bindService(intent,boundServiceConnection,BIND_AUTO_CREATE);
    }

    @Nullable
    private URL[] getUrls() {
        URL[] urls=null;
        try {
            urls=new URL[]{
                new URL(((EditText)findViewById(R.id.edt_url1)).getText().toString()),
                new URL(((EditText)findViewById(R.id.edt_url2)).getText().toString()),
                new URL(((EditText)findViewById(R.id.edt_url3)).getText().toString()),
                new URL(((EditText)findViewById(R.id.edt_url4)).getText().toString())
            };
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urls;
    }

    public void onCancel(View view) {
        unbindService(boundServiceConnection);
    }

    public void updateDownloadProgress(final String logMsg){

        handler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.txt_log)).append(logMsg+"\n");
            }
        });
    }

}
