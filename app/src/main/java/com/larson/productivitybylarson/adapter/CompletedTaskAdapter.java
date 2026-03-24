package com.larson.productivitybylarson.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.larson.productivitybylarson.R;
import com.larson.productivitybylarson.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompletedTaskAdapter extends RecyclerView.Adapter<CompletedTaskAdapter.ViewHolder> {

    public interface OnTaskDeleteListener {
        void onDelete(Task task);
    }

    private List<Task> tasks = new ArrayList<>();
    private final OnTaskDeleteListener deleteListener;

    public CompletedTaskAdapter(OnTaskDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

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

        // Category color
        try {
            holder.colorDot.getBackground().setTint(Color.parseColor(task.getCategoryColor()));
        } catch (Exception e) {
            holder.colorDot.getBackground().setTint(Color.parseColor("#4CAF50"));
        }

        // Priority score
        int score = (int) task.getPriorityScore();
        holder.scoreText.setText("Score: " + score);

        // Created date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        holder.dateText.setText("Created: " + sdf.format(new Date(task.getCreatedAtMillis())));

        // Attribute bars
        holder.urgencyBar.setProgress(task.getUrgency());
        holder.importanceBar.setProgress(task.getImportance());
        holder.desireBar.setProgress(task.getDesire());
        holder.creativeBar.setProgress(task.getCreative());

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, scoreText, dateText;
        View colorDot;
        ProgressBar urgencyBar, importanceBar, desireBar, creativeBar;
        ImageButton btnDelete;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.completedTaskTitle);
            description = view.findViewById(R.id.completedTaskDescription);
            colorDot = view.findViewById(R.id.colorDot);
            scoreText = view.findViewById(R.id.completedScoreText);
            dateText = view.findViewById(R.id.completedDateText);
            urgencyBar = view.findViewById(R.id.completedUrgencyBar);
            importanceBar = view.findViewById(R.id.completedImportanceBar);
            desireBar = view.findViewById(R.id.completedDesireBar);
            creativeBar = view.findViewById(R.id.completedCreativeBar);
            btnDelete = view.findViewById(R.id.btnDeleteTask);
        }
    }
}
