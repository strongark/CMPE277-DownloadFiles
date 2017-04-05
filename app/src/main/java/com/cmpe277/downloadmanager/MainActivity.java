package com.cmpe277.downloadmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    static final String TAG="MyMainActivity";
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView logView= ((TextView)findViewById(R.id.txt_log));
        logView.setText("");
        logView.setMovementMethod(new ScrollingMovementMethod());
        Log.d(TAG, "onCreate");
    }

    public void onDownload(View view) {
        Log.d(TAG, "Download file");
        //Intent intent = new Intent(this,DownloadService.class);
        Intent intent = new Intent(this,PreDownloadIntentService.class);
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
        MyResultReceiver receiver = new MyResultReceiver(null);
        intent.putExtra("receiver",receiver);
        startService(intent);
    }

    public void onCancel(View view) {

    }

    public void updateDownloadProgress(final String logMsg){

        handler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.txt_log)).append(logMsg+"\n");
            }
        });
    }

//    public static class ReceiveDownloadMessage extends BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String message=intent.getExtras().getString("message");
//            Toast.makeText(context,message,Toast.LENGTH_LONG);
//        }
//    }
    private class MyResultReceiver extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        Log.i(TAG, "Receive message from service");
        Log.i(TAG, Thread.currentThread().getName());
        super.onReceiveResult(resultCode, resultData);
        if(resultCode==1&&resultData!=null){
            String msg=resultData.getString("message");
            updateDownloadProgress(msg);
        }
    }
}
}
