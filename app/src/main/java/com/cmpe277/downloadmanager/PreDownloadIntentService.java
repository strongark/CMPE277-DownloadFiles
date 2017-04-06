package com.cmpe277.downloadmanager;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class PreDownloadIntentService extends IntentService {
    static final String TAG="MyIntentDownloadService";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FOO = "com.cmpe277.downloadmanager.action.FOO";
    public static final String ACTION_BAZ = "com.cmpe277.downloadmanager.action.BAZ";

    // TODO: Rename parameters
    public static final String EXTRA_PARAM1 = "com.cmpe277.downloadmanager.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "com.cmpe277.downloadmanager.extra.PARAM2";

    public PreDownloadIntentService() {
        super("PreDownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_FOO.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionFoo(param1, param2);
//            } else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            }
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

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}