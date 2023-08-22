package com.workfence.others;

import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.workfence.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.workfence.others.MyService.notificationManager;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    public static long startTimer = -1;
    public static long endTimer;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("BroadcastActions", "Action " + action + "received");

        boolean isTimerOn = Utils.getBoolean(context, "isTimerOn", false);

        if (!isTimerOn)
            return;

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_OFF) {
                Log.d("BroadcastActions", "Bluetooth is off");
                startTimer = System.currentTimeMillis();
                Notification notif = new Notification();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    notif = new NotificationCompat.Builder(context, "SocDisServiceChannel")
                            .setContentText("Turn on bluetooth for workfence to work")
                            .setOnlyAlertOnce(true)
                            .setColorized(true)
                            .setSmallIcon(R.drawable.ic_notif)
                            .setColor(context.getColor(R.color.red))
                            .setContentTitle("Bluetooth is off!!")
                            .setChannelId("exampleServiceChannel")
                            .build();
                else
                    notif = new NotificationCompat.Builder(context, "SocDisServiceChannel")
                            .setContentText("Turn on bluetooth for workfence to work")
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(R.drawable.ic_notif)
                            .setColor(context.getColor(R.color.red))
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setContentTitle("Bluetooth is off!!")
                            .build();
                if (notificationManager != null)
                    notificationManager.notify(1, notif);
            } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                Log.d("BroadcastActions", "Bluetooth is turning off");
            } else if (state == BluetoothAdapter.STATE_ON) {
                Log.d("BroadcastActions", "Bluetooth is on");
                endTimer = System.currentTimeMillis();
                if (startTimer != -1) {
                    upload(context, endTimer - startTimer);
                    startTimer = -1;
                }
                Notification notif = new Notification();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    notif = new NotificationCompat.Builder(context, "SocDisServiceChannel")
                            .setContentText("No nearby devices with WorkFence app within 1.5 meters")
                            .setOnlyAlertOnce(true)
                            .setColorized(true)
                            .setSmallIcon(R.drawable.ic_notif)
                            .setColor(context.getColor(R.color.green))
                            .setContentTitle("Social Distancing")
                            .setChannelId("exampleServiceChannel")
                            .build();
                else
                    notif = new NotificationCompat.Builder(context, "SocDisServiceChannel")
                            .setContentText("No nearby devices with WorkFence app within 1.5 meters")
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(R.drawable.ic_notif)
                            .setColor(context.getColor(R.color.green))
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setContentTitle("Social Distancing")
                            .build();
                if (notificationManager != null)
                    notificationManager.notify(1, notif);
            }
        }
    }

    private void upload(final Context context, final long time) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, context.getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            /*ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_CAMERA_COUNT, cameraCount);
                            context.getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);*/
                        }
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("bleoff", time);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getString(context, "authToken", ""));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

}
