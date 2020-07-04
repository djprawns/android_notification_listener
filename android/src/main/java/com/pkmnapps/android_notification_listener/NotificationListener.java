package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
//import android.support.v4.app.NotificationCompat;
import androidx.core.app.NotificationCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.BundleData;
import data.Request;
import data.Scrobble;
import io.flutter.plugin.common.MethodChannel;

@SuppressLint({"NewApi", "Registered"})
public class NotificationListener extends NotificationListenerService {

    private static MethodChannel channel;

    private boolean same = false;

    private String title;

    private String subtitle;

    private String packageName;

    private String appName;

    public static void setBackgroundChannel(MethodChannel channel) {
        NotificationListener.channel = channel;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        same = true;
        if (channel != null) {
            Notification notification = sbn.getNotification();
            if (notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
                Bundle extras = NotificationCompat.getExtras(notification);

                BundleData bundleData = parseBundle(extras);

                if (bundleData.text != null) {
//                    System.out.println("Bundle Title " + bundleData.title);
//                    System.out.println("App Title " + title);
                    if (bundleData.title.equals(title)) {
//                        System.out.println("SAME");
                        same = true;
                    } else {
//                        System.out.println("DIFFERENT");
                        same = false;
                    }
                    title = bundleData.title;
                }
                if (bundleData.text != null) {
                    subtitle = bundleData.text;
                }
                if (packageName == null || !packageName.equals(sbn.getPackageName())) {
                    try {
                        appName = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA)).toString();
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }
                }
//                System.out.println("Title - " + title + " Album - " + subtitle + " App - " + appName);

                if (!same) {
                    System.out.println("Scrobbling");
                    System.out.println("Title - " + title + " Album - " + subtitle + " App - " + appName);
//                    System.out.println(extras.toString());
                    Scrobble scrobble = new Scrobble();
                    scrobble.title = title;
                    scrobble.album = subtitle;
//                    scrobble.appName = appName;
                    scrobble.app = appName.toUpperCase();
                    scrobble.platform = "ANDROID";
//                    Request requestPayload = new Request();
//                    requestPayload.request_payload = scrobble;
                    scrobble(scrobble);
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

    public BundleData parseBundle(Bundle bundle) {
        BundleData bundleData = new BundleData();
        for(String key : bundle.keySet()) {
            Object obj = bundle.get(key);   //later parse it as per your required type
            try {
                if (key.equals("android.title")) {
                    bundleData.title = obj.toString();
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            try {
                if (key.equals("android.subText")) {
                    bundleData.subText = obj.toString();
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            try {
                if (key.equals("android.text")) {
                    bundleData.text = obj.toString();
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return bundleData;
    }

    public void scrobble(Scrobble request) {

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("here");
                System.out.println(error.toString());
//                if (error instanceof NetworkError) {
//                    System.out.println("here");
//                    System.out.println(error.toString());
//                } else {
//                    System.out.println(error.toString());
//                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.43.62:8081/metadata_ms/scrobble";
        Gson gson = new Gson();
        String json = gson.toJson(request);
        JSONObject jsonObject;
        System.out.println("Making Request");
        try {
            jsonObject = new JSONObject(json);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, jsonObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
//                    VolleyLog.wtf(response.toString(), "utf-8");
                    System.out.println("Made Request");
//                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                }
            }, errorListener) {

                @Override
                public int getMethod() {
                    return Method.POST;
                }

                @Override
                public Priority getPriority() {
                    return Priority.NORMAL;
                }
            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        } catch (JSONException err) {
            System.out.println("Error in Request");
            Log.d("Error", err.toString());
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }


}
