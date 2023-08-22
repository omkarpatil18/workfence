package com.workfence.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.workfence.R;
import com.workfence.models.EmployeeAttendanceModel;
import com.workfence.others.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EmployeeAttendanceAdapter extends RecyclerView.Adapter<EmployeeAttendanceAdapter.ViewHolder> {
    private Context context;
    private ArrayList<EmployeeAttendanceModel> detailsModels;

    private int mExpandedPosition = -1;

    public EmployeeAttendanceAdapter(Context context, ArrayList<EmployeeAttendanceModel> detailsModels) {
        this.context = context;
        this.detailsModels = detailsModels;
    }

    @NonNull
    @Override
    public EmployeeAttendanceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_attendance_list_item, parent, false);
        return new EmployeeAttendanceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeAttendanceAdapter.ViewHolder holder, final int position) {
        View view = holder.mView;

        TextView check_in = view.findViewById(R.id.check_in);
        TextView check_out = view.findViewById(R.id.check_out);
        TextView date = view.findViewById(R.id.et_date);
        TextView inTime = view.findViewById(R.id.in_time);
        TextView outTime = view.findViewById(R.id.out_time);
        TextView inStatus = view.findViewById(R.id.check_in_stat);
        TextView outStatus = view.findViewById(R.id.check_out_stat);
        TextView outDuration = view.findViewById(R.id.outside_time);
        TextView screen_time = view.findViewById(R.id.screen_time);
        TextView camera_count = view.findViewById(R.id.camera_count);

        ImageView screenTimeArrow = view.findViewById(R.id.imageView9);
        Boolean b1 = true, b2 = true;

        LinearLayout screen_ll = view.findViewById(R.id.screen_ll);
        LinearLayout camera_ll = view.findViewById(R.id.camera_ll);

        if (!Utils.getBoolean(context, "settingsCamera", true)) {
            b1 = false;
            camera_ll.setVisibility(View.GONE);
        }
        if (!Utils.getBoolean(context, "settingsScreen", true)) {
            b2 = false;
            screen_ll.setVisibility(View.GONE);
        }

        if (!b1 && b2) {
            screenTimeArrow.setVisibility(View.VISIBLE);
        } else
            screenTimeArrow.setVisibility(View.GONE);

        EmployeeAttendanceModel model = detailsModels.get(position);

        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        if (!model.getOutTime().equals("null")) {
            Log.i("TAG14", "onBindViewHolder: " + model.getOutTime());
            long stop = Long.parseLong(model.getOutTime());
            Date stopdate = new Date(stop);
            String formattedOut = sdf.format(stopdate);
            check_out.setText(formattedOut);
            outTime.setText(formattedOut);

            switch (model.getOutStatus()) {
                case 0:
                    outStatus.setText("Inside");
                    outStatus.setTextColor(Color.parseColor("#006400"));
                    break;
                case 1:
                    outStatus.setText("Outside");
                    outStatus.setTextColor(Color.parseColor("#A52A2A"));
                    break;
                case 2:
                    outStatus.setText("WFH");
                    outStatus.setTextColor(Color.GRAY);
                    break;
            }
        } else {
            check_out.setText("-");
            outTime.setText("-");
            outStatus.setText("-");
            outStatus.setTextColor(Color.WHITE);
        }
        long start = Long.parseLong(model.getInTime());
        Date startdate = new Date(start);
        String formattedIn = sdf.format(startdate);
        check_in.setText(formattedIn);
        inTime.setText(formattedIn);
        date.setText(df.format(startdate));

        int outDur = model.getOutsideTime() / 1000;
        String outDura;
        if (outDur < 60) {
            outDura = outDur + "s";
        } else {
            outDura = outDur / 60 + "m " + outDur % 60 + "s";
        }
        outDuration.setText(outDura);

        screen_time.setText(model.getScreenTime() / 60000 + "m");
        camera_count.setText(String.valueOf(model.getCameraCount()));

        final LinearLayout collapse_ll, expand_ll;
        final ImageView collapse, expand;

        collapse_ll = view.findViewById(R.id.ll_collapse);
        expand_ll = view.findViewById(R.id.ll_expand);
        collapse = view.findViewById(R.id.collapse);
        expand = view.findViewById(R.id.expand);

        ImageView expandA1 = view.findViewById(R.id.expand_icon);
        ImageView expandA2 = view.findViewById(R.id.imageView13);

        final boolean isExpanded = position == mExpandedPosition;
        if (isExpanded) {
            expand_ll.setVisibility(View.VISIBLE);
            collapse_ll.setVisibility(View.GONE);
        } else {
            expand_ll.setVisibility(View.GONE);
            collapse_ll.setVisibility(View.VISIBLE);
        }


        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = -1;
                notifyDataSetChanged();
            }
        });


        screenTimeArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = -1;
                notifyDataSetChanged();
            }
        });

        expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = position;
                notifyDataSetChanged();
            }
        });

        switch (model.getInStatus()) {
            case 0:
                inStatus.setText("Inside");
                inStatus.setTextColor(Color.parseColor("#006400"));
                break;
            case 1:
                inStatus.setText("Outside");
                inStatus.setTextColor(Color.parseColor("#A52A2A"));
                break;
            case 2:
                inStatus.setText("WFH");
                inStatus.setTextColor(Color.GRAY);
                break;
        }

        if (!b1 && !b2) {
            expand.setVisibility(View.GONE);
            expandA2.setVisibility(View.GONE);
            expandA1.setVisibility(View.GONE);
        } else {
            expand.setVisibility(View.VISIBLE);
            expandA2.setVisibility(View.VISIBLE);
            expandA1.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return detailsModels.size();
    }

    public void incrementPos(int n) {
        if (mExpandedPosition != -1)
            mExpandedPosition += n;
    }

    static class ViewHolder extends RecyclerAdapter.ViewHolder {
        View mView;

        ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }
}
