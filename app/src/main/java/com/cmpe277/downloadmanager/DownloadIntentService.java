package com.cmpe277.downloadmanager;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadIntentService extends IntentService {
    static final String TAG="MyIntentDownloadService";
    ResultReceiver receiver=null;
    public DownloadIntentService() {
        super("DownloadIntentService");
    }
    String filePath="/data/user/0/com.cmpe277.downloadmanager/files";


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            receiver = intent.getParcelableExtra("receiver");
        }

        URL url=intent.getParcelableExtra("url");

        if (receiver!=null&&url!=null){
            try {
                httpDownloadFile(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
            onProgressUpdate(DownloadTask.DownloadCallback.Progress.CONNECT_SUCCESS);
            int responseCode=connection.getResponseCode();
            if(responseCode!=HttpURLConnection.HTTP_OK){
                throw new IOException("HTTP error code: "+responseCode);
            }

            inputStream=connection.getInputStream();

            onProgressUpdate(DownloadTask.DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS);
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
            onProgressUpdate(DownloadTask.DownloadCallback.Progress.GET_OUTPUT_STREAM_SUCCESS);
            if (inputStream!=null){
                //Start reading from it and write to output stream
                //
                byte[] b=new byte[500];
                int readLen=0;
                onProgressUpdate(DownloadTask.DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS);
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
                            onProgressUpdate(DownloadTask.DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS
                                    ,progress);
                            report_threshold+=0.1;
                        }
                    }
                }
            }
            onProgressUpdate(DownloadTask.DownloadCallback.Progress.DOWNLOAD_SUCCESS);

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

    public void onProgressUpdate(int progressCode) {
        onProgressUpdate(progressCode,0);
    }
    public void onProgressUpdate(int progressCode, int percentComplete) {

        switch(progressCode) {
            // You can add UI behavior for progress updates here.
            case DownloadTask.DownloadCallback.Progress.ERROR:
                break;
            case DownloadTask.DownloadCallback.Progress.CONNECT_SUCCESS:
                break;
            case DownloadTask.DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case DownloadTask.DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS:
                broadcastDownloadProgress("update percentage",percentComplete);
                break;
            case DownloadTask.DownloadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
            case DownloadTask.DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case DownloadTask.DownloadCallback.Progress.DOWNLOAD_SUCCESS:
                break;

        }
    }

    public void broadcastDownloadProgress(String message, int percentComplete){
        Bundle downloadProgressUpdate=new Bundle();
        downloadProgressUpdate.putString("message",message);
        downloadProgressUpdate.putInt("code"
                , DownloadTask.DownloadCallback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS);
        downloadProgressUpdate.putInt("percentComplete",percentComplete);
        receiver.send(1,downloadProgressUpdate);
    }
}