package com.easyplan.mobileapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.Bundle;
import java.io.*;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.*;
import java.util.*;
import org.json.*;

/**
 * Created by André Augusto Tissot on 15/10/16.
 */

public class EasyplanService extends IntentService {
    public EasyplanService() {
        super("EasyplanService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Log.e("EasyplanService", "onHandleIntent called");
        this.runActivity(workIntent);
    }

    public void postRequest(
        String serverUrl,
        String refreshToken,
        String requestUrl,
        JSONObject requestJson
    ) throws Exception {
       StringBuilder result = new StringBuilder();
       // TODO: get this from the config
       URL url = new URL(serverUrl + requestUrl);
       HttpURLConnection conn = (HttpURLConnection) url.openConnection();
       conn.setRequestMethod("POST");
       conn.setRequestProperty("Authorization", "Bearer " + refreshToken);
       conn.setRequestProperty("Accept", "application/json");
       conn.setRequestProperty("Content-Type", "application/json");
       OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
       wr.write(requestJson.toString());
       wr.flush();
       wr.close();

       BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
       rd.close();
    }

    public void runActivity(Intent intent)
    {
        final EasyplanService that = this;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(that.getApplicationContext());
                String serverUrl = preferences.getString("serverUrl", null);
                String refreshToken = preferences.getString("refreshToken", null);

                try {
                    JSONObject actionDetails = new JSONObject(intent.getStringExtra("actionDetails"));
                    String action = actionDetails.getString("action");
                    String requestUrl = actionDetails.getString("requestUrl");
                    JSONObject requestJson = actionDetails.getJSONObject("requestJson");
                    if (action.equals("POST_REQUEST")) {
                        that.postRequest(serverUrl, refreshToken, requestUrl, requestJson);
                    }
                    boolean dismissOnComplete = actionDetails.getBoolean("dismissOnComplete");
                    if (dismissOnComplete) {
                        int notificationId = intent.getIntExtra("notificationId", -1);
                        NotificationManager notificationManager
                            = (NotificationManager) that.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(notificationId);
                    }
                } catch (Exception e) {
                    Log.e("EasyplanService", "exception thrown in test()");
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
