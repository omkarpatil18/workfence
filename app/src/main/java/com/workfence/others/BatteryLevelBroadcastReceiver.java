package com.workfence.others;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.workfence.R;

public class BatteryLevelBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int percent = (level * 100) / scale;

            if (percent <= 15) {
                Notification notification;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    notification = new NotificationCompat.Builder(context, "LowBattery")
                            .setContentText("Make sure to charge your phone")
                            .setColorized(true)
                            .setSmallIcon(R.drawable.ic_notif)
                            .setContentTitle("Low Battery")
                            .setColor(context.getColor(R.color.red))
                            .setChannelId("batteryLevel")
                            .build();
                else
                    notification = new NotificationCompat.Builder(context, "LowBattery")
                            .setContentText("Make sure to charge your phone")
                            .setContentTitle("Low Battery")
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setSmallIcon(R.drawable.ic_notif)
                            .setColor(context.getColor(R.color.red))
                            .build();

                MyService.notificationManager.notify(2, notification);
            } else
                MyService.notificationManager.cancel(2);
        }
    }
}
