package com.cmpe277.downloadmanager;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class DownloadIntentService extends IntentService {
    static final String TAG="MyIntentDownloadService";
    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ResultReceiver receiver = intent.getParcelableExtra("receiver");
            Integer i=0;
            for(i =0;i<5;i++){
                try {
                    Thread.sleep(2000);
                    Log.d(TAG, "Thread counter:"+i);
                    Bundle bunder = new Bundle();
                    bunder.putString("message","Thread counter:"+i);
                    receiver.send(1,bunder);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Finish running task with thread counter: "+i);

            receiver.send(1,new Bundle());
        }
    }

}