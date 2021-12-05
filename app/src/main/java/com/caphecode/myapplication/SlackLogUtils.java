package com.caphecode.myapplication;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.caphecode.myapplication.model.SlackLogModel;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SlackLogUtils {
    private static SlackLogUtils instance;
    private RequestQueue requestQueue;
    private Context mContext;
    private final String URL = "https://hooks.slack.com/services/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";

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

    public void sendSlackNotification(Object object, SendSlackListener sendSlackListener) {
        requestQueue.stop();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        sendSlackListener.onCompleted(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.getMessage());
                        sendSlackListener.onCompleted(false);
                    }
                }) {


            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return new Gson().toJson(object).getBytes();
            }
        };
        requestQueue.add(stringRequest);
        requestQueue.start();
    }

    public interface SendSlackListener {
        void onCompleted(boolean isSuccess);
    }
}