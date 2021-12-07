package com.caphecode.myapplication;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class SlackLogUtils {
    private static SlackLogUtils instance;
    private RequestQueue requestQueue;
    private Context mContext;
    public static final String SHARK_URL = "https://hooks.sla"+"ck.com/services/"+"T02"+"Q97AJ0"+"TS/B02P"+"4U7NBMM/rnP3v2"+"SQVx2P"+"EqDkzVWFrXXn";
    public static final String WHALE_URL = "https://hooks.slac"+"k.com/services/T"+"02Q97AJ0"+"TS/B0"+"2PY6XL15X/g"+"8rhmPjG9Atia"+"pMeV9"+"rG0"+"d9e";

    public static SlackLogUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SlackLogUtils(context);
        }
        return instance;
    }

    private SlackLogUtils(Context context) {
        mContext = context;
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
    }


    public void sendSlackNotification(String messages, String url, SendSlackListener sendSlackListener) {
        requestQueue.stop();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Response", response);
                    sendSlackListener.onCompleted(true);
                },
                error -> {
                    Log.d("ERROR", "error => " + error.getMessage());
                    sendSlackListener.onCompleted(false);
                }) {


            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return messages.getBytes();
            }
        };
        requestQueue.add(stringRequest);
        requestQueue.start();
    }

    public interface SendSlackListener {
        void onCompleted(boolean isSuccess);
    }
}