package com.workfence.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.workfence.R;
import com.workfence.models.EmployeeInteractionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EmployeeInteractionItemAdapter extends RecyclerView.Adapter<EmployeeInteractionItemAdapter.ViewHolder> {
    private ArrayList<EmployeeInteractionModel> detailsModels;

    public EmployeeInteractionItemAdapter(ArrayList<EmployeeInteractionModel> detailsModels) {
        this.detailsModels = detailsModels;
    }

    @NonNull
    @Override
    public EmployeeInteractionItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_interaction_list_item_inside, parent, false);
        return new EmployeeInteractionItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeInteractionItemAdapter.ViewHolder holder, int position) {
        View view = holder.mView;

        TextView time_interac = view.findViewById(R.id.time_intreac);
        TextView dist = view.findViewById(R.id.dist);
        TextView time = view.findViewById(R.id.time);

        EmployeeInteractionModel model = detailsModels.get(position);
        long intercTime = (Long.parseLong(model.getStopTime()) - Long.parseLong(model.getStartTime())) / 1000;
        String string_time = "";
        if (intercTime < 60)
            string_time = "0 min"; //intercTime + "sec";
        else {
            string_time = intercTime / 60 + " min "; //+ intercTime % 60 + "sec";
        }

        time_interac.setText(string_time);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy\nHH:mm");
        string_time = df.format(new Date(Long.parseLong(model.getStartTime())));

        time.setText(string_time);
    }

    @Override
    public int getItemCount() {
        return detailsModels.size();
    }

    public static class ViewHolder extends RecyclerAdapter.ViewHolder {
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }
}
