package net.learn2develop.Services;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import android.os.Binder;
import android.os.IBinder;

public class MyService extends Service {
	int counter = 0;
	public URL[] urls;

	static final int UPDATE_INTERVAL = 1000;
	private Timer timer = new Timer();

	private final IBinder binder = new MyBinder();

	public class MyBinder extends Binder {
		MyService getService() {
			return MyService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		//return null;
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        /*
        try {
			int result = DownloadFile(new URL("http://www.amazon.com"));
			Toast.makeText(getBaseContext(), "Downloaded " + result + " bytes", Toast.LENGTH_LONG).show();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        */

        /*
        try {
			new DoBackgroundTask().execute(
					new URL("http://www.amazon.com/somefiles.pdf"),
					new URL("http://www.wrox.com/somefiles.pdf"),
					new URL("http://www.google.com/somefiles.pdf"),
					new URL("http://www.learn2develop.net/somefiles.pdf"));

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

        //doSomethingRepeatedly();

        Object[] objUrls = (Object[]) intent.getExtras().get("URLs");
        URL[] urls = new URL[objUrls.length];
        for (int i=0; i<objUrls.length-1; i++) {
        	urls[i] = (URL) objUrls[i];
        }
    	new DoBackgroundTask().execute(urls);

		return START_STICKY;
	}

	private void doSomethingRepeatedly() {
		timer.scheduleAtFixedRate( new TimerTask() {
			public void run() {
                Log.d("MyService", String.valueOf(++counter));
                try {
					Thread.sleep(4000);
	                Log.d("MyService", counter + " Finished");

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 0, UPDATE_INTERVAL);
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null){
        	timer.cancel();
        }
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

	private int DownloadFile(URL url) {
		try {
			//---simulate taking some time to download a file---
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 100;
	}

	public void downloadFile(String fileURL, String fileName) {

     StatFs stat_fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
     double avail_sd_space = (double)stat_fs.getAvailableBlocks() *(double)stat_fs.getBlockSize();
     //double GB_Available = (avail_sd_space / 1073741824);
     double MB_Available = (avail_sd_space / 10485783);
     //System.out.println("Available MB : " + MB_Available);
     Log.d("MB",""+MB_Available);
    try {
         File root =new File(Environment.getExternalStorageDirectory()+"/vvveksperten");
         if(root.exists() && root.isDirectory()) {

         }else{
             root.mkdir();
         }
         Log.d("CURRENT PATH",root.getPath());
         URL u = new URL(fileURL);
         HttpURLConnection c = (HttpURLConnection) u.openConnection();
         c.setRequestMethod("GET");
         c.setDoOutput(true);
         c.connect();
           int fileSize  = c.getContentLength()/1048576;
           Log.d("FILESIZE",""+fileSize);
           if(MB_Available <= fileSize ){
                this.showNotification(getResources().getString(R.string.notification_no_memory),getResources().getString(R.string.notification_error));
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
         if(file.exists()){
             file.delete();
             Log.d("FILE-DELETE","YES");
         }else{
             Log.d("FILE-DELETE","NO");
         }
         File from =new File(root.getAbsolutePath() + "/" + fileName);
         File to = new File(root.getAbsolutePath() + "/" + "some.pdf");


         } catch (Exception e) {
         Log.d("Downloader", e.getMessage());

     }

	private class DoBackgroundTask extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            int count = urls.length;
            long totalBytesDownloaded = 0;
            for (int i = 0; i < count; i++) {
                totalBytesDownloaded += DownloadFile(urls[i]);
                //---calculate precentage downloaded and
                // report its progress---
                publishProgress((int) (((i+1) / (float) count) * 100));
            }
            return totalBytesDownloaded;
        }

        protected void onProgressUpdate(Integer... progress) {
        	Log.d("Downloading files",
        			String.valueOf(progress[0]) + "% downloaded");
        	Toast.makeText(getBaseContext(),
        			String.valueOf(progress[0]) + "% downloaded",
        			Toast.LENGTH_LONG).show();
        }

        protected void onPostExecute(Long result) {
        	Toast.makeText(getBaseContext(),
        			"Downloaded " + result + " bytes",
        			Toast.LENGTH_LONG).show();
        	stopSelf();
        }
	}
}
