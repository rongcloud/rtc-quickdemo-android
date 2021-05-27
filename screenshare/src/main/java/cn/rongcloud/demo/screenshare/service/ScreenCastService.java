/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.screenshare.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import cn.rongcloud.demo.screenshare.R;
import cn.rongcloud.demo.screenshare.ui.activity.MeetingActivity;

public class ScreenCastService extends Service {


    private final String TAG = ScreenCastService.class.getName();
    private Binder myBinder = new MyBinder();

    @RequiresApi(api = VERSION_CODES.O)
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "----onBind----");
        createNotificationChannel();
        return myBinder;
    }

    private class MyBinder extends Binder {

    }

    @RequiresApi(api = VERSION_CODES.O)
    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this, MeetingActivity.class);

        builder
                .setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setContentTitle("屏幕共享中... ...")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel =
                new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId("notification_id");

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;

        startForeground(10241024, notification);

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "----onDestroy----");
        stopForeground(true);
        super.onDestroy();
    }
}
