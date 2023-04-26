package com.trailsi.malmo.android.controller.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.trailsi.malmo.android.R;
import com.trailsi.malmo.android.common.Constant;
import com.trailsi.malmo.android.common.cache.CachedData;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.common.utils.DeviceUtil;
import com.trailsi.malmo.android.common.utils.StringHelper;
import com.trailsi.malmo.android.model.Audio;
import com.trailsi.malmo.android.model.Document;
import com.trailsi.malmo.android.model.LocalFile;
import com.trailsi.malmo.android.model.Location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SyncService extends Service {

    public final static String TAG = "SyncService";

    private List<String> downloadFiles;
    private int downloadedCount;

    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initVariable();
    }

    public void initVariable() {
        downloadFiles = new ArrayList<>();
        downloadedCount = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String cmd = StringHelper.getNotNullString(intent.getStringExtra("cmd"));
            switch (cmd) {
                case Constant.DOWNLOAD_FILE:
                    downloadFiles();
                    break;

                case Constant.DOWNLOAD_AUDIO:
                    downloadAudios();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    synchronized void downloadFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendStatus(STATUS_START, ""); // Sync Start

                downloadFiles.clear();
                downloadedCount = 0;

                // Add Place Image
                String placeMapImage = CachedData.getString(CachedData.kPlaceMapImage, "");
                if (!StringHelper.isEmpty(placeMapImage)) {
                    checkLocalFile(placeMapImage);
                }

                // Add Locations
                List<Location> locations = new ArrayList<>();
                DBManager.getInstance().getLocations(locations);
                for (Location location : locations) {
                    if (!StringHelper.isEmpty(location.image)) {
                        checkLocalFile(location.image);
                    }
                }

                // Add Documents
                List<Document> documents = new ArrayList<>();
                DBManager.getInstance().getDocuments(documents);
                for (Document document : documents) {
                    if (!StringHelper.isEmpty(document.file)) {
                        checkLocalFile(document.file);
                    }
                }

                // Add PlaceInfo
                String placeInfoImage = CachedData.getString(CachedData.kPlaceInfoImage, "");
                if (!StringHelper.isEmpty(placeInfoImage)) {
                    checkLocalFile(placeInfoImage);
                }

                if (downloadFiles.size() > 0) {
                    downloadAllFiles(downloadFiles.size());
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendStatus(STATUS_FINISH_FILE, ""); // Sync Finished
            }
        }).start();
    }

    synchronized void downloadAudios() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendStatus(STATUS_START, ""); // Sync Start

                downloadFiles.clear();
                downloadedCount = 0;

                // Add Locations
                List<Location> locations = new ArrayList<>();
                DBManager.getInstance().getLocations(locations);
                for (Location location : locations) {
                    for (Audio audio : location.audios) {
                        if (!StringHelper.isEmpty(audio.audio)) {
                            checkLocalFile(audio.audio);
                        }
                    }
                }

                if (downloadFiles.size() > 0) {
                    downloadAllFiles(downloadFiles.size());
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendStatus(STATUS_FINISH_AUDIO, ""); // Sync Finished
            }
        }).start();
    }

    private void checkLocalFile(String cloudPath) {
        LocalFile localFile = DBManager.getInstance().getLocalFile(cloudPath);
        if (localFile == null) {
            downloadFiles.add(cloudPath);
            return;
        }

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), localFile.local_file);
        if (!file.exists()) {
            DBManager.getInstance().deleteLocalFile(localFile);
            downloadFiles.add(cloudPath);
        }
    }

    private void downloadAllFiles(int count) {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService mExecutor = Executors.newFixedThreadPool(processors);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    boolean loop = false;
                    int retry = 0;
                    do {
                        loop = downloadOneFile(finalI);
                        if (loop) {
                            retry++;
                            if (retry > 2) loop = false;
                            Log.d(TAG, "Count: " + finalI + " FAILED, Retry: " + retry);
                        } else {
                            Log.d(TAG, "Count: " + finalI + " Success");
                        }
                    } while (loop);
                }
            });
        }

        mExecutor.shutdown();
        boolean loop = false;
        while (!loop) {
            try {
                loop = mExecutor.awaitTermination(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                loop = true;
            }
        }
    }

    boolean downloadOneFile(final int index) {
        String error = "";
        boolean retry = false;
        try {
            String cloudPath = downloadFiles.get(index);
            Log.e(TAG, "Downloading: " + index + " - " + cloudPath);
            URL url = new URL(cloudPath);
            URLConnection c = url.openConnection();

            String localFileName = DeviceUtil.getUUID();
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), localFileName);
            file.mkdirs();
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            InputStream is = c.getInputStream();
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int count;

            while ((count = is.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
            fos.close();
            is.close();

            LocalFile localFile = new LocalFile(cloudPath, localFileName);
            DBManager.getInstance().addLocalFile(localFile);
            downloadedCount++;
        } catch (FileNotFoundException fnfe) {
            error = getString(R.string.cannot_found_file);
            retry = true;
        } catch (IOException e) {
            error = e.getMessage();
            retry = true;
        }
        sendStatus(STATUS_DOWNLOAD, error);
        return retry;
    }

    public static final int STATUS_START = 0;
    public static final int STATUS_DOWNLOAD = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_FINISH_FILE = 3;
    public static final int STATUS_FINISH_AUDIO = 4;

    private void sendStatus(int cmd, String error) {
        int percent = 0;
        if (downloadFiles.size() > 0) {
            percent = (downloadedCount * 100) / downloadFiles.size();
        }
        Log.e(TAG, "CMD: " + cmd + ", Percent: " + percent + ", Error: " + error);
        Intent intentBroadcast = new Intent(Constant.ACTION_FILE_DOWNLOAD_STATUS);
        intentBroadcast.putExtra("command", cmd);
        intentBroadcast.putExtra("percent", percent);
        intentBroadcast.putExtra("error", error);
        sendBroadcast(intentBroadcast);
    }
}
