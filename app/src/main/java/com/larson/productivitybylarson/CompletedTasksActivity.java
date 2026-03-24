package com.larson.productivitybylarson;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.larson.productivitybylarson.adapter.CompletedTaskAdapter;
import com.larson.productivitybylarson.database.TaskDatabase;
import com.larson.productivitybylarson.dao.TaskDao;
import com.larson.productivitybylarson.model.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletedTasksActivity extends AppCompatActivity {

    private TaskDao taskDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        taskDao = TaskDatabase.getInstance(this).taskDao();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView emptyText = findViewById(R.id.emptyCompletedText);
        RecyclerView recyclerView = findViewById(R.id.completedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CompletedTaskAdapter adapter = new CompletedTaskAdapter(task -> {
            // Delete task on button click
            executor.execute(() -> {
                taskDao.delete(task);
            });
        });
        recyclerView.setAdapter(adapter);

        taskDao.getCompletedTasks().observe(this, tasks -> {
            if (tasks != null) {
                adapter.setTasks(tasks);
                emptyText.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(tasks.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }
}
