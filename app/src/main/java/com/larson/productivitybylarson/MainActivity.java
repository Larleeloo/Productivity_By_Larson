package com.larson.productivitybylarson;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.larson.productivitybylarson.adapter.SwipeCardView;
import com.larson.productivitybylarson.database.TaskDatabase;
import com.larson.productivitybylarson.dao.TaskDao;
import com.larson.productivitybylarson.model.Task;
import com.larson.productivitybylarson.util.NotificationHelper;
import com.larson.productivitybylarson.worker.UrgencyUpdateWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SwipeCardView.SwipeListener {

    private static final int ADD_TASK_REQUEST = 1001;

    private TaskDao taskDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SwipeCardView swipeCardView;

    private FrameLayout cardContainer;
    private LinearLayout emptyState;
    private LinearLayout wellbeingBanner;
    private LinearLayout progressSection;
    private TextView taskCountText;
    private TextView progressText;
    private android.widget.ProgressBar overallProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this);

        // Initialize database
        taskDao = TaskDatabase.getInstance(this).taskDao();

        // Setup views
        cardContainer = findViewById(R.id.cardContainer);
        emptyState = findViewById(R.id.emptyState);
        wellbeingBanner = findViewById(R.id.wellbeingBanner);
        progressSection = findViewById(R.id.progressSection);
        taskCountText = findViewById(R.id.taskCountText);
        progressText = findViewById(R.id.progressText);
        overallProgressBar = findViewById(R.id.overallProgressBar);

        // Initialize swipe card view
        swipeCardView = new SwipeCardView(this, cardContainer, this);

        // Setup buttons
        setupButtons();

        // Show wellbeing banner if user hasn't dismissed it
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("banner_dismissed", false)) {
            wellbeingBanner.setVisibility(View.VISIBLE);
        }

        ImageButton dismissBanner = findViewById(R.id.dismissBanner);
        dismissBanner.setOnClickListener(v -> {
            wellbeingBanner.setVisibility(View.GONE);
            prefs.edit().putBoolean("banner_dismissed", true).apply();
        });

        // Observe tasks
        loadTasks();

        // Observe task count
        taskDao.getActiveTaskCount().observe(this, count -> {
            taskCountText.setText(count + " task" + (count != 1 ? "s" : ""));
        });

        // Observe progress for non-persistent tasks
        observeProgress();

        // Schedule periodic urgency updates
        scheduleUrgencyUpdates();
    }

    private void setupButtons() {
        ImageButton btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(v -> swipeCardView.swipeLeft());

        ImageButton btnDoIt = findViewById(R.id.btnDoIt);
        btnDoIt.setOnClickListener(v -> swipeCardView.swipeRight());

        ImageButton btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTaskActivity.class);
            startActivityForResult(intent, ADD_TASK_REQUEST);
        });

        ImageButton btnCompleted = findViewById(R.id.btnCompleted);
        btnCompleted.setOnClickListener(v -> {
            Intent intent = new Intent(this, CompletedTasksActivity.class);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        taskDao.getActiveTasksByPriority().observe(this, tasks -> {
            if (tasks == null || tasks.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                cardContainer.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                cardContainer.setVisibility(View.VISIBLE);

                // Update urgency scores before displaying
                executor.execute(() -> {
                    List<Task> updatedTasks = new ArrayList<>();
                    for (Task task : tasks) {
                        int effectiveUrgency = com.larson.productivitybylarson.util.PriorityCalculator.getEffectiveUrgency(task);
                        task.setUrgency(effectiveUrgency);
                        updatedTasks.add(task);
                    }
                    // Sort by priority score descending
                    updatedTasks.sort((a, b) -> Double.compare(b.getPriorityScore(), a.getPriorityScore()));

                    runOnUiThread(() -> swipeCardView.setTasks(updatedTasks));
                });
            }
        });
    }

    @Override
    public void onSwipeRight(Task task) {
        if (task.isPersistent()) {
            // Persistent tasks: show timer but don't mark as completed
            showTimerDialog(task);
        } else {
            // Normal tasks: show timer and mark as completed
            showTimerDialog(task);
        }
    }

    @Override
    public void onSwipeLeft(Task task) {
        // Task skipped - stays in the list, will reappear next time
        Toast.makeText(this, "Skipped: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCardsEmpty() {
        emptyState.setVisibility(View.VISIBLE);
        cardContainer.setVisibility(View.GONE);
    }

    private void showTimerDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_timer, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        NumberPicker picker = dialogView.findViewById(R.id.minutePicker);
        picker.setMinValue(5);
        picker.setMaxValue(120);
        picker.setValue(25); // Default Pomodoro-style 25 minutes
        picker.setWrapSelectorWheel(false);

        dialogView.findViewById(R.id.btnSkipTimer).setOnClickListener(v -> {
            dialog.dismiss();
            markTaskInProgress(task);
        });

        dialogView.findViewById(R.id.btnStartTimer).setOnClickListener(v -> {
            int minutes = picker.getValue();
            long durationMillis = minutes * 60 * 1000L;
            NotificationHelper.scheduleTimerAlarm(this, task.getId(), task.getTitle(), durationMillis);
            Toast.makeText(this, "Timer set for " + minutes + " minutes!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            markTaskInProgress(task);
        });

        dialog.show();
    }

    private void markTaskInProgress(Task task) {
        if (task.isPersistent()) {
            // Persistent tasks: never mark as completed, they just cycle back
            Toast.makeText(this, "Working on: " + task.getTitle() + " (persistent)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Working on: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            // Mark as completed since user chose to do it
            executor.execute(() -> {
                task.setCompleted(true);
                taskDao.update(task);
                // Cancel any deadline alarms for this task
                runOnUiThread(() -> NotificationHelper.cancelDeadlineAlarms(this, task.getId()));
            });
        }
    }

    private void observeProgress() {
        // Track completed and total counts for non-persistent tasks
        final int[] counts = new int[2]; // [completed, total]

        taskDao.getCompletedNonPersistentCount().observe(this, completed -> {
            counts[0] = completed != null ? completed : 0;
            updateProgressBar(counts[0], counts[1]);
        });

        taskDao.getTotalNonPersistentCount().observe(this, total -> {
            counts[1] = total != null ? total : 0;
            updateProgressBar(counts[0], counts[1]);
        });
    }

    private void updateProgressBar(int completed, int total) {
        if (total > 0) {
            progressSection.setVisibility(View.VISIBLE);
            int percent = (int) ((completed * 100.0) / total);
            overallProgressBar.setProgress(percent);
            progressText.setText(completed + " / " + total + " completed");
        } else {
            progressSection.setVisibility(View.GONE);
        }
    }

    private void scheduleUrgencyUpdates() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                UrgencyUpdateWorker.class, 1, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "urgency_update",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Tasks are observed via LiveData, so they auto-refresh
    }
}
