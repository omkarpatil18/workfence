package com.workfence.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.workfence.R;
import com.workfence.others.Utils;
import com.workfence.others.VolleyMultiPartRequest;
import com.workfence.others.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    EditText code, phone, name;
    Button cont_btn;
    Context context;
    boolean hasPermission;
    ProgressBar progress;
    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        if (Utils.getBoolean(context, "isOtp", false)) {
            Intent i = new Intent(LoginActivity.this, OtpActivity.class);
            i.putExtra("name", Utils.getString(this, "name", ""));
            i.putExtra("phone", Utils.getString(this, "phone", ""));
            startActivity(i);
        }

        permissions();

        code = findViewById(R.id.et_code);
        phone = findViewById(R.id.et_phone);
        name = findViewById(R.id.et_name);
        cont_btn = findViewById(R.id.continue_btn);
        progress = findViewById(R.id.progressBar_cyclic);
        constraintLayout = findViewById(R.id.cons_layout);
        progress.setVisibility(View.GONE);

        code.setFocusable(false);
        name.requestFocus();

        cont_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPermission) {
                    locPermissions();
                } else {
                    check();
                }
            }
        });

        code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (code.getText().toString().trim().length() > 2) {
                    code.clearFocus();
                    phone.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    void permissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, 12);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 319) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            }
        }
        if (requestCode == 12) {
            locPermissions();
        }
    }

    private void locPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 319);
            } else
                hasPermission = true;
        }
    }

    public void check() {
        if (code.getText().toString().equals("")) {
            code.setError("");
            return;
        }
        if (name.getText().toString().equals("")) {
            phone.setError("Name should be present");
            return;
        }
        if (phone.getText().toString().equals("")) {
            phone.setError("Phone number should be present");
            return;
        }

        progress.setVisibility(View.VISIBLE);

        generateOtp("+91" + phone.getText().toString());
    }

    //Volley call for Otp generation
    private void generateOtp(final String phone) {
        StringRequest userCreate = new StringRequest(Request.Method.POST, getString(R.string.url_root) + "/user/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Utils.setClientID(LoginActivity.this, jsonObject.getString("client_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.POST, getString(R.string.url_root) + "/auth/generate/",
                                new Response.Listener<NetworkResponse>() {
                                    @Override
                                    public void onResponse(NetworkResponse response) {
                                        Intent i = new Intent(LoginActivity.this, OtpActivity.class);
                                        i.putExtra("name", name.getText().toString().trim());
                                        i.putExtra("phone", phone);
                                        Utils.setString(context, "name", name.getText().toString().trim());
                                        Utils.setString(context, "phone", phone);
                                        Utils.setBoolean(context, "isOtp", true);
                                        progress.setVisibility(View.GONE);
                                        Log.i("userparams", Utils.getUserParams(LoginActivity.this));
                                        startActivity(i);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                                            Snackbar.make(constraintLayout, "Check your network connection", Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Snackbar.make(constraintLayout, "Some error occurred. Try again Later", Snackbar.LENGTH_LONG).show();
                                        }
                                        progress.setVisibility(View.GONE);
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
                                    jsonBody.put("username", phone);
                                    jsonBody.put("password", phone);
                                    final String mRequestBody = jsonBody.toString();
                                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                                } catch (JSONException uee) {
                                    Log.e("Volley Error", uee.toString());
                                    return null;
                                }
                            }
                        };

                        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                                15000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(volleyMultipartRequest);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                            Snackbar.make(constraintLayout, "Check your network connection", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(constraintLayout, "Some error occurred. Try again Later", Snackbar.LENGTH_LONG).show();
                        }
                        progress.setVisibility(View.GONE);
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
                    jsonBody.put("username", phone);
                    jsonBody.put("password", phone);
                    jsonBody.put("email", "placeholder@domain.com");
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(userCreate);
    }
}
