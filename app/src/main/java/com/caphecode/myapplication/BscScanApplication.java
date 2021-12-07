package com.caphecode.myapplication;

import android.app.Application;
import android.content.Intent;

import com.caphecode.myapplication.service.BscScanService;

public class BscScanApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
//        SlackLogUtils.getInstance(this).sendSlackNotification("ERROR", e.getMessage(), SlackLogUtils.SHARK_URL, null);
        startService(new Intent(this, BscScanService.class));
    }
}