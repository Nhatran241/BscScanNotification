package com.caphecode.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebView;

import com.caphecode.myapplication.model.Transaction;
import com.caphecode.myapplication.service.BscScanService;

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

public class MainActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    finish();
                    startService(new Intent(MainActivity.this, BscScanService.class));
                });
        someActivityResultLauncher.launch(intent);

//        Observable.create(emitter -> {
//            emitter.onNext(emitter);
//            emitter.onComplete();
//        }).doOnNext(o -> {
//            crawlBscScanData();
//        }).doOnError(throwable -> Log.d("crawlBscScanData", throwable.getMessage()))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe();
    }



    private void crawlBscScanData() {
        try {
            String html = getHtml("https://bscscan.com/token/generic-tokentxns2?m=normal&contractAddress=0x7083609fce4d1d8dc0c979aab8c869ea2c873402&a=&sid=3048d37e696a4af88786553ec1ad9f65&p=1");
            Document document = Jsoup.parse(html);
            Element mainDiv = document.getElementById("mainDiv");
            Elements tables = mainDiv.getElementsByTag("table");
            List<Transaction> listTransaction = new ArrayList<>();
            if (!tables.isEmpty()) {
                Elements trs = tables.get(0).getElementsByTag("tr");
                for (Element element : trs) {
                    listTransaction.add(getBscTransaction(element));
                }
            }
            processPrediction(listTransaction);
        } catch (Exception e) {
            Log.d("crawlBscScanData", e.getMessage());
        }

    }

    public String getHtml(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet, localContext);
        String result = "";

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        response.getEntity().getContent()
                )
        );

        String line = null;
        while ((line = reader.readLine()) != null) {
            result += line + "\n";
        }
        return result;
    }


    private void processPrediction(List<Transaction> listTransaction) {
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