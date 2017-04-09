package com.cmpe277.downloadmanager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class BoundDownloadActivity extends AppCompatActivity {

    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    static final String TAG="MyMainActivity";
    Handler handler = new Handler();
    DownloadBoundService downloadBoundService =null;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String outputText="";
            int code=intent.getIntExtra("code",-1);
            if(code == DownloadTask.DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS){
                int percent=intent.getIntExtra("percentComplete",-1);
                outputText=percent+"%";
                if(percent<100){
                    outputText+="..";
                }
                else
                    outputText+="\n";
            }
            else
                outputText=intent.getStringExtra("message")+"\n";

            updateDownloadProgress(outputText);
        }
    };

    ServiceConnection boundServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBoundService = ((DownloadBoundService.LocalBinder)service).getService();
            Log.i(TAG, "onServiceConnected: Start downloading");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mn_started:
                startActivity(new Intent(this,MainActivity.class));
                break;
            case R.id.mn_intent:
                startActivity(new Intent(this,IntentDownloadActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        };
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bound_menu,menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bound_download);
        TextView logView= ((TextView)findViewById(R.id.txt_log));
        logView.setText("");
        logView.setMovementMethod(new ScrollingMovementMethod());
        Log.d(TAG, "onCreate: "+ "register receiver");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DOWNLOAD_INTENT_MSG);
        registerReceiver(broadcastReceiver,intentFilter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this,DownloadBoundService.class);
        bindService(intent,boundServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(boundServiceConnection);
        unregisterReceiver(broadcastReceiver);
    }

    public void onDownload(View view) {
        Log.d(TAG, "Download file");
        URL[] urls = getUrls();
        downloadBoundService.downloadFile(urls);
    }

    @Nullable
    private URL[] getUrls() {
        URL[] urls=null;
        try {
            urls=new URL[]{
                    new URL(((EditText)findViewById(R.id.edt_url1)).getText().toString()),
                    new URL(((EditText)findViewById(R.id.edt_url2)).getText().toString()),
                    new URL(((EditText)findViewById(R.id.edt_url3)).getText().toString())
            };
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this,"URL is not valid",Toast.LENGTH_LONG).show();
        }
        return urls;
    }

    public void onCancel(View view) {
        downloadBoundService.cancelDownload();
        Intent intent = new Intent(this,DownloadStartedService.class);
    }

    public void updateDownloadProgress(final String logMsg){

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.txt_log)).append(logMsg);
            }
        },100);
    }

    //using result receiver in case of intent service
    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            String outputText="";
            int code=resultData.getInt("code",-1);
            if(code == DownloadTask.DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS){
                int percent=resultData.getInt("percentComplete",-1);
                outputText=percent+"%";
                if(percent<100){
                    outputText+="..";
                }
                else
                    outputText+="\n";
            }
            else
                outputText=resultData.getString("message")+"\n";

            updateDownloadProgress(outputText);
        }
    }}
