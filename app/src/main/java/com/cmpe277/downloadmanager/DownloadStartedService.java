package com.cmpe277.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by tranpham on 4/4/17.
 */

public class DownloadStartedService extends Service {
    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    static final String TAG="DownloadStartedService";
    boolean isDownloading=false;
    boolean isCancelled=false;
    Queue<URL> downloadQueue = new LinkedList<URL>();
    DownloadTask externalDownloadTask=null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"service created");
    }


    private void processDownloadQueue() {
        Log.i(TAG, "processDownloadQueue: ");
        if(!isDownloading&&!isCancelled){
            URL url=downloadQueue.poll();
            if(url!=null)
            {
                Log.i(TAG, "processDownloadQueue: initiate download task");
                isDownloading=true;
                String[] urlString=url.toString().split("/");
                broadcastDownloadProgress("Download "+urlString[urlString.length-1]);
                externalDownloadTask= new DownloadTask(new DownloadTask.DownloadCallback() {

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
                                broadcastDownloadProgress("update percentage",percentComplete);
                                break;
                            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                                break;
                            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                                break;
                            case Progress.DOWNLOAD_SUCCESS:
                                break;

                        }
                    }

                    @Override
                    public void finishDownloading() {
                        broadcastDownloadProgress("Finish Download!");
                        isDownloading=false;
                        //continue until the queue is empty
                        processDownloadQueue();
                    }
                });
                externalDownloadTask.execute(url);
            }
        }
        else{
            Log.i(TAG, "processDownloadQueue: defer download job because a download task is running");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        Object[] objUrls = (Object[]) intent.getExtras().get("urls");
        for (int i=0; i<objUrls.length; i++) {
            URL url = (URL) objUrls[i];
            downloadQueue.add(url);
        }
        Log.i(TAG, "onStartCommand: number of urls "+objUrls.length);
        broadcastDownloadProgress("Starting to download");
        processDownloadQueue();
        return START_STICKY;

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
