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
import com.workfence.R;
import com.workfence.activities.MainActivity;
import com.workfence.adapters.EmployeeAttendanceAdapter;
import com.workfence.models.EmployeeAttendanceModel;
import com.workfence.others.Utils;
import com.workfence.others.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttHistoryFragment extends Fragment {

    Context context;
    StringRequest stringRequestattend;
    RecyclerView rv_attend;
    ConstraintLayout constraintLayout;
    ArrayList<EmployeeAttendanceModel> attendanceModels;
    String URL_attend;
    CoordinatorLayout mCoordinatorLayout;
    EmployeeAttendanceAdapter attendanceAdapter;
    String uuid;
    FrameLayout frameLayout;

    public AttHistoryFragment() {
    }

    public AttHistoryFragment(Context c) {
        context = c;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_att_history, container, false);
        URL_attend = getString(R.string.url_root) + "/user/";

        rv_attend = view.findViewById(R.id.rv_attend);
        mCoordinatorLayout = view.findViewById(R.id.main_content);
        frameLayout = view.findViewById(R.id.framelayout);

        attendanceModels = new ArrayList<>();

        attendanceAdapter = new EmployeeAttendanceAdapter(context, attendanceModels);

        rv_attend.setLayoutManager(new LinearLayoutManager(context));
        rv_attend.setAdapter(attendanceAdapter);

        uuid = Utils.getClientID(context);

        URL_attend = URL_attend + uuid + "/attendance/";

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

        stringRequestattend = new StringRequest(Request.Method.GET, URL_attend,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            URL_attend = jsonObject.getString("next");
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            int prev = attendanceModels.size();
                            Log.i("TAG18", "onResponse: " + response);
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

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequestattend);
    }
}