package com.caphecode.myapplication.service;

import static android.content.Intent.ACTION_TIME_TICK;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.caphecode.myapplication.SlackLogUtils;
import com.caphecode.myapplication.model.SlackLogModel;
import com.caphecode.myapplication.model.Transaction;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nhatran241 on 12/5/2021.
 * trannhat2411999@gmail.com
 */
public class BscScanService extends AccessibilityService {
    private boolean isProcessing = false;
    List<Transaction> transactions = new ArrayList<>();


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                listenerContentChangedEvents(getRootInActiveWindow(), 0);
            }
        }
    }

    public void listenerContentChangedEvents(AccessibilityNodeInfo nodeInfo, int depth) {
        if (nodeInfo == null && isProcessing) return;
        processBSCData(nodeInfo, depth);
    }

    private boolean isChromeRunning() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals("com.android.chrome")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void processBSCData(AccessibilityNodeInfo nodeInfo, int depth) {
        if (nodeInfo != null) {
            if (depth == 20 && nodeInfo.getClassName().equals("android.widget.GridView")) {
                int childCount = nodeInfo.getChildCount();
                try {

                    for (int i = 1; i < childCount; i++) {
                        AccessibilityNodeInfo accessibilityNodeInfo = nodeInfo.getChild(i);


                    }
                } catch (Exception e) {

                }
                Log.d(this.getPackageName(), nodeInfo.getText() + " " + nodeInfo.getContentDescription() + " | " + "" + nodeInfo.getClassName() + " | " + nodeInfo.getPackageName());
            }
            for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
                processBSCData(nodeInfo.getChild(i), depth + 1);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        openBscScanFromChrome();
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                openBscScanFromChrome();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TIME_TICK);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void openBscScanFromChrome() {
        isProcessing = false;
        String url = "https://shopeefood.vn/ho-chi-minh/ministop-s115-hung-gia";
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        myIntent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }

}
