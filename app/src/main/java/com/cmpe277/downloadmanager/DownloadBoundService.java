package com.cmpe277.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.net.URL;

/**
 * Created by tranpham on 4/6/17.
 */

public class DownloadBoundService extends Service {
    static final String TAG = "My"+DownloadBoundService.class.getSimpleName();
    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    DownloadTask externalDownloadTask = new DownloadTask(new DownloadTask.DownloadCallback() {

        @Override
        public void updateFromDownload(String result) {
            broadcastDownloadProgress(result);
        }

        @Override
        public NetworkInfo getActiveNetworkInfo() {
            ConnectivityManager connectivityManager=
                    (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            return connectivityManager.getActiveNetworkInfo();
        }

        @Override
        public void onProgressUpdate(int progressCode, int percentComplete) {

            switch(progressCode) {
                // You can add UI behavior for progress updates here.
                case Progress.ERROR:
                    break;
                case Progress.CONNECT_SUCCESS:
                    break;
                case Progress.GET_INPUT_STREAM_SUCCESS:
                    break;
                case Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS:
                    broadcastDownloadProgress(percentComplete+"%");
                    break;
                case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                    break;
            }
        }

        @Override
        public void finishDownloading() {
            broadcastDownloadProgress("Finish Download!");
        }
    });

    IBinder binder = new LocalBinder();
    class LocalBinder extends Binder {
        public DownloadBoundService getService(){
            return DownloadBoundService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: "+Thread.currentThread().getName());
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: "+Thread.currentThread().getName());
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: "+Thread.currentThread().getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: "+Thread.currentThread().getName());
    }

    public void DownloadFile(URL...urls){
        //Async download task
        //TODO handle download multiple urls
        externalDownloadTask.execute(urls);
    }

    public void broadcastDownloadProgress(String message){
        Intent downloadProgressUpdate=new Intent(DOWNLOAD_INTENT_MSG);
        downloadProgressUpdate.putExtra("message",message);
        sendBroadcast(downloadProgressUpdate);
    }

    public void broadcastDownloadProgress(String message, int percentComplete){
        Intent downloadProgressUpdate=new Intent(DOWNLOAD_INTENT_MSG);
        downloadProgressUpdate.putExtra("message",message);
        downloadProgressUpdate.putExtra("code"
                , DownloadTask.DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS);
        downloadProgressUpdate.putExtra("percentComplete",percentComplete);
        sendBroadcast(downloadProgressUpdate);
    }

}
