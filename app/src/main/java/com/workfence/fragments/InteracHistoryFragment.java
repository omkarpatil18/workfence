package com.workfence.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.workfence.adapters.EmployeeInteractionsAdapter;
import com.workfence.models.EmployeeInteractionModel;
import com.workfence.others.Utils;
import com.workfence.others.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class InteracHistoryFragment extends Fragment {

    Context context;
    RecyclerView rv;
    ConstraintLayout constraintLayout;
    FrameLayout frameLayout;
    ArrayList<EmployeeInteractionModel> models;
    String URL_interac;
    EmployeeInteractionsAdapter adapter;
    String uuid;
    StringRequest stringRequest;
    CoordinatorLayout mCoordinatorLayout;

    public InteracHistoryFragment() {
        // Required empty public constructor
    }

    public InteracHistoryFragment(Context c) {
        context = c;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        URL_interac = getString(R.string.url_root) + "/user/";
        View view = inflater.inflate(R.layout.fragment_interac_history, container, false);

        rv = view.findViewById(R.id.emp_interac_rv);
        mCoordinatorLayout = view.findViewById(R.id.main_content);
        constraintLayout = view.findViewById(R.id.cons_layout);
        frameLayout = view.findViewById(R.id.framelayout);

        models = new ArrayList<>();

        adapter = new EmployeeInteractionsAdapter(context, models);

        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);

        uuid = Utils.getClientID(context);

        URL_interac = URL_interac + uuid + "/interaction/?limit=30&offset=0";

        stringRequest = new StringRequest(Request.Method.GET, URL_interac,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            HashMap<String, String> selection = new HashMap<>();
                            Log.i("TAG26", "onResponse: " + response);
                            models.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                long startTime = Long.parseLong(object.getString("start_time"));
                                long stopTime = Long.parseLong(object.getString("stop_time"));
                                JSONArray jsonArray1 = object.getJSONArray("employee_data");
                                String name;
                                String clientId;
                                String name1 = object.getJSONArray("employee_data").getJSONObject(0).getString("name");
                                String uuid1 = object.getJSONArray("employee_data").getJSONObject(0).getString("client_id");
                                String uuid2 = object.getJSONArray("employee_data").getJSONObject(1).getString("client_id");
                                String name2 = object.getJSONArray("employee_data").getJSONObject(1).getString("name");
                                String creator = object.getString("creator");

                                if (jsonArray1.getJSONObject(0).getString("client_id").equals(uuid)) {
                                    name = jsonArray1.getJSONObject(1).getString("name");
                                    clientId = jsonArray1.getJSONObject(1).getString("client_id");
                                } else {
                                    name = jsonArray1.getJSONObject(0).getString("name");
                                    clientId = jsonArray1.getJSONObject(0).getString("client_id");
                                }
                                EmployeeInteractionModel interactionModel = new EmployeeInteractionModel(name,
                                        String.valueOf(startTime), String.valueOf(stopTime), object.getString("maxDist"));
                                if (stopTime - startTime > 60000)
                                    models.add(interactionModel);
                            }
                            //rating_tv.setText("Rating: " + String.format("%.1f", (100 - intTime / K)));
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

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                populateRv();
            }
        }, 0, 10000);
        return view;
    }

    private void populateRv() {
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}