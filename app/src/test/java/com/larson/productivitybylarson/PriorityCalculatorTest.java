package com.larson.productivitybylarson;

import static org.junit.Assert.*;

import com.larson.productivitybylarson.model.Task;
import com.larson.productivitybylarson.util.PriorityCalculator;

import org.junit.Test;

public class PriorityCalculatorTest {

    @Test
    public void testPriorityScoreCalculation() {
        Task task = new Task();
        task.setUrgency(5);
        task.setBaseUrgency(5);
        task.setImportance(5);
        task.setDesire(5);
        task.setCreative(5);

        // (5*3) + (5*3) + (5*2) + (5*2) = 15 + 15 + 10 + 10 = 50
        assertEquals(50.0, task.getPriorityScore(), 0.01);
    }

    @Test
    public void testMaxPriorityScore() {
        Task task = new Task();
        task.setUrgency(10);
        task.setBaseUrgency(10);
        task.setImportance(10);
        task.setDesire(10);
        task.setCreative(10);

        // (10*3) + (10*3) + (10*2) + (10*2) = 30 + 30 + 20 + 20 = 100
        assertEquals(100.0, task.getPriorityScore(), 0.01);
    }

    @Test
    public void testNoDeadlineReturnsBaseUrgency() {
        Task task = new Task();
        task.setBaseUrgency(3);
        task.setDeadlineMillis(null);

        assertEquals(3, PriorityCalculator.calculateDynamicUrgency(task));
    }

    @Test
    public void testOverdueDeadlineReturnsMax() {
        Task task = new Task();
        task.setBaseUrgency(1);
        task.setDeadlineMillis(System.currentTimeMillis() - 1000);

        assertEquals(10, PriorityCalculator.calculateDynamicUrgency(task));
    }

    @Test
    public void testDeadlineFarAwayReturnsMin() {
        Task task = new Task();
        task.setBaseUrgency(1);
        // 100 days from now
        task.setDeadlineMillis(System.currentTimeMillis() + (100L * 24 * 60 * 60 * 1000));

        assertEquals(1, PriorityCalculator.calculateDynamicUrgency(task));
    }

    @Test
    public void testEffectiveUrgencyUsesMaxOfBaseAndDeadline() {
        Task task = new Task();
        task.setBaseUrgency(8);
        // 100 days from now -> deadline urgency = 1
        task.setDeadlineMillis(System.currentTimeMillis() + (100L * 24 * 60 * 60 * 1000));

        // Base (8) > Deadline (1), so effective = 8
        assertEquals(8, PriorityCalculator.getEffectiveUrgency(task));
    }

    @Test
    public void testDeadlineUrgencyScaleDay() {
        Task task = new Task();
        task.setBaseUrgency(1);
        // 12 hours from now -> should be 9
        task.setDeadlineMillis(System.currentTimeMillis() + (12L * 60 * 60 * 1000));

        assertEquals(9, PriorityCalculator.calculateDynamicUrgency(task));
    }

    @Test
    public void testDeadlineUrgencyScale5Days() {
        Task task = new Task();
        task.setBaseUrgency(1);
        // 5 days from now -> should be 6
        task.setDeadlineMillis(System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000));

        assertEquals(6, PriorityCalculator.calculateDynamicUrgency(task));
    }

    @Test
    public void testDeadlineUrgencyScale45Days() {
        Task task = new Task();
        task.setBaseUrgency(1);
        // 45 days from now -> should be 3
        task.setDeadlineMillis(System.currentTimeMillis() + (45L * 24 * 60 * 60 * 1000));

        assertEquals(3, PriorityCalculator.calculateDynamicUrgency(task));
    }
}
