package com.trailsi.malmo.android;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import com.google.android.gms.ads.MobileAds;
import com.trailsi.malmo.android.common.cache.CachedData;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.controller.service.SyncService;
import com.trailsi.malmo.android.controller.service.TempForegroundService;

public class App extends Application {

    private static Context gContext;
    private static AppOpenManager appOpenManager;
    @Override
    public void onCreate() {
        super.onCreate();


        MobileAds.initialize(
                this,
                initializationStatus -> {
                });
        appOpenManager = new AppOpenManager(this);
        gContext = getApplicationContext();
        CachedData.init(gContext);
        DBManager.init(gContext);
    }



    @Override
    protected void attachBaseContext(@NonNull Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void startService(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(gContext, TempForegroundService.class);
            intent.putExtra("cmd", command);
            gContext.startForegroundService(intent);
        } else {
            Intent intent = new Intent(gContext, SyncService.class);
            intent.putExtra("cmd", command);
            gContext.startService(intent);
        }
    }

  }