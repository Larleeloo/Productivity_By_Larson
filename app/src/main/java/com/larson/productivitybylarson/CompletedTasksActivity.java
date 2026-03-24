package com.larson.productivitybylarson;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.larson.productivitybylarson.adapter.CompletedTaskAdapter;
import com.larson.productivitybylarson.database.TaskDatabase;
import com.larson.productivitybylarson.dao.TaskDao;

public class CompletedTasksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.completedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CompletedTaskAdapter adapter = new CompletedTaskAdapter();
        recyclerView.setAdapter(adapter);

        TaskDao taskDao = TaskDatabase.getInstance(this).taskDao();
        taskDao.getCompletedTasks().observe(this, tasks -> {
            if (tasks != null) {
                adapter.setTasks(tasks);
            }
        });
    }
}
