package com.larson.productivitybylarson.util;

import com.larson.productivitybylarson.model.Task;

/**
 * Calculates dynamic urgency based on deadline proximity.
 *
 * Scale (distance to deadline -> urgency):
 *   90+ days  -> 1
 *   60-89 days -> 2
 *   30-59 days -> 3
 *   14-29 days -> 4
 *   7-13 days  -> 5
 *   3-6 days   -> 6
 *   2 days     -> 7
 *   1 day      -> 8
 *   2-24 hours -> 9
 *   < 1 hour   -> 10
 */
public class PriorityCalculator {

    private static final long MILLIS_PER_HOUR = 3_600_000L;
    private static final long MILLIS_PER_DAY = 86_400_000L;

    public static int calculateDynamicUrgency(Task task) {
        if (task.getDeadlineMillis() == null) {
            return task.getBaseUrgency();
        }

        long now = System.currentTimeMillis();
        long timeRemaining = task.getDeadlineMillis() - now;

        if (timeRemaining <= 0) {
            return 10; // overdue
        }

        long daysRemaining = timeRemaining / MILLIS_PER_DAY;
        long hoursRemaining = timeRemaining / MILLIS_PER_HOUR;

        if (hoursRemaining < 1) return 10;
        if (hoursRemaining < 24) return 9;
        if (daysRemaining < 2) return 8;
        if (daysRemaining < 3) return 7;
        if (daysRemaining < 7) return 6;
        if (daysRemaining < 14) return 5;
        if (daysRemaining < 30) return 4;
        if (daysRemaining < 60) return 3;
        if (daysRemaining < 90) return 2;
        return 1;
    }

    /**
     * Returns the effective urgency: the maximum of the user-set base urgency
     * and the deadline-calculated urgency.
     */
    public static int getEffectiveUrgency(Task task) {
        int deadlineUrgency = calculateDynamicUrgency(task);
        return Math.max(task.getBaseUrgency(), deadlineUrgency);
    }

    public static double calculatePriorityScore(Task task) {
        int effectiveUrgency = getEffectiveUrgency(task);
        return (effectiveUrgency * 3.0) + (task.getImportance() * 3.0)
                + (task.getDesire() * 2.0) + (task.getCreative() * 2.0);
    }
}
