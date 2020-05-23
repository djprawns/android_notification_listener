package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * AndroidNotificationListenerPlugin
 */
public class AndroidNotificationListenerPlugin implements MethodCallHandler {

    private final MethodChannel channel;
    private Activity activity;


    private AndroidNotificationListenerPlugin(Activity activity, MethodChannel channel) {
        this.activity = activity;
        this.channel = channel;
        this.channel.setMethodCallHandler(this);
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "android_notification_listener");
        channel.setMethodCallHandler(new AndroidNotificationListenerPlugin(registrar.activity(), channel));
    }

    @SuppressLint("NewApi")
    @Override
    public void onMethodCall(MethodCall call, Result result) {

        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "isPermissionGranted":
                if (Settings.Secure.getString(activity.getContentResolver(), "enabled_notification_listeners").contains(activity.getPackageName())) {
                    result.success(true);
                } else {
                    result.success(false);
                }
                break;
            case "askPermission":
                activity.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                result.success(null);
                break;
            case "startListener":
                NotificationListener.setBackgroundChannel(channel);
                activity.startService(new Intent(activity, NotificationListener.class));
                result.success(true);
                break;
            default:
                result.notImplemented();
        }
    }
}
