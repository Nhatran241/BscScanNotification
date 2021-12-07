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
                        Transaction transaction = new Transaction();
                        transaction.setTxn(accessibilityNodeInfo.getChild(0).getText().toString());
                        transaction.setMethod(accessibilityNodeInfo.getChild(1).getText().toString());
                        transaction.setAge(accessibilityNodeInfo.getChild(2).getText().toString());
                        transaction.setFrom(accessibilityNodeInfo.getChild(3).getText().toString());
                        transaction.setTo(accessibilityNodeInfo.getChild(5).getText().toString());
                        double quantity = 0;
                        try {
                            quantity = Double.parseDouble(accessibilityNodeInfo.getChild(6).getText().toString());
                        } catch (Exception e) {

                        }
                        transaction.setQuantity(quantity);
                        if (!transactions.contains(transaction)) {
                            transactions.add(transaction);
                        }

                    }
                } catch (Exception e) {

                }
                processPrediction(transactions);
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
        String url = "https://bscscan.com/token/0x7130d2a12b9bcbfae4f2634d864a1ee1ce3ead9c?" + System.currentTimeMillis();
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        myIntent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }

    private void processPrediction(List<Transaction> listTransaction) {
        isProcessing = true;
        List<Transaction> listTransactionShark = new ArrayList<>();
        List<Transaction> listTransactionWhale = new ArrayList<>();
        for (Transaction transaction : listTransaction) {
            if ((transaction.getQuantity() > 10 && !transaction.isNoty())) {
                transaction.setNoty(true);
                listTransactionWhale.add(transaction);
                continue;
            }

            if ((transaction.getQuantity() > 1 && !transaction.isNoty())) {
                transaction.setNoty(true);
                listTransactionShark.add(transaction);
            }
        }

        if (listTransactionWhale.size() > 0) {
            String messages = getSlackMessages(listTransactionWhale);
            SlackLogUtils.getInstance(this).sendSlackNotification(messages, SlackLogUtils.WHALE_URL, isSuccess -> {
                if (listTransactionShark.size() > 0) {
                    String messagesShark = getSlackMessages(listTransactionShark);
                    SlackLogUtils.getInstance(this).sendSlackNotification(messagesShark, SlackLogUtils.SHARK_URL, isSuccessShark -> {
                        while (listTransaction.size() > 30) {
                            listTransaction.remove(0);
                        }
                        openBscScanFromChrome();
                    });
                } else {
                    while (listTransaction.size() > 30) {
                        listTransaction.remove(0);
                    }
                    openBscScanFromChrome();
                }
            });
        } else if (listTransactionShark.size() > 0) {
            String messagesShark = getSlackMessages(listTransactionShark);
            SlackLogUtils.getInstance(this).sendSlackNotification(messagesShark, SlackLogUtils.SHARK_URL, isSuccessShark -> {
                while (listTransaction.size() > 30) {
                    listTransaction.remove(0);
                }
                openBscScanFromChrome();
            });
        } else {
            while (listTransaction.size() > 30) {
                listTransaction.remove(0);
            }
            openBscScanFromChrome();
        }
    }

    private boolean transferBinance(Transaction transaction) {
        return (transaction.getFrom().contains("Binance") || transaction.getTo().contains("Binance")) && transaction.getQuantity() > 0.1;
    }

    private String getSlackMessages(List<Transaction> listTransactionNoty) {
        String blocks = "{\"text\": \"";

        for (Transaction transaction : listTransactionNoty) {
            String from = transaction.getFrom().length() > 7 ? transaction.getFrom().substring(0, 6) : transaction.getFrom();
            String to = transaction.getTo().length() > 7 ? transaction.getTo().substring(0, 6) : transaction.getTo();
            String name = transaction.getTag();
            String total = String.valueOf(transaction.getQuantity());
            total = total.length() > 8 ? total.substring(0, 7) : total;
            String messages = "*%s* `%s` *From* `%s` *To* `%s`";
            blocks += String.format(messages, name, total, from, to);
            blocks += ('\n');
        }
        return blocks +"\"}";
    }

    private Transaction getBscTransaction(Element element) {
        Transaction transaction = new Transaction();
        Elements elements = element.getElementsByTag("a");
        transaction.setTxn(elements.get(0).text());
        transaction.setFrom(elements.get(1).text());
        transaction.setTo(elements.get(2).text());

        Elements date = element.getElementsByClass("showAge");
        transaction.setAge(date.get(0).getElementsByTag("span").text());

        Elements tds = element.getElementsByTag("td");
        transaction.setQuantity(Double.parseDouble(tds.get(tds.size() - 2).text()));
        return transaction;
    }
}
