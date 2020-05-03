package com.andretissot.firebaseextendednotification;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import java.util.*;
import org.json.*;

import com.android.installreferrer.api.*;
import com.google.firebase.analytics.FirebaseAnalytics;


/**
 * Created by André Augusto Tissot on 15/10/16.
 */

public class FirebaseExtendedNotification extends CordovaPlugin {
    static private Map<String, Object> lastNotificationTappedData;
    public static void setLastNotificationTappedData(Map<String, Object> notificationData){
        lastNotificationTappedData = notificationData;
    }
    public static Map<String, Object> getLastNotificationTappedData(){
        return lastNotificationTappedData;
    }

    private FirebaseAnalytics mFirebaseAnalytics = null;

    public static Bundle fromJson(JSONObject s) {
        Bundle bundle = new Bundle();

        for (Iterator<String> it = s.keys(); it.hasNext(); ) {
            String key = it.next();
            String str = s.optString(key);
            if (str != null) {
                bundle.putString(key, str);
            }
        }

        return bundle;
    }

    public boolean execute(final String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        Log.e("FirebaseExtendedNotification", "execute called");
        if (action.equals("saveRefreshToken")) {
            Log.e("FirebaseExtendedNotification", "execute action is saveRefreshToken");
            String refreshToken, serverUrl;
            try {
                serverUrl = args.getString(0);
                refreshToken = args.getString(1);
            } catch (JSONException e) {
                callbackContext.error(e.getMessage());
                return false;
            }
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity().getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("refreshToken", refreshToken);
            editor.putString("serverUrl", serverUrl);
            editor.commit();
            Log.e("FirebaseExtendedNotification", "Saved refreshToken successfully");
            callbackContext.success(new JSONObject());
        } else if (action.equals("getLastNotificationTappedData")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Map<String, Object> lastNotificationData
                            = FirebaseExtendedNotification.getLastNotificationTappedData();
                        if(lastNotificationData == null){
                            callbackContext.success(new JSONObject());
                        } else {
                            callbackContext.success(new JSONObject(lastNotificationData));
                        }
                    } catch (Exception e){
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        } else if (action.equals("closeNotification")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Manager manager = new Manager(cordova.getActivity());
                        int notificationId = args.getInt(0);
                        if(manager.notificationExists(notificationId)){
                            manager.cancelNotification(notificationId);
                            callbackContext.success(new JSONObject());
                        } else {
                            callbackContext.error("Notification not found with id="+notificationId);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        } else if (action.equals("notificationExists")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Manager manager = new Manager(cordova.getActivity());
                        if(manager.notificationExists(args.getInt(0)))
                            callbackContext.success(1);
                        else callbackContext.success(0);
                    } catch (Exception e){
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        } else if (action.equals("showNotification")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        JSONObject dataJSON = args.getJSONObject(0), optionsJSON = args.getJSONObject(1);
                        dataJSON.put("notificationOptions", optionsJSON);
                        new Manager(cordova.getActivity()).showNotification(dataJSON, optionsJSON);
                    } catch (Exception e){
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        } else if (action.equals("logEvent")) {
            Log.e("FirebaseExtendedNotification", "logEvent called");
            try {
                if (mFirebaseAnalytics == null) {
                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(
                        cordova.getActivity().getApplicationContext());
                }
                String eventName = args.getString(0);
                JSONObject params = args.getJSONObject(1);
                Bundle bundle = fromJson(params);
                mFirebaseAnalytics.logEvent(eventName, bundle);
                callbackContext.success(new JSONObject());
            } catch (Exception e) {
                Log.e("FirebaseExtendedNotification", "exception in logEvent " + e.getMessage());
                callbackContext.error(e.getMessage());
                return false;
            }
        } else if (action.equals("getReferrer")) {
            Log.e("FirebaseExtendedNotification", "getReferrer called");
            try {
                InstallReferrerClient referrerClient;
                referrerClient = InstallReferrerClient.newBuilder(cordova.getActivity().getApplicationContext()).build();
                referrerClient.startConnection(new InstallReferrerStateListener() {
                    @Override
                    public void onInstallReferrerSetupFinished(int responseCode) {
                        JSONObject callbackResponse = new JSONObject();
                        switch (responseCode) {
                            case InstallReferrerClient.InstallReferrerResponse.OK:
                                try {
                                    ReferrerDetails response = referrerClient.getInstallReferrer();
                                    callbackResponse.put("referrerUrl", response.getInstallReferrer());
                                    callbackResponse.put("referrerClickTime", response.getReferrerClickTimestampSeconds());
                                    callbackResponse.put("appInstallTime", response.getInstallBeginTimestampSeconds());
                                    callbackResponse.put("instantExperienceLaunched", response.getGooglePlayInstantParam());
                                    callbackContext.success(callbackResponse);
                                } catch (RemoteException e) {
                                    callbackContext.error("REMOTE_EXCEPTION");
                                } catch (JSONException e) {
                                    callbackContext.error("JSON_EXCEPTION");
                                }
                                referrerClient.endConnection();
                                break;
                            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                                // API not available on the current Play Store app.
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                                    cordova.getActivity().getApplicationContext());
                                if (sharedPref.contains("referrer")) {
                                    try {
                                        callbackResponse.put("referrerUrl", sharedPref.getString("referrer", ""));
                                        callbackContext.success(callbackResponse);
                                    } catch (JSONException e) {
                                        callbackContext.error("JSON_EXCEPTION");
                                    }
                                } else {
                                    callbackContext.error("FEATURE_NOT_SUPPORTED");
                                }
                                break;
                            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                                callbackContext.error("SERVICE_UNAVAILABLE");
                                break;
                        }
                    }

                    @Override
                    public void onInstallReferrerServiceDisconnected() {
                        callbackContext.error("SERVICE_DISCONNECTED");
                    }
                });
            } catch (Exception e) {
                Log.e("FirebaseExtendedNotification", "exception in getReferrer " + e.getMessage());
                callbackContext.error("GENERAL_EXCEPTION");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
