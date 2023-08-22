package com.workfence.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.workfence.R;
import com.workfence.models.DeviceDetailsModel;
import com.workfence.others.Utils;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private Context context;
    private ArrayList<DeviceDetailsModel> detailsModels = new ArrayList<>();

    public RecyclerAdapter(@NonNull Context context, int resource, ArrayList<DeviceDetailsModel> detailsModels) {
        this.context = context;
        this.detailsModels.addAll(detailsModels);
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View view = holder.mView;

        CardView linearLayout = view.findViewById(R.id.card_view);
        final LinearLayout dist_ll = view.findViewById(R.id.dist_ll);
        TextView name = view.findViewById(R.id.name);
        TextView distance = view.findViewById(R.id.dist);
        TextView device_name = view.findViewById(R.id.textView9);
        TextView time = view.findViewById(R.id.time_intreac);
        final Button expand = view.findViewById(R.id.expand_btn);
        expand.setVisibility(View.GONE);

        DeviceDetailsModel model = detailsModels.get(position);

        name.setText(model.getName());
        distance.setText(String.format("%.1f", model.getDistance()) + "m" + " and RSSI = " + Utils.rssi);
        distance.setTextSize(14);
        device_name.setText(model.getDevice_model() + " : ");
        int exTime = (int) (model.getStopTime() - model.getStartTime());
        exTime /= 1000;
        String setTime = exTime / 60 + ":" + exTime % 60;
        time.setText(setTime);

        linearLayout.setBackgroundResource(getColor(model.getZone()));

        expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dist_ll.getVisibility() == View.GONE) {
                    dist_ll.setVisibility(View.VISIBLE);
                    expand.setBackgroundResource(R.drawable.ic_sort_up_solid);
                } else {
                    dist_ll.setVisibility(View.GONE);
                    expand.setBackgroundResource(R.drawable.ic_sort_down_solid);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return detailsModels.size();
    }

    public void updateDeviceDetails(ArrayList<DeviceDetailsModel> models) {
        detailsModels.clear();
        detailsModels.addAll(models);
        this.notifyDataSetChanged();
    }

    private int getColor(int zone) {
        switch (zone) {
            case 1:
                return R.drawable.bg_red;
            case 2:
                return Color.YELLOW;
            case 3:
                return R.drawable.bg_green;
            default:
                return Color.GRAY;

        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }
}
