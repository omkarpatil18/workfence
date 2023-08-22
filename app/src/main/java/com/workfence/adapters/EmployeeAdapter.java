package com.workfence.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.workfence.R;
import com.workfence.activities.EmployeeInteractionDetails;
import com.workfence.models.EmployeeModel;
import com.workfence.others.Utils;

import java.util.ArrayList;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {
    ArrayList<EmployeeModel> detailsModelsCopy = new ArrayList<>();
    private Context context;
    private ArrayList<EmployeeModel> detailsModels;

    public EmployeeAdapter(Context context, ArrayList<EmployeeModel> detailsModels) {
        this.context = context;
        this.detailsModels = detailsModels;
    }

    @NonNull
    @Override
    public EmployeeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_list_item, parent, false);
        return new EmployeeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeAdapter.ViewHolder holder, int position) {
        View view = holder.mView;

        TextView name = view.findViewById(R.id.name);
        TextView screenTime = view.findViewById(R.id.tv_screen);
        TextView workTime = view.findViewById(R.id.tv_work);
        TextView rating = view.findViewById(R.id.rating);
        CardView ll = view.findViewById(R.id.card_view);

        LinearLayout screen_ll = view.findViewById(R.id.screen_ll);

        if (!Utils.getBoolean(context, "settingsScreen", true)) {
            screen_ll.setVisibility(View.GONE);
        }

        final EmployeeModel model = detailsModels.get(position);

        name.setText(model.getName());

        long screen = model.getScreenTime() / 60000;
        String screenText;
        if (screen > 60)
            screenText = screen / 60 + "hr " + screen % 60 + "m";
        else
            screenText = screen % 60 + "m";
        screenTime.setText(screenText);

        long work = model.getWorkTime() / 60000;
        String workText;
        if (screen > 60)
            workText = work / 60 + "hr " + work % 60 + "m";
        else
            workText = work % 60 + "m";
        workTime.setText(workText);


        rating.setText(String.format("%.1f", model.getRating()));

        if (model.getStatus())
            name.setTextColor(Color.parseColor("#A52A2A"));
        else {
            name.setTextColor(Color.WHITE);
        }

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, EmployeeInteractionDetails.class);
                i.putExtra("name", model.getName());
                i.putExtra("phone", model.getPhone());
                i.putExtra("uuid", model.getUuid());
                i.putExtra("todo", model.getTodo());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return detailsModels.size();
    }

    public void filter(String query, boolean isSearch) {

        if (!isSearch) {
            detailsModelsCopy.clear();
            detailsModelsCopy.addAll(detailsModels);
        }

        detailsModels.clear();
        if (query.isEmpty()) {
            detailsModels.addAll(detailsModelsCopy);
        } else {
            query = query.toLowerCase();
            for (EmployeeModel model : detailsModelsCopy) {
                if (model.getName().toLowerCase().contains(query)) {
                    detailsModels.add(model);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerAdapter.ViewHolder {
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

}
