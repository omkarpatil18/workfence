package com.workfence.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
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
import com.workfence.adapters.EmployeeAdapter;
import com.workfence.models.EmployeeModel;
import com.workfence.others.Utils;
import com.workfence.others.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EmployeeMetricsFragment extends Fragment {

    static Context context;
    RecyclerView rv;
    EditText et_search;
    ImageButton btn_search;
    ArrayList<EmployeeModel> models;
    String URL;
    EmployeeAdapter adapter;
    StringRequest stringRequest;
    FrameLayout frameLayout;
    CoordinatorLayout mCoordinatorLayout;

    public EmployeeMetricsFragment() {
    }

    public EmployeeMetricsFragment(Context context) {
        EmployeeMetricsFragment.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        URL = getString(R.string.url_root) + "/analytics/?limit=10&offset=0";
        View view = inflater.inflate(R.layout.fragment_employee_metrics, container, false);
        rv = view.findViewById(R.id.emp_rv);
        et_search = view.findViewById(R.id.et_search);
        btn_search = view.findViewById(R.id.btn_search);
        models = new ArrayList<>();
        mCoordinatorLayout = view.findViewById(R.id.main_content);
        adapter = new EmployeeAdapter(context, models);
        frameLayout = view.findViewById(R.id.layout);

        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(et_search.getText().toString(), true);
                if (et_search.getText().toString().length() > 0)
                    btn_search.setImageResource(R.drawable.ic_baseline_close_24);
                else
                    btn_search.setImageResource(R.drawable.ic_baseline_search_24);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText("");
                et_search.clearFocus();
            }
        });

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        if (!URL.equals("null"))
                            populateRv();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isSlidingToLast = dy > 0;
            }
        });
        populateRv();

        return view;
    }

    private void populateRv() {
        stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("TAG23", "onResponse: " + Utils.getAuthToken(context));
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            URL = jsonObject.getString("next");
                            jsonObject = jsonObject.getJSONObject("results");
                            Iterator<String> keys = jsonObject.keys();

                            while (keys.hasNext()) {
                                String key = keys.next();
                                if (jsonObject.get(key) instanceof JSONObject) {
                                    JSONObject object = jsonObject.getJSONObject(key);
                                    EmployeeModel employeeModel = new EmployeeModel(object.getJSONObject("employee").getString("name"),
                                            key, object.getJSONObject("employee").getString("phno"),
                                            object.getLong("screen_time"), object.getLong("work_time"),
                                            object.getLong("interaction_time"),
                                            object.getJSONObject("employee").getString("todo"),
                                            object.getJSONObject("employee").getBoolean("recent_interaction"));
                                    models.add(employeeModel);
                                }
                            }
                            Log.i("TAG24", "onResponse: " + models.size());
                            adapter.filter(et_search.getText().toString(), false);
                            //adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("TAG25", "onErrorResponse: " + error.getMessage() + " " + Utils.getAuthToken(context));
                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                            Snackbar.make(frameLayout, "Check your network connection", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(frameLayout, "Some error occurred. Try again Later", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
