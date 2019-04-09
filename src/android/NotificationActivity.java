package com.andretissot.firebaseextendednotification;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import com.gae.scaffolder.plugin.*;
import java.util.*;
import org.json.*;

/**
 * Created by André Augusto Tissot on 15/10/16.
 */

public class NotificationActivity extends FCMPluginActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if(extras == null)
            return;

        boolean dismissOnComplete = false;
        try {
            JSONObject actionDetails = new JSONObject(getIntent().getStringExtra("actionDetails"));
            dismissOnComplete = actionDetails.getBoolean("dismissOnComplete");
        } catch (Exception e) {
        }

        if (dismissOnComplete) {
            int notificationId = getIntent().getIntExtra("notificationId", -1);
            NotificationManager notificationManager
                = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("wasTapped", true);
        for (String key : extras.keySet()) {
            String value = extras.getString(key);
            data.put(key, value);
        }
        FirebaseExtendedNotification.setLastNotificationTappedData(data);
    }
}
