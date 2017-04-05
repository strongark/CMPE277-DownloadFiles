package com.cmpe277.downloadmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDownload(View view) {
        //Intent intent = new Intent(this,DownloadService.class);
        Intent intent = new Intent(this,DownloadService.class);
        //put url to intent

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
        intent.putExtra("URLs",urls);
        //startService(intent);
    }

    public void onCancel(View view) {

    }

    public void updateDownloadProgress(final String logMsg){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.txt_log)).append(logMsg+"\n");
            }
        },100);
    }

    public static class ReceiveDownloadMessage extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String message=intent.getExtras().getString("message");
            Toast.makeText(context,message,Toast.LENGTH_LONG);
        }
    }
}
