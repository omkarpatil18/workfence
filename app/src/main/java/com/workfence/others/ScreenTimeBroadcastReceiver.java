package com.workfence.others;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.workfence.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ScreenTimeBroadcastReceiver extends BroadcastReceiver {

    public static final long TIME_ERROR = 1000;
    public static long startTimer;
    public static long endTimer;
    public static long screenOnTimeSingle;

    public void onReceive(Context context, Intent intent) {
        Log.i("Screen", "ScreenTimeService onReceive");

        long screenTime = Utils.getLong(context, "screenTime", (long) -1);
        if (screenTime == -1)
            return;

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

            startTimer = System.currentTimeMillis();

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            endTimer = System.currentTimeMillis();
            screenOnTimeSingle = endTimer - startTimer;

            if (screenOnTimeSingle > TIME_ERROR) {
                Utils.setLong(context, "screenTime", screenTime + screenOnTimeSingle);
                upload(context, screenOnTimeSingle);
            }
        }
    }

    private void upload(final Context context, final Long time) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, context.getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!error.toString().equals("com.android.volley.TimeoutError")) {
                            Log.e("Volley error", error.toString());
                        }
                    }
                }) {

            /*@Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uuid", sharedPreferences.getString("uuid", "-1"));
                params.put("outDuration", String.valueOf(outDuration));
                params.put("inTime", sharedPreferences.getString("inTime", "-1"));
                return params;
            }*/

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("screen_time", time);
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
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
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