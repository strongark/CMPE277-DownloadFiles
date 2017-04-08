package com.cmpe277.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.net.URL;

/**
 * Created by tranpham on 4/4/17.
 */

public class DownloadStartedService extends Service {
    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    static final String TAG="DownloadStartedService";
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
        downloadTask = new DownloadTask(new DownloadTask.DownloadCallback() {
            @Override
            public void updateFromDownload(String result) {

            }

            @Override
            public NetworkInfo getActiveNetworkInfo() {
                return null;
            }

            @Override
            public void onProgressUpdate(int progressCode, int percentComplete) {

            }

            @Override
            public void finishDownloading() {

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
