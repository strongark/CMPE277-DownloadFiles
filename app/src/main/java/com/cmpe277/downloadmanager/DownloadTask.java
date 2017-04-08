package com.cmpe277.downloadmanager;

import android.app.usage.NetworkStats;
import android.database.CursorJoiner;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by tranpham on 4/4/17.
 */

public class DownloadTask extends AsyncTask<URL,Integer,DownloadTask.Result> {

    final static String TAG ="MyDownloadTask";
    String filePath="/data/user/0/com.cmpe277.downloadmanager/files";
    String fileName;

    public interface DownloadCallback {

        interface Progress{
            int ERROR = -1;
            int CONNECT_SUCCESS = 0;
            int GET_INPUT_STREAM_SUCCESS = 1;
            int PROCESS_INPUT_STREAM_IN_PROGRESS = 2;
            int PROCESS_INPUT_STREAM_SUCCESS = 3;
            int GET_OUTPUT_STREAM_SUCCESS=4;
            int PROCESS_OUTPUT_STREAM_IN_PROGRESS = 5;
            int DOWNLOAD_SUCCESS = 6;
        }
        /**
         * Indicates that the callback handler needs to update its appearance or information based on
         * the result of the task. Expected to be called from the main thread.
         */
        void updateFromDownload(String result);
        /**
         * Get the device's active network status in the form of a NetworkInfo object.
         */
        NetworkInfo getActiveNetworkInfo();
        /**
         * Indicate to callback handler any progress update.
         * @param progressCode must be one of the constants defined in DownloadCallback.Progress.
         * @param percentComplete must be 0-100.
         */
        void onProgressUpdate(int progressCode, int percentComplete);
        /**
         * Indicates that the download operation has finished. This method is called even if the
         * download hasn't completed successfully.
         */
        void finishDownloading();
    }

    DownloadCallback mCallback =null;
    DownloadTask(DownloadCallback callback){
        mCallback =callback;
    }

    /**
     * Wrapper class that serves as a union of a result value and an exception. When the download
     * task has completed, either the result value or exception can be a non-null value.
     * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
     */
    static class Result {
        public String mResultValue;
        public Exception mException;
        public Result(String resultValue) {
            mResultValue = resultValue;
        }
        public Result(Exception exception) {
            mException = exception;
        }
    }

    @Override
    protected void onPreExecute() {
        Log.i(TAG, "onPreExecute: ");
        if(mCallback!=null){
            NetworkInfo networkInfo=mCallback.getActiveNetworkInfo();
            if(networkInfo==null||!networkInfo.isConnected()
               ||(networkInfo.getType()!= ConnectivityManager.TYPE_WIFI
                    &&networkInfo.getType()!=ConnectivityManager.TYPE_MOBILE)
                    ){
                //no connectivity should terminate the task and callback
                mCallback.updateFromDownload(null);
                cancel(true);
            }
            Log.i(TAG, "onPreExecute: get file path");
        }
    }

    @Override
    protected void onCancelled(Result result) {
        Log.i(TAG, "onCancelled: ");
        super.onCancelled(result);
        mCallback.finishDownloading();
    }

    /*
     *Expect 1 url, if many url, should call multiple async task
     *this is easier to scale
     */
    @Override
    protected DownloadTask.Result doInBackground(URL... urls) {
        Result result=null;
        Log.i(TAG, "doInBackground: ");
        if (!isCancelled() && urls!=null && urls.length>0){
            try {
                //download
                httpDownloadFile(urls[0]);
                result=new Result("SUCCESS");
            }
            catch (Exception ex){
                Log.d(TAG, "Error download file:"+ex.toString());
                result = new Result(ex);
            }
        }
        return result;
    }


    @Override
    protected void onPostExecute(DownloadTask.Result result) {
        Log.i(TAG, "onPostExecute: ");
        if(result!=null&&mCallback!=null){
            if(result.mException!=null){
                mCallback.updateFromDownload(result.mException.getMessage());
            }
            else if(result.mResultValue!=null){
                mCallback.updateFromDownload(result.mResultValue);
            }
            mCallback.finishDownloading();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(values.length>1)
            mCallback.onProgressUpdate(values[0],values[1]);
        else
            mCallback.onProgressUpdate(values[0],0);
    }

    /**
     * Download using http Client: HttpsURLConnection
     *
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it will read from the url and write to a file.
     * Otherwise, it will throw an IOException.
     **/
    private void httpDownloadFile(URL url) throws IOException {
        InputStream inputStream=null;
        HttpURLConnection connection=null;
        OutputStream outputStream=null;
        try {
            Log.i(TAG, "httpDownloadFile: making connection to "+url.toString());
            connection= (HttpURLConnection) url.openConnection();
            //set timeout for reading to input stream
            connection.setReadTimeout(3000);
            //set timeout for connect();
            connection.setConnectTimeout(3000);
            //for this case we just read from the source so it is GET
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            /*
            * do real connection here
             * can throw IOException or SocketTimeoutException
            * */
            connection.connect();
            publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
            int responseCode=connection.getResponseCode();
            if(responseCode!=HttpURLConnection.HTTP_OK){
                throw new IOException("HTTP error code: "+responseCode);
            }

            inputStream=connection.getInputStream();
            publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS);

            String outputDirectory = filePath;
            Log.i(TAG, "httpDownloadFile Directory: "+outputDirectory);
            String[] fileName=url.toString().split("/");
            Log.i(TAG, "httpDownloadFile Name: "+fileName[fileName.length-1]);

            outputStream=new FileOutputStream(new File(outputDirectory,fileName[fileName.length-1]));
            Log.i(TAG, "httpDownloadFile: open output stream success");

            long fileLength=connection.getContentLength();
            Log.i(TAG, "httpDownloadFile: file length "+fileLength);
            long byteStored=0;
            //TODO: check if local/external storage have enough available space

            //TODO: check if file with same name already exists
            publishProgress(DownloadCallback.Progress.GET_OUTPUT_STREAM_SUCCESS);
            if (inputStream!=null){
                //Start reading from it and write to output stream
                //
                byte[] b=new byte[500];
                int readLen=0;
                publishProgress(DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS);
                //set threshold to report back to UI (10% step)
                double report_threshold=0.1;
                while ((readLen=inputStream.read(b))!=-1){
                    if (readLen>0){
                        outputStream.write(b,0,readLen);
                        byteStored+=readLen;
                        //calculate the progress
                        if(byteStored>=report_threshold*fileLength)
                        {
                            int progress = (int)((byteStored/(float)fileLength)*100);
                            Log.i(TAG, String.format("Download Progress:%d(%d/%d)",progress,byteStored,fileLength));
                            //publish progress
                            publishProgress(DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS
                                    ,progress);
                            report_threshold+=0.1;
                        }
                    }
                }
            }
            publishProgress(DownloadCallback.Progress.DOWNLOAD_SUCCESS);

        }finally {
            Log.i(TAG, "httpDownloadFile: release resrource");
            if(connection!=null){
                connection.disconnect();
            }
            if (inputStream!=null) {
                inputStream.close();
            }
            if (outputStream!=null){
                outputStream.close();
            }
        }
    }

    /**
     * Download using http Client: OkHttp
     * OkHttp perseveres when the network is troublesome: it will silently recover from common
     * connection problems. If your service has multiple IP addresses OkHttp will attempt alternate
     * addresses if the first connect fails. This is necessary for IPv4+IPv6 and for services
     * hosted in redundant data centers. OkHttp initiates new connections with modern TLS features
     * (SNI, ALPN), and falls back to TLS 1.0 if the handshake fails.
     **/
    private void okHttpDownloadFile(URL param) {

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
