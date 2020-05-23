package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

@SuppressLint({"NewApi", "Registered"})
public class NotificationListener extends NotificationListenerService {

    private static MethodChannel channel;

    public static void setBackgroundChannel(MethodChannel channel) {
        NotificationListener.channel = channel;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (channel != null) {
            Notification notification = sbn.getNotification();
//            System.out.println(notification.toString());
            if (notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
                Bundle extras = NotificationCompat.getExtras(notification);
                System.out.println(extras.toString());

                for(String key : extras.keySet()) {
                    Object obj = extras.get(key);   //later parse it as per your required type
                    System.out.println(key + " : " + String.valueOf(obj));
                    if (key.equals("android.title")) {
                        System.out.println(key + " : " + String.valueOf(obj));
                    }
                }
            }
            List<String> obj = new ArrayList<>();

            obj.add(sbn.getPackageName());
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_SUB_TEXT));

            channel.invokeMethod("onNotificationPosted", obj);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }


}
