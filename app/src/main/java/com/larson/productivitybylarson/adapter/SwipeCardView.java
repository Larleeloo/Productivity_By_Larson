package com.larson.productivitybylarson.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.larson.productivitybylarson.R;
import com.larson.productivitybylarson.model.Task;
import com.larson.productivitybylarson.util.PriorityCalculator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SwipeCardView {

    public interface SwipeListener {
        void onSwipeRight(Task task);
        void onSwipeLeft(Task task);
        void onCardsEmpty();
    }

    private final Context context;
    private final FrameLayout container;
    private final SwipeListener listener;
    private List<Task> tasks;
    private int currentIndex = 0;
    private View currentCard;
    private View nextCard;

    private float startX, startY;
    private float dX, dY;
    private static final float SWIPE_THRESHOLD = 200f;
    private static final float ROTATION_FACTOR = 0.08f;

    public SwipeCardView(Context context, FrameLayout container, SwipeListener listener) {
        this.context = context;
        this.container = container;
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        this.currentIndex = 0;
        container.removeAllViews();
        loadCards();
    }

    public Task getCurrentTask() {
        if (tasks != null && currentIndex < tasks.size()) {
            return tasks.get(currentIndex);
        }
        return null;
    }

    private void loadCards() {
        container.removeAllViews();

        if (tasks == null || currentIndex >= tasks.size()) {
            listener.onCardsEmpty();
            return;
        }

        // Load next card behind current (if available)
        if (currentIndex + 1 < tasks.size()) {
            nextCard = createCard(tasks.get(currentIndex + 1));
            nextCard.setScaleX(0.95f);
            nextCard.setScaleY(0.95f);
            nextCard.setAlpha(0.7f);
            container.addView(nextCard);
        }

        // Load current card on top
        currentCard = createCard(tasks.get(currentIndex));
        setupTouchListener(currentCard);
        container.addView(currentCard);
    }

    private View createCard(Task task) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_card, container, false);

        // Category color stripe
        View stripe = view.findViewById(R.id.categoryStripe);
        try {
            stripe.setBackgroundColor(Color.parseColor(task.getCategoryColor()));
        } catch (Exception e) {
            stripe.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        // Task image
        ImageView taskImage = view.findViewById(R.id.taskImage);
        if (task.getImagePath() != null && !task.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(Uri.fromFile(new File(task.getImagePath())))
                    .centerCrop()
                    .placeholder(R.drawable.ic_task_placeholder)
                    .into(taskImage);
        } else {
            taskImage.setImageResource(R.drawable.ic_task_placeholder);
            taskImage.setScaleType(ImageView.ScaleType.CENTER);
            try {
                taskImage.setBackgroundColor(Color.parseColor(task.getCategoryColor()));
                taskImage.setAlpha(0.3f);
            } catch (Exception e) {
                taskImage.setBackgroundColor(Color.parseColor("#E0E0E0"));
            }
        }

        // Title and description
        TextView title = view.findViewById(R.id.taskTitle);
        title.setText(task.getTitle());

        TextView description = view.findViewById(R.id.taskDescription);
        description.setText(task.getDescription());

        // Deadline
        TextView deadline = view.findViewById(R.id.taskDeadline);
        if (task.getDeadlineMillis() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.US);
            deadline.setText("Due: " + sdf.format(new Date(task.getDeadlineMillis())));
            deadline.setVisibility(View.VISIBLE);
        } else {
            deadline.setVisibility(View.GONE);
        }

        // Persistent badge
        TextView persistentBadge = view.findViewById(R.id.persistentBadge);
        if (task.isPersistent()) {
            persistentBadge.setVisibility(View.VISIBLE);
        }

        // Priority badge
        TextView priorityBadge = view.findViewById(R.id.priorityBadge);
        int score = (int) PriorityCalculator.calculatePriorityScore(task);
        priorityBadge.setText("Score: " + score);

        // Attribute bars
        int effectiveUrgency = PriorityCalculator.getEffectiveUrgency(task);

        ProgressBar urgencyBar = view.findViewById(R.id.urgencyBar);
        urgencyBar.setProgress(effectiveUrgency);
        ((TextView) view.findViewById(R.id.urgencyValue)).setText(String.valueOf(effectiveUrgency));

        ProgressBar importanceBar = view.findViewById(R.id.importanceBar);
        importanceBar.setProgress(task.getImportance());
        ((TextView) view.findViewById(R.id.importanceValue)).setText(String.valueOf(task.getImportance()));

        ProgressBar desireBar = view.findViewById(R.id.desireBar);
        desireBar.setProgress(task.getDesire());
        ((TextView) view.findViewById(R.id.desireValue)).setText(String.valueOf(task.getDesire()));

        ProgressBar creativeBar = view.findViewById(R.id.creativeBar);
        creativeBar.setProgress(task.getCreative());
        ((TextView) view.findViewById(R.id.creativeValue)).setText(String.valueOf(task.getCreative()));

        return view;
    }

    private void setupTouchListener(View card) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    dX = card.getX() - startX;
                    dY = card.getY() - startY;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX;
                    float moveDistance = event.getRawX() - startX;
                    card.setX(newX);
                    card.setRotation(moveDistance * ROTATION_FACTOR);

                    // Show/hide swipe labels
                    TextView rightLabel = card.findViewById(R.id.swipeRightLabel);
                    TextView leftLabel = card.findViewById(R.id.swipeLeftLabel);

                    if (moveDistance > 50) {
                        rightLabel.setVisibility(View.VISIBLE);
                        rightLabel.setAlpha(Math.min(moveDistance / SWIPE_THRESHOLD, 1f));
                        leftLabel.setVisibility(View.GONE);
                    } else if (moveDistance < -50) {
                        leftLabel.setVisibility(View.VISIBLE);
                        leftLabel.setAlpha(Math.min(Math.abs(moveDistance) / SWIPE_THRESHOLD, 1f));
                        rightLabel.setVisibility(View.GONE);
                    } else {
                        rightLabel.setVisibility(View.GONE);
                        leftLabel.setVisibility(View.GONE);
                    }

                    // Scale up next card as current is swiped
                    if (nextCard != null) {
                        float progress = Math.min(Math.abs(moveDistance) / SWIPE_THRESHOLD, 1f);
                        nextCard.setScaleX(0.95f + 0.05f * progress);
                        nextCard.setScaleY(0.95f + 0.05f * progress);
                        nextCard.setAlpha(0.7f + 0.3f * progress);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    float finalDistance = event.getRawX() - startX;

                    if (Math.abs(finalDistance) > SWIPE_THRESHOLD) {
                        animateSwipeOff(card, finalDistance > 0);
                    } else {
                        resetCard(card);
                    }
                    return true;
            }
            return false;
        });
    }

    public void swipeRight() {
        if (currentCard != null) {
            animateSwipeOff(currentCard, true);
        }
    }

    public void swipeLeft() {
        if (currentCard != null) {
            animateSwipeOff(currentCard, false);
        }
    }

    private void animateSwipeOff(View card, boolean toRight) {
        float targetX = toRight ? container.getWidth() * 2 : -container.getWidth() * 2;
        float targetRotation = toRight ? 30 : -30;

        card.animate()
                .x(targetX)
                .rotation(targetRotation)
                .alpha(0)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Task swipedTask = tasks.get(currentIndex);
                        currentIndex++;
                        if (toRight) {
                            listener.onSwipeRight(swipedTask);
                        } else {
                            listener.onSwipeLeft(swipedTask);
                        }
                        loadCards();
                    }
                })
                .start();
    }

    private void resetCard(View card) {
        card.animate()
                .x(0)
                .rotation(0)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator())
                .setListener(null)
                .start();

        card.findViewById(R.id.swipeRightLabel).setVisibility(View.GONE);
        card.findViewById(R.id.swipeLeftLabel).setVisibility(View.GONE);

        if (nextCard != null) {
            nextCard.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .alpha(0.7f)
                    .setDuration(200)
                    .start();
        }
    }
}
