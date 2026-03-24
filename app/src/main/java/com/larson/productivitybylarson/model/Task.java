package com.larson.productivitybylarson.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;
    private String imagePath;
    private String categoryColor; // hex color string e.g. "#FF5722"

    // Scoring attributes (1-10)
    private int urgency;
    private int importance;
    private int desire;
    private int creative;

    // Deadline (null if no deadline)
    private Long deadlineMillis;

    // Base urgency set by user (before deadline scaling)
    private int baseUrgency;

    private long createdAtMillis;
    private boolean completed;

    public Task() {
        this.createdAtMillis = System.currentTimeMillis();
        this.completed = false;
        this.categoryColor = "#4CAF50";
    }

    // Priority score calculation
    public double getPriorityScore() {
        return (urgency * 3.0) + (importance * 3.0) + (desire * 2.0) + (creative * 2.0);
    }

    // Getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        this.urgency = urgency;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public int getDesire() {
        return desire;
    }

    public void setDesire(int desire) {
        this.desire = desire;
    }

    public int getCreative() {
        return creative;
    }

    public void setCreative(int creative) {
        this.creative = creative;
    }

    public Long getDeadlineMillis() {
        return deadlineMillis;
    }

    public void setDeadlineMillis(Long deadlineMillis) {
        this.deadlineMillis = deadlineMillis;
    }

    public int getBaseUrgency() {
        return baseUrgency;
    }

    public void setBaseUrgency(int baseUrgency) {
        this.baseUrgency = baseUrgency;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
