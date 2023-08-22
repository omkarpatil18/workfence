package com.workfence.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.workfence.adapters.InteractionsAdapter;
import com.workfence.models.InteractionsModel;
import com.workfence.others.Utils;
import com.workfence.others.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class InteractionFragment extends Fragment {

    RecyclerView rv;
    ArrayList<InteractionsModel> interactionsModels;
    String URL;
    InteractionsAdapter adapter;
    Context context;
    Activity activity;
    StringRequest stringRequest;
    FrameLayout frameLayout;
    TextView textView, emp, totalTime;
    CoordinatorLayout mCoordinatorLayout;

    public InteractionFragment() {
    }

    public InteractionFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
            Log.i("TAG_IF", "onAttach: ");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interaction, container, false);
        URL = getString(R.string.url_root) + "/interaction/?limit=  50&offset=0";
        rv = view.findViewById(R.id.interaction_rv);
        interactionsModels = new ArrayList<>();
        adapter = new InteractionsAdapter(context, interactionsModels);
        frameLayout = view.findViewById(R.id.layout);
        mCoordinatorLayout = view.findViewById(R.id.main_content);
        textView = view.findViewById(R.id.date);
        emp = view.findViewById(R.id.emp_count);
        totalTime = view.findViewById(R.id.tot_time);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        textView.setText(sdf.format(date.getTime()));

        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);

        stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            Log.i("TAG27", "onResponse: " + jsonArray.toString());

                            interactionsModels.clear();
                            Set<String> selection = new HashSet<>();
                            Set<String> empCount = new HashSet<>();
                            long intTime = 0;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                String name1 = object.getJSONArray("employee_data").getJSONObject(0).getString("name");
                                String uuid1 = object.getJSONArray("employee_data").getJSONObject(0).getString("client_id");
                                String uuid2 = object.getJSONArray("employee_data").getJSONObject(1).getString("client_id");
                                String name2 = object.getJSONArray("employee_data").getJSONObject(1).getString("name");
                                String creator = object.getString("creator");
                                long startTime = Long.parseLong(object.getString("start_time"));
                                long stopTime = Long.parseLong(object.getString("stop_time"));
                                InteractionsModel interactionsModel = new InteractionsModel(uuid1, uuid2, name1, name2,
                                        object.getString("start_time"), object.getString("stop_time"),
                                        object.getString("maxDist"));
                                Log.i("Taggy", interactionsModel.getString());

                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(startTime);
                                Calendar nowCal = Calendar.getInstance();
                                Log.d("startYear", String.valueOf(cal.get(Calendar.YEAR)));
                                Log.d("startDate", String.valueOf(cal.get(Calendar.DAY_OF_YEAR)));
                                Log.d("curYear", String.valueOf(nowCal.get(Calendar.YEAR)));
                                Log.d("curDate", String.valueOf(nowCal.get(Calendar.DAY_OF_YEAR)));
                                if (stopTime - startTime > 60000 && (nowCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) && (nowCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR))) {
                                    empCount.add(uuid1);
                                    empCount.add(uuid2);
                                    if (!selection.contains(uuid1 + "#" + uuid2) && !selection.contains(uuid2 + "#" + uuid1)) {
                                        selection.add(uuid1 + "#" + uuid2);
                                        interactionsModels.add(interactionsModel);
                                        intTime += Long.parseLong(interactionsModel.getStopTime()) - Long.parseLong(interactionsModel.getStartTime());

                                    } else if (selection.contains(uuid1 + "#" + uuid2)) {
                                        interactionsModels.add(interactionsModel);
                                        intTime += Long.parseLong(interactionsModel.getStopTime()) - Long.parseLong(interactionsModel.getStartTime());
                                    }
                                }
                            }

                            Log.i("TAG", "onResponse: " + interactionsModels.size());
                            emp.setText("Employees violating distancing: " + empCount.size());
                            totalTime.setText("Total violation time: " + (intTime / 60000) + " min");

                            Collections.sort(interactionsModels, new Comparator<InteractionsModel>() {
                                public int compare(InteractionsModel o1, InteractionsModel o2) {
                                    return (int) (Long.parseLong(o2.getStartTime()) - Long.parseLong(o1.getStartTime()));
                                }
                            });

                            adapter.onChange(interactionsModels);
                        } catch (JSONException e) {
                            Log.i("TAGCatch", "onResponse: " + e.getLocalizedMessage());
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
                if (activity != null && InteractionFragment.this.isVisible()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateRv();
                        }
                    });
                }
            }
        }, 0, 5000);

        return view;
    }

    private void populateRv() {
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}