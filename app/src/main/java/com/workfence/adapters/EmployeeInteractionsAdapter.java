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
import com.workfence.models.EmployeeInteractionModel;

import java.util.ArrayList;
import java.util.HashMap;

public class EmployeeInteractionsAdapter extends RecyclerView.Adapter<EmployeeInteractionsAdapter.ViewHolder> {
    HashMap<String, ArrayList<EmployeeInteractionModel>> hashMap = new HashMap<>();
    ArrayList<String> keys = new ArrayList<>();
    private Context context;
    private ArrayList<EmployeeInteractionModel> detailsModels;

    public EmployeeInteractionsAdapter(Context context, ArrayList<EmployeeInteractionModel> detailsModels) {
        this.context = context;
        this.detailsModels = detailsModels;
    }

    @NonNull
    @Override
    public EmployeeInteractionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_interactions_list_item, parent, false);
        return new EmployeeInteractionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeInteractionsAdapter.ViewHolder holder, int position) {
        View view = holder.mView;

        TextView name = view.findViewById(R.id.name);
        //For a list in each card
        RecyclerView rv = view.findViewById(R.id.rv_interac);

        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(new EmployeeInteractionItemAdapter(hashMap.get(keys.get(position))));
        String nameKey = keys.get(position);
        name.setText(nameKey);
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    public void onChange(ArrayList<EmployeeInteractionModel> models) {
        this.detailsModels = models;

        hashMap.clear();
        keys.clear();
        for (int i = 0; i < detailsModels.size(); i++) {
            if (hashMap.containsKey(detailsModels.get(i).getName())) {
                hashMap.get(detailsModels.get(i).getName()).add(detailsModels.get(i));
            } else {
                Log.i("TAG15", "InteractionsAdapter:o ");
                ArrayList<EmployeeInteractionModel> model = new ArrayList<>();
                model.add(detailsModels.get(i));
                keys.add((detailsModels.get(i).getName()));
                hashMap.put(detailsModels.get(i).getName(), model);
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerAdapter.ViewHolder {
        View mView;

        ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }
}
