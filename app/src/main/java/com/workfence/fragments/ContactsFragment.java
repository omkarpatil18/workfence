package com.workfence.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.workfence.R;
import com.workfence.adapters.RecyclerAdapter;
import com.workfence.database.DataContract;
import com.workfence.models.DeviceDetailsModel;
import com.workfence.others.MyService;
import com.workfence.others.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ContactsFragment extends Fragment {

    Activity activity;
    CoordinatorLayout mCoordinatorLayout;
    RecyclerView recyclerView;
    ImageView imageView;
    private ArrayList<DeviceDetailsModel> models = new ArrayList<>();
    private RecyclerAdapter adapter;
    private Context context;
    TextView calib_tv;
    ImageView calib_img;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public ContactsFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
            Log.i("TAG21", "onAttach: ");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        calib_img = view.findViewById(R.id.img_is_calib);
        calib_tv = view.findViewById(R.id.is_calib);

        if (context != null) {
            int calib_state = Utils.getInteger(context, "isCalibrated", 0);
            if (calib_state == 0) {
                calib_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_phonelink_erase_24));
                calib_tv.setText(R.string.is_not_calib);
            } else {
                calib_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_circle_24));
                calib_tv.setText(R.string.is_calib);
            }
        }

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        imageView = view.findViewById(R.id.imageView);

        adapter = new RecyclerAdapter(context, R.layout.list_item, models);
        recyclerView.setAdapter(adapter);
        displayDatabaseInfo();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayDatabaseInfo();
                        }
                    });
                }
            }
        }, 1000, 1000);
        Log.i("TAG22", "onCreateView: ");
        return view;
    }

    private void displayDatabaseInfo() {
        long currTime = System.currentTimeMillis();

        models.clear();
        Iterator it = MyService.UIReadings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ContentValues values = (ContentValues) pair.getValue();
            String currentName = values.getAsString(DataContract.InteractionEntry.COLUMN_NAME);
            String currentModel = values.getAsString(DataContract.InteractionEntry.COLUMN_MODEL);
            String currentAddress = values.getAsString(DataContract.InteractionEntry.COLUMN_ADDRESS);
            int currentRssi = values.getAsInteger(DataContract.InteractionEntry.COLUMN_AVG_RSSI);
            float currentDistance = values.getAsFloat(DataContract.InteractionEntry.COLUMN_DISTANCE);
            long startTime = values.getAsLong(DataContract.InteractionEntry.COLUMN_TSTART);
            long stopTime = values.getAsLong(DataContract.InteractionEntry.COLUMN_TSTOP);
            int currentZone = values.getAsInteger(DataContract.InteractionEntry.COLUMN_ZONE);
            if (currTime < stopTime + 10000) // Displaying only devices connected within last 10 seconds
                models.add(new DeviceDetailsModel(currentAddress, currentName, currentRssi,
                        currentDistance, startTime, stopTime, currentZone, currentModel));
            else
                it.remove();
        }

        if (models.size() == 0)
            MyService.stopNotification();
        adapter.updateDeviceDetails(models);
        if (models.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
        }
    }
}
