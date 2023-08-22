package com.workfence.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import com.workfence.R;
import com.workfence.activities.MainActivity;
import com.workfence.adapters.AttendanceAdapter;
import com.workfence.models.AttendanceModel;
import com.workfence.others.Utils;
import com.workfence.others.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AttendanceFragment extends Fragment {

    StringRequest stringRequest;
    FrameLayout frameLayout;
    CoordinatorLayout mCoordinatorLayout;
    TextView textView;
    private ArrayList<AttendanceModel> attendanceModels;
    private String URL;
    private AttendanceAdapter adapter;
    private Context context;

    public AttendanceFragment() {
    }

    public AttendanceFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        URL = getString(R.string.url_root) + "/attendance/?limit=20&offset=0";
        RecyclerView rv = view.findViewById(R.id.att_rv);
        attendanceModels = new ArrayList<>();
        mCoordinatorLayout = view.findViewById(R.id.main_content);
        frameLayout = view.findViewById(R.id.layout);
        adapter = new AttendanceAdapter(context, attendanceModels);
        textView = view.findViewById(R.id.date);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        textView.setText(sdf.format(date.getTime()));

        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);

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
        Log.i("TAG19", "populateRv: " + URL);
        stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("TAG20", "onResponse: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            URL = jsonObject.getString("next");
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            int prev = attendanceModels.size();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                AttendanceModel attendanceModel = new AttendanceModel(object.getJSONObject("employee").getString("name"),
                                        object.getString("in_time"), object.getString("out_time"), object.getString("out_duration"),
                                        object.getInt("screen_time"), object.getInt("cam_count"),
                                        object.getInt("status_in"), object.getInt("status_out"));
                                attendanceModels.add(attendanceModel);
                            }
                            int now = attendanceModels.size();
                            adapter.incrementPos(now - prev);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                            MainActivity.showSnack("Check your network connection");
                        } else {
                            MainActivity.showSnack("Some error occurred. Try again Later");
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