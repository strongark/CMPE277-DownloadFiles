package com.cmpe277.downloadmanager;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;

/**
 * Created by tranpham on 4/4/17.
 */

public class DownloadTask extends AsyncTask<URL,String,Boolean> {

    final static String TAG ="DownloadTask";
    public interface AsyncResponse{
        void progressUpdate(String msg);
        void postExecute(String msg);
        void cancel();
    }

    AsyncResponse delegateResponse=null;
    DownloadTask(AsyncResponse asyncResponse){
        delegateResponse=asyncResponse;
    }
    //Expect list of URLs
    @Override
    protected Boolean doInBackground(URL... params) {
        Boolean res=Boolean.TRUE;
        try {
            for (URL url:params) {
                //download

                publishProgress(url.getFile());
            }
        }
        catch (Exception ex){
            Log.d(TAG, "Error download file:"+ex.toString());
            res=Boolean.FALSE;
        }
        return res;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        for (String msg:values) {
            delegateResponse.progressUpdate(msg);
        }

    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        if (isSuccess){
            delegateResponse.postExecute("Files is downloaded successfully!");
        }
        else
            delegateResponse.postExecute("There's some error in download process");

    }

}
