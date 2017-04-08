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
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by tranpham on 4/6/17.
 */

public class DownloadBoundService extends Service {

    static final String TAG = "My"+DownloadBoundService.class.getSimpleName();
    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    Queue<URL> downloadQueue=new LinkedList<URL>();
    boolean isDownloading=false;
    DownloadTask externalDownloadTask = null;

    private void processDownloadQueue() {
        Log.i(TAG, "processDownloadQueue: ");
        if(!isDownloading){
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

    public void downloadFile(URL...urls){
        /*
        * Async download task
        * queue up all the url to be downloaded because Async can only process one at a time
        * except the case we want to create an executor to pool multiple async task
        * TODO: implement executor to pool multiple async
        * for now just download one by one
        * */
        for (URL url:urls)
            downloadQueue.add(url);

        processDownloadQueue();
    }

    public void cancelDownload(){

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
