package warrocker.vkmusicdownloader.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;

import warrocker.vkmusicdownloader.R;

public class DownloadService extends Service {
    ArrayDeque<JSONObject> itemQueue = new ArrayDeque<>();
    int id = 1;
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    Handler toastHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Toast.makeText(DownloadService.this, (String) message.obj , Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void onCreate() {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(DownloadService.this);
        mBuilder.setContentTitle("File Download")
                .setSmallIcon(R.drawable.ic_button_download);
        new DownloadThread().execute();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
    public void downloadFile(JSONObject audioItemForDownload){
        addItemToQueue(audioItemForDownload);
    }
    private void addItemToQueue(JSONObject audioItemForDownload){
        itemQueue.addLast(audioItemForDownload);
        if(itemQueue.size() > 1) {
        mBuilder.setContentTitle("Download " + (itemQueue.size() + 1) + " files");
        }else{
            mBuilder.setContentTitle("Download file");
        }
        mNotifyManager.notify(id, mBuilder.build());
    }

    private class DownloadThread implements Runnable{
        Thread t;
        private DownloadThread(){
            t = new Thread(this);
        }
        void execute() {
            t.start();
        }
        @Override
        public void run() {
            while (!t.isInterrupted()) {
                while (itemQueue.peek() !=null) {
                    if(itemQueue.size() > 1) {
                        mBuilder.setContentTitle("Download " + (itemQueue.size()) + " files");
                    }else{
                        mBuilder.setContentTitle("Download file");
                    }
                    mNotifyManager.notify(id, mBuilder.build());
                    JSONObject audioItem = itemQueue.pop();
                    loadFromUrlToFile(audioItem);
                }
            }
        }


        private int getFileSize(URL url) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                return conn.getContentLength();
            } catch (IOException e) {
                return -1;
            } finally {
                if(conn != null)
                    conn.disconnect();
            }
        }
        private void loadFromUrlToFile(JSONObject audioItem) {
            URL downloadURL = null;
            int fileLenght;
            NotifyThread notificationThread;
            String titleText = "";
            if (audioItem != null) {
                try {
                    if(itemQueue.size() ==0) {
                        titleText = audioItem.getString("artist") + " - " + audioItem.getString("title");
                    } else {
                        titleText = audioItem.getString("artist") + " - " + audioItem.getString("title");
                    }
                    downloadURL = new URL(audioItem.getString("url"));
                } catch (MalformedURLException e) {
                    Message message = toastHandler.obtainMessage(1, "Unknown address");
                    message.sendToTarget();
                } catch (JSONException e) {
                    Message message = toastHandler.obtainMessage(1, "File error");
                    message.sendToTarget();
                }

                mBuilder.setContentText(titleText);
                fileLenght = getFileSize(downloadURL);
                notificationThread = new NotifyThread(0, fileLenght);
                if (downloadURL != null) {
                    try (BufferedInputStream in = new BufferedInputStream(downloadURL.openStream());
                         FileOutputStream fio = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Music/" + titleText + ".mp3")) {
                        byte[] byteArray = new byte[8192];
                        int count;
                        int currentProgress = 0;
                        while ((count = in.read(byteArray)) != -1) {
                            fio.write(byteArray, 0, count);
                            currentProgress = currentProgress + (count);
                            notificationThread.updateProgress(currentProgress);
                        }
                        notificationThread.setInterrupted();
                        mBuilder.setContentTitle("Download complete")
                                // Removes the progress bar
                                .setProgress(0, 0, false);
                        mNotifyManager.notify(id, mBuilder.build());
                    } catch (IOException e) {
                        Message message = toastHandler.obtainMessage(1, "File error");
                        message.sendToTarget();
                    }
                }
            }
        }
    }
    class NotifyThread implements Runnable{
        private int progress;
        private int maxProgress;
        boolean interrupted = false;
        Thread t;
        NotifyThread(int progress, int maxProgress){
            this.progress = progress;
            this.maxProgress = maxProgress;
            t = new Thread(this);
            t.start();

        }

        void updateProgress(int progress) {
            this.progress = progress;
        }

        private void setInterrupted() {
            t.interrupt();
        }

        @Override
        public void run() {
            while (!interrupted) {
                mBuilder.setProgress(maxProgress, progress, false);
                mNotifyManager.notify(id, mBuilder.build());
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
    }
}