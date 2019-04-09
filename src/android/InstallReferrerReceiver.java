package com.easyplan.mobileapp;

import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import android.util.Log;

public class InstallReferrerReceiver extends BroadcastReceiver {

@Override
public void onReceive(Context context, Intent intent) {
    Log.e("InstallReferrerReceiver", "onReceive called");
    Bundle extras = intent.getExtras();
    if (extras != null) {
    	String referrerString = extras.getString("referrer");
        Log.e("InstallReferrerReceiver", "onReceive received string: " + referrerString);
        if (referrerString != null) {
    	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    	    Editor edit = sharedPreferences.edit();
    	    edit.putString("referrer", referrerString);
    	    edit.commit();
        }
    }
}

} // end of class
