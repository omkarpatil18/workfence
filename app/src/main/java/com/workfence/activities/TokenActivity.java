package com.workfence.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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



public class TokenActivity extends AppCompatActivity {

    Button submit;
    EditText token;
    ConstraintLayout constraintLayout;
    Context context = this;
    ProgressBar progress;
    EditText emailID;
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        submit = findViewById(R.id.continue_btn);
        token = findViewById(R.id.editText3);
        constraintLayout = findViewById(R.id.cons_layout);
        progress = findViewById(R.id.progressBar_cyclic);
        progress.setVisibility(View.GONE);
        emailID = findViewById(R.id.editTextTextEmailAddress);



        emailID.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (emailID.getRight() - emailID.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        Snackbar.make(constraintLayout, "We collect the e-mail ID to send your attendance and social distancing report only.", Snackbar.LENGTH_LONG).show();
                        return true;
                    }
                }
                return false;
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isNetworkAvailable()) {
                    Snackbar.make(constraintLayout, "Check your Internet connection", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (token.getText().toString().length() > 0 && emailID.getText().toString().length() > 0) {
                    //if (confirmToken(token.getText().toString())) {
                    progress.setVisibility(View.VISIBLE);
                    upload(Utils.getName(context), Utils.getPhone(context), emailID.getText().toString(), token.getText().toString());
                    Utils.setString(context,"tokenString", token.getText().toString());
                }
            }
        });
    }

    //Verifying the token entered and uploading other details
    private void upload(final String name, final String phone, final String email, final String token) {
        final StringRequest stringRequest = new StringRequest(Request.Method.PATCH, getString(R.string.url_root) + "/user/" + Utils.getClientID(context) + "/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Utils.setBoolean(context, "token", true);
                Log.i("TAG11", "onResponse: " + response);
                try {
                    JSONObject respObject = new JSONObject(response);
                    JSONObject jsonObject = respObject.getJSONObject("company");
                    Utils.setBoolean(context, "settingsCamera", jsonObject.getBoolean("camera"));
                    Utils.setBoolean(context, "settingsLocation", jsonObject.getBoolean("location"));
                    Utils.setBoolean(context, "settingsScreen", jsonObject.getBoolean("screen"));
                    Utils.setFloat(context, "lat", Float.parseFloat(jsonObject.getString("company_lat")));
                    Utils.setFloat(context, "lng", Float.parseFloat(jsonObject.getString("company_lon")));
                    Utils.setString(context, "compname", jsonObject.getString("name"));
                    Utils.setIsAdmin(getApplicationContext(), respObject.getBoolean("is_supervisor"));
                    Log.i("babadook", "onResponse: " + Utils.getBoolean(context, "is_supervisor", false));
                } catch (JSONException e) {
                    Log.i("babadook", "onResponse: Catch");
                    Log.i("babadook", response);
                    e.printStackTrace();
                }
                Utils.setCompanyID(context, token);
                Intent i = new Intent(TokenActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                progress.setVisibility(View.GONE);
                Log.i("userparams", Utils.getUserParams(TokenActivity.this));
                startActivity(i);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.setVisibility(View.GONE);
                Log.i("TAG12", "onErrorResponse: " + error.getMessage() + Utils.getClientID(TokenActivity.this));
                if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError)
                    Snackbar.make(constraintLayout, "Check your internet connection", Snackbar.LENGTH_LONG).show();
                else if (error.networkResponse != null && error.networkResponse.statusCode == 400)
                    Snackbar.make(constraintLayout, "Invalid Token!!", Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(constraintLayout, "Some error has occurred! Please try again", Snackbar.LENGTH_LONG).show();
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
                    jsonBody.put("first_name", name);
                    jsonBody.put("email", email);
                    jsonBody.put("office_lat", ""); //TODO
                    jsonBody.put("office_lon", ""); //TODO
                    jsonBody.put("device_model", getDeviceName());
                    jsonBody.put("company", token);
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }
}
