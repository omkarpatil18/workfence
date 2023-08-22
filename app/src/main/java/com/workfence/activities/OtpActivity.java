package com.workfence.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

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
import com.mukesh.OtpView;
import com.workfence.R;
//import com.workfence.others.PinEntryEditText;
import com.workfence.others.Utils;
import com.workfence.others.VolleyMultiPartRequest;
import com.workfence.others.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtpActivity extends AppCompatActivity {

    OtpView et_otp;
    Button submit, resend;
    String phone, name;
    ConstraintLayout constraintLayout;
    Context context;
    ProgressBar progress;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        context = this;


        et_otp = findViewById(R.id.pin_entry);
        submit = findViewById(R.id.btn_submit);
        resend = findViewById(R.id.resend_btn);
        constraintLayout = findViewById(R.id.cons_layout);
        progress = findViewById(R.id.progressBar_cyclic);
        progress.setVisibility(View.GONE);
        phone = getIntent().getStringExtra("phone");
        name = getIntent().getStringExtra("name");


        et_otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_otp.getText().toString().trim().length() > 3)
                    hideKeyboard(OtpActivity.this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                verify(phone, et_otp.getText().toString());
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendOtp();
            }
        });
    }

    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{4}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void resendOtp() {
        progress.setVisibility(View.GONE);
        generateOtp(phone, Utils.getClientID(context));
    }

    private void verify(final String phone, final String entered) {
        Log.i("TAG10", "verify: " + entered);
        final StringRequest request = new StringRequest(
                Request.Method.POST,
                getString(R.string.url_root) + "/auth/verify/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String token = null, companyId = null, name = null;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            token = jsonObject.getString("token");
                            companyId = jsonObject.getString("company_id");
                            name = jsonObject.getString("company_name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Utils.setBoolean(context, "serviceStarted", false);
                        Utils.setBoolean(context, "isOtp", false);
                        Utils.setString(context, "authToken", token);

                        Log.i("userparams", Utils.getUserParams(OtpActivity.this));
                        Intent i = new Intent(OtpActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();

                        progress.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                            Snackbar.make(constraintLayout, "Check your network connection", Snackbar.LENGTH_LONG).show();
                        } else if (error.networkResponse != null && error.networkResponse.statusCode == 400)
                            Snackbar.make(constraintLayout, "Invalid Otp", Snackbar.LENGTH_LONG).show();
                        else {
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
                    jsonBody.put("otp", Integer.parseInt(entered));
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return model.substring(manufacturer.length() + 1).replaceAll(" ", "_");
        } else {
            return model.replaceAll(" ", "_");
        }
    }

    private void generateOtp(final String phone, final String clientID) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.POST, getString(R.string.url_root) + "/auth/generate/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        progress.setVisibility(View.GONE);
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
        VolleySingleton.getInstance(this).addToRequestQueue(volleyMultipartRequest);
    }

    @Override
    public void onBackPressed() {
        Utils.setBoolean(this, "isOtp", false);
        super.onBackPressed();
    }
}
