package com.cmpe277.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.URL;

/**
 * Created by tranpham on 4/6/17.
 */

public class DownloadBoundService extends Service {
    static final String TAG = "My"+DownloadBoundService.class.getSimpleName();
    static final String DOWNLOAD_INTENT_MSG="com.cmpe277.downloadmanager.message";
    DownloadTask externalDownloadTask = new DownloadTask(new DownloadTask.AsyncResponse() {
        @Override
        public void progressUpdate(String msg) {
            broadcastDownloadUpdate(msg);
        }

        @Override
        public void postExecute(String msg) {
            broadcastDownloadUpdate(msg);
        }

        @Override
        public void cancel() {

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
        //new downloadTask().execute(urls);
        externalDownloadTask.execute(urls);
    }

    private void DownloadFile(URL url){
        Log.d(TAG, "DownloadFile: "+url.toString());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastDownloadUpdate(String msg){
        Intent intent = new Intent(DOWNLOAD_INTENT_MSG);
        intent.putExtra("message",msg);
        sendBroadcast(intent);
    }
    private class downloadTask extends AsyncTask<URL,String,Integer>{

        @Override
        protected Integer doInBackground(URL... params) {
            for (URL url:params) {
                DownloadFile(params[0]);
                publishProgress("Finish download: "+url.getFile());
            }

            return 1;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            broadcastDownloadUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            broadcastDownloadUpdate("Download complete");
        }
    }
}
