package com.workfence.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.workfence.R;
import com.workfence.adapters.EmployeeAttendanceAdapter;
import com.workfence.adapters.EmployeeInteractionsAdapter;
import com.workfence.models.EmployeeAttendanceModel;
import com.workfence.models.EmployeeInteractionModel;
import com.workfence.others.Utils;
import com.workfence.others.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class EmployeeInteractionDetails extends AppCompatActivity {

    RecyclerView rv, rv_attend;
    ConstraintLayout constraintLayout;
    ArrayList<EmployeeInteractionModel> models;
    ArrayList<EmployeeAttendanceModel> attendanceModels;
    String URL_interac;
    String URL_attend;
    String URL_todo;
    EmployeeInteractionsAdapter adapter;
    EmployeeAttendanceAdapter attendanceAdapter;
    TextView phone_tv, rating_tv;
    TextView switch_interac, switch_attend, todo_tv;
    ImageView edit_todo;
    String uuid;
    StringRequest stringRequestattend, stringRequest;
    float K = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_interaction_details);
        URL_interac = getString(R.string.url_root) + "/user/";
        URL_attend = getString(R.string.url_root) + "/user/";
        URL_todo = getString(R.string.url_root) + "/user/";

        rv = findViewById(R.id.emp_interac_rv);
        rv_attend = findViewById(R.id.rv_attend);
        phone_tv = findViewById(R.id.phone);
        rating_tv = findViewById(R.id.rating);
        switch_interac = findViewById(R.id.interac_tv);
        switch_attend = findViewById(R.id.attendance_tv);
        constraintLayout = findViewById(R.id.cons_layout);
        todo_tv = findViewById(R.id.tv_todo);
        edit_todo = findViewById(R.id.edit_todo);

        models = new ArrayList<>();
        attendanceModels = new ArrayList<>();

        adapter = new EmployeeInteractionsAdapter(this, models);
        attendanceAdapter = new EmployeeAttendanceAdapter(this, attendanceModels);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        rv_attend.setLayoutManager(new LinearLayoutManager(this));
        rv_attend.setAdapter(attendanceAdapter);

        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        final String todo = getIntent().getStringExtra("todo");
        uuid = getIntent().getStringExtra("uuid");

        todo_tv.setText(todo);
        getSupportActionBar().setTitle(name);

        URL_interac = URL_interac + uuid + "/interaction/?limit=30&offset=0";
        URL_attend = URL_attend + uuid + "/attendance/?limit=20&offset=0";
        URL_todo = URL_todo + uuid + "/";

        phone_tv.setText(phone);
        rating_tv.setText("Rating: 100");
        rating_tv.setVisibility(View.GONE);

        switch_interac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_interac.setBackgroundResource(R.drawable.switch_btn_bg);
                switch_interac.setTextColor(Color.parseColor("#2F80ED"));
                switch_attend.setBackgroundResource(0);
                switch_attend.setTextColor(Color.WHITE);
                rv.setVisibility(View.VISIBLE);
                rv_attend.setVisibility(View.GONE);
            }
        });

        switch_attend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_attend.setBackgroundResource(R.drawable.switch_btn_bg);
                switch_interac.setBackgroundResource(0);
                switch_interac.setTextColor(Color.WHITE);
                switch_attend.setTextColor(Color.parseColor("#2F80ED"));
                rv.setVisibility(View.GONE);
                rv_attend.setVisibility(View.VISIBLE);
            }
        });

        edit_todo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText todo_et;
                LinearLayout parent;
                todo_et = new EditText(EmployeeInteractionDetails.this);
                todo_et.setHint("Enter Todo");
                todo_et.setText(todo);

                parent = new LinearLayout(EmployeeInteractionDetails.this);
                parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                parent.setOrientation(LinearLayout.VERTICAL);
                parent.setGravity(Gravity.CENTER);
                parent.removeAllViews();
                parent.addView(todo_et);
                AlertDialog alertDialog = new AlertDialog.Builder(EmployeeInteractionDetails.this)
                        .setCancelable(false)
                        .setTitle("Set Todo")
                        .setView(parent)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                submitTodo(todo_et.getText().toString());
                            }
                        })
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                alertDialog.show();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        stringRequest = new StringRequest(Request.Method.GET, URL_interac,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            Log.i("TAG6", "onResponse: " + response);
                            models.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                long startTime = Long.parseLong(object.getString("start_time"));
                                long stopTime = Long.parseLong(object.getString("stop_time"));
                                JSONArray jsonArray1 = object.getJSONArray("employee_data");
                                String name;
                                if (jsonArray1.getJSONObject(0).getString("client_id").equals(uuid)) {
                                    name = jsonArray1.getJSONObject(1).getString("name");
                                } else {
                                    name = jsonArray1.getJSONObject(0).getString("name");
                                }
                                EmployeeInteractionModel interactionModel = new EmployeeInteractionModel(name,
                                        String.valueOf(startTime), String.valueOf(stopTime), object.getString("maxDist"));
                                if (stopTime - startTime > 60000)
                                    models.add(interactionModel);
                            }
                            Collections.sort(models, new Comparator<EmployeeInteractionModel>() {
                                public int compare(EmployeeInteractionModel o1, EmployeeInteractionModel o2) {
                                    return (int) (Long.parseLong(o2.getStartTime()) - Long.parseLong(o1.getStartTime()));
                                }
                            });
                            adapter.onChange(models);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + Utils.getAuthToken(EmployeeInteractionDetails.this));
                return params;
            }
        };


        //Pagination
        rv_attend.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isSlidingToLast = false;
            int lastVisibleItemPosition = 0;
            int lastPositions = 0;
            LinearLayoutManager linearLayoutManager;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && linearLayoutManager.getItemCount() > 0) {
                    lastPositions = linearLayoutManager.getItemCount();
                    lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                    if (lastPositions - 1 == lastVisibleItemPosition && isSlidingToLast) {
                        if (!URL_attend.equals("null"))
                            populateAttend();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isSlidingToLast = dy > 0;
            }
        });
        populateAttend();

        //Updating in every minute
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                populateRv();
            }
        }, 0, 60000);
    }

    //For populating the interactions
    private void populateRv() {
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //For populating the attendance
    private void populateAttend() {

        stringRequestattend = new StringRequest(Request.Method.GET, URL_attend,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            URL_attend = jsonObject.getString("next");
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            int prev = attendanceModels.size();
                            Log.i("TAG7", "onResponse: " + response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                EmployeeAttendanceModel attendanceModel = new EmployeeAttendanceModel(
                                        object.getString("in_time"), object.getString("out_time"),
                                        object.getInt("out_duration"), object.getInt("cam_count"),
                                        object.getInt("screen_time"), object.getInt("status_in"), object.getInt("status_out"));
                                attendanceModels.add(attendanceModel);
                            }
                            int now = attendanceModels.size();
                            attendanceAdapter.incrementPos(now - prev);
                            attendanceAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + Utils.getAuthToken(EmployeeInteractionDetails.this));
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequestattend);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //For changing the todos for each employee
    void submitTodo(final String todo) {
        StringRequest stringRequestTodo = new StringRequest(Request.Method.PATCH, URL_todo,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        todo_tv.setText(todo);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError)
                            Snackbar.make(constraintLayout, "Check your internet connection !!", Snackbar.LENGTH_SHORT).show();
                        else
                            Snackbar.make(constraintLayout, "Error Occurred. Try again", Snackbar.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + Utils.getAuthToken(EmployeeInteractionDetails.this));
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("todo", todo);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequestTodo);
    }
}
