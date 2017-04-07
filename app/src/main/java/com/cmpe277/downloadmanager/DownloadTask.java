package com.cmpe277.downloadmanager;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
                httpDownloadFile(params[0]);
                publishProgress("Finish download: "+url.getFile());

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

    private void httpDownloadFile(URL param) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String fileURL, String fileName) {

        StatFs stat_fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double avail_sd_space = (double) stat_fs.getAvailableBlocks() * (double) stat_fs.getBlockSize();
        //double GB_Available = (avail_sd_space / 1073741824);
        double MB_Available = (avail_sd_space / 10485783);
        //System.out.println("Available MB : " + MB_Available);
        Log.d("MB", "" + MB_Available);
        try {
            File root = new File(Environment.getExternalStorageDirectory() + "/vvveksperten");
            if (root.exists() && root.isDirectory()) {

            } else {
                root.mkdir();
            }
            Log.d("CURRENT PATH", root.getPath());
            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            int fileSize = c.getContentLength() / 1048576;
            Log.d("FILESIZE", "" + fileSize);
            if (MB_Available <= fileSize) {
                //this.showNotification(getResources().getString(R.string.notification_no_memory), getResources().getString(R.string.notification_error));
                c.disconnect();
                return;
            }

            FileOutputStream f = new FileOutputStream(new File(root.getPath(), fileName));

            InputStream in = c.getInputStream();


            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            File file = new File(root.getAbsolutePath() + "/" + "some.pdf");
            if (file.exists()) {
                file.delete();
                Log.d("FILE-DELETE", "YES");
            } else {
                Log.d("FILE-DELETE", "NO");
            }
            File from = new File(root.getAbsolutePath() + "/" + fileName);
            File to = new File(root.getAbsolutePath() + "/" + "some.pdf");

        } catch (Exception e) {
            Log.d("Downloader", e.getMessage());

        }
    }
}
