package com.workfence.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.workfence.R;
import com.workfence.others.Utils;
import com.workfence.others.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MaskChangeActivity extends AppCompatActivity {

    private static final int OFFICE_MASK_CHECK = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask_change);
        Intent intent = new Intent(MaskChangeActivity.this, ClassifierActivity.class);
        startActivityForResult(intent, OFFICE_MASK_CHECK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OFFICE_MASK_CHECK:
                String masked = "";
                if (resultCode == 0) {
                    masked = "0";
                } else {
                    masked="1";
                }
                final String finalMasked = masked;
                final StringRequest stringRequest = new StringRequest(Request.Method.PATCH, getString(R.string.url_root) + "/attendance/mask_upd/", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent intent = new Intent(MaskChangeActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MaskChangeActivity.this, error.toString(),Toast.LENGTH_SHORT).show();
                        Log.e("Error",error.toString());
                        Intent intent = new Intent(MaskChangeActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
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
                            jsonBody.put("mask", finalMasked);
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
                        params.put("Authorization", "Token " + Utils.getAuthToken(MaskChangeActivity.this));
                        return params;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
                break;
            default:
                break;
        }
    }
}