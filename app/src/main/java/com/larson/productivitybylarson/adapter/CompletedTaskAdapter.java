package com.larson.productivitybylarson.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.larson.productivitybylarson.R;
import com.larson.productivitybylarson.model.Task;

import java.util.ArrayList;
import java.util.List;

public class CompletedTaskAdapter extends RecyclerView.Adapter<CompletedTaskAdapter.ViewHolder> {

    private List<Task> tasks = new ArrayList<>();

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_completed_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        try {
            holder.colorDot.getBackground().setTint(Color.parseColor(task.getCategoryColor()));
        } catch (Exception e) {
            holder.colorDot.getBackground().setTint(Color.parseColor("#4CAF50"));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        View colorDot;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.completedTaskTitle);
            description = view.findViewById(R.id.completedTaskDescription);
            colorDot = view.findViewById(R.id.colorDot);
        }
    }
}
