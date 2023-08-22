package com.workfence.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.workfence.R;
import com.workfence.models.InteractionsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class InteractionsAdapter extends RecyclerView.Adapter<InteractionsAdapter.ViewHolder> {
    HashMap<String, ArrayList<InteractionsModel>> hashMap = new HashMap<>();
    ArrayList<String> keys = new ArrayList<>();
    ArrayList<String> name1 = new ArrayList<>();
    ArrayList<String> name2 = new ArrayList<>();
    private Context context;
    private ArrayList<InteractionsModel> detailsModels;


    public InteractionsAdapter(Context context, ArrayList<InteractionsModel> detailsModels) {
        this.context = context;
        this.detailsModels = detailsModels;
    }

    @NonNull
    @Override
    public InteractionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.interactions_list_item, parent, false);
        return new InteractionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InteractionsAdapter.ViewHolder holder, int position) {
        View view = holder.mView;

        RecyclerView rv = view.findViewById(R.id.rv_interac);
        TextView name1 = view.findViewById(R.id.name1);
        TextView name2 = view.findViewById(R.id.name2);

        //For a list in each card
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(new InteractionItemAdapter(Objects.requireNonNull(hashMap.get(keys.get(position)))));
        name1.setText(this.name1.get(position));
        name2.setText(this.name2.get(position));
/*
        InteractionsModel model = detailsModels.get(position);
        long intercTime = (Long.parseLong(model.getStopTime()) - Long.parseLong(model.getStartTime())) / 1000;
        String string_time = "";
        if (intercTime < 60)
            string_time = intercTime + "sec";
        else {
            string_time = intercTime / 60 + "min " + intercTime % 60 + "sec";
        }

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        string_time = df.format(new Date(Long.parseLong(model.getStartTime()))) + " - " + string_time;

        name1.setText(model.getName1());
        name2.setText(model.getName2());
        time.setText(string_time);
        dist.setText(String.format("%.1f", model.getAvg_dist()) + "m");*/
    }

    @Override
    public int getItemCount() {
        Log.i("TAG16", "getItemCount: " + keys.size());
        return keys.size();
    }

    public void onChange(ArrayList<InteractionsModel> models) {
        this.detailsModels = models;

        hashMap.clear();
        keys.clear();
        name1.clear();
        name2.clear();
        for (int i = 0; i < detailsModels.size(); i++) {
            if (hashMap.containsKey(detailsModels.get(i).getUuid1() + "-" + detailsModels.get(i).getUuid2())) {
                hashMap.get(detailsModels.get(i).getUuid1() + "-" + detailsModels.get(i).getUuid2()).add(detailsModels.get(i));
            }else {
                Log.i("TAG17", "InteractionsAdapter:0 ");
                ArrayList<InteractionsModel> model = new ArrayList<>();
                model.add(detailsModels.get(i));
                keys.add((detailsModels.get(i).getUuid1() + "-" + detailsModels.get(i).getUuid2()));
                name1.add((detailsModels.get(i).getName1()));
                name2.add((detailsModels.get(i).getName2()));
                hashMap.put(detailsModels.get(i).getUuid1() + "-" + detailsModels.get(i).getUuid2(), model);
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
