package com.larson.productivitybylarson.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.larson.productivitybylarson.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY " +
            "(urgency * 3.0 + importance * 3.0 + desire * 2.0 + creative * 2.0) DESC")
    LiveData<List<Task>> getActiveTasksByPriority();

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY " +
            "(urgency * 3.0 + importance * 3.0 + desire * 2.0 + creative * 2.0) DESC")
    List<Task> getActiveTasksByPrioritySync();

    @Query("SELECT * FROM tasks WHERE completed = 0 AND deadlineMillis IS NOT NULL")
    List<Task> getTasksWithDeadlines();

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY createdAtMillis DESC")
    LiveData<List<Task>> getCompletedTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskById(long taskId);

    @Query("UPDATE tasks SET urgency = :urgency WHERE id = :taskId")
    void updateUrgency(long taskId, int urgency);

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0")
    LiveData<Integer> getActiveTaskCount();

    // Count of non-persistent tasks that are completed
    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1 AND persistent = 0")
    LiveData<Integer> getCompletedNonPersistentCount();

    // Count of all non-persistent tasks
    @Query("SELECT COUNT(*) FROM tasks WHERE persistent = 0")
    LiveData<Integer> getTotalNonPersistentCount();
}
