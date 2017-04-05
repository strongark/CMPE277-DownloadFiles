package com.cmpe277.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.URL;

/**
 * Created by tranpham on 4/4/17.
 */

public class DownloadService extends Service {
    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    static final String TAG="DownloadService";
    DownloadTask downloadTask=null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"service created");
        downloadTask = new DownloadTask(new DownloadTask.AsyncResponse() {
            @Override
            public void progressUpdate(String msg) {
                broadcastDownloadProgress(msg);
            }

            @Override
            public void postExecute(String msg) {
                broadcastDownloadProgress(msg);
                stopSelf();
            }

            @Override
            public void cancel() {
                broadcastDownloadProgress("Cancelled!");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        Object[] objUrls = (Object[]) intent.getExtras().get("URLs");
        URL[] urls = new URL[objUrls.length];
        for (int i=0; i<objUrls.length; i++) {
            urls[i] = (URL) objUrls[i];

        }
        broadcastDownloadProgress("Starting to download");
        //downloadTask.execute(urls);
        return START_STICKY;

    }

    public void broadcastDownloadProgress(String message){
        Intent downloadProgressUpdate=new Intent(DOWNLOAD_INTENT_MSG);
        downloadProgressUpdate.putExtra("message",message);
        sendBroadcast(downloadProgressUpdate);
    }
}
