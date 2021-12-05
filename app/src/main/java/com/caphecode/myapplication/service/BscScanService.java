package com.caphecode.myapplication.service;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.caphecode.myapplication.SlackLogUtils;
import com.caphecode.myapplication.model.SlackLogModel;
import com.caphecode.myapplication.model.Transaction;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
                            Log.d("nhatnhat", transaction.getTxn() + "/" + transaction.getMethod() + "/" + transaction.getAge() + "/" + transaction.getFrom() + "/" + transaction.getTo() + "/" + transaction.getQuantity());
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
    }

    private void openBscScanFromChrome() {
        isProcessing = false;
        String url = "https://bscscan.com/token/0x7083609fce4d1d8dc0c979aab8c869ea2c873402?" + System.currentTimeMillis();
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        myIntent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
//        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }

    private void processPrediction(List<Transaction> listTransaction) {
        isProcessing = true;
        List<Transaction> listTransactionNoty = new ArrayList<>();
        for (Transaction transaction : listTransaction) {
            if (transaction.getQuantity() > 1 && !transaction.isNoty()) {
                transaction.setNoty(true);
                listTransactionNoty.add(transaction);
            }
        }

        if (listTransactionNoty.size() > 0) {
            String slackMessages = getSlackMessages(listTransactionNoty);
            SlackLogModel slackLogModel = new SlackLogModel("DOT", slackMessages);
            SlackLogUtils.getInstance(this).sendSlackNotification(slackLogModel, isSuccess -> {
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

    private String getSlackMessages(List<Transaction> listTransactionNoty) {
        StringBuilder messages = new StringBuilder();
        for (Transaction transaction : listTransactionNoty) {
            messages.append(transaction.getMethod());
            messages.append(" : ");
            messages.append(transaction.getQuantity());
            messages.append("\n");
        }
        return messages.toString();
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
