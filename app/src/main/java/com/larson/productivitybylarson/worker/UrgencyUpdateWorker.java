package com.larson.productivitybylarson.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.larson.productivitybylarson.database.TaskDatabase;
import com.larson.productivitybylarson.dao.TaskDao;
import com.larson.productivitybylarson.model.Task;
import com.larson.productivitybylarson.util.PriorityCalculator;

import java.util.List;

/**
 * Periodically updates urgency scores for tasks with deadlines
 * based on how close the deadline is.
 */
public class UrgencyUpdateWorker extends Worker {

    public UrgencyUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        TaskDao dao = TaskDatabase.getInstance(getApplicationContext()).taskDao();
        List<Task> tasksWithDeadlines = dao.getTasksWithDeadlines();

        for (Task task : tasksWithDeadlines) {
            int newUrgency = PriorityCalculator.getEffectiveUrgency(task);
            if (newUrgency != task.getUrgency()) {
                dao.updateUrgency(task.getId(), newUrgency);
            }
        }

        return Result.success();
    }
}
