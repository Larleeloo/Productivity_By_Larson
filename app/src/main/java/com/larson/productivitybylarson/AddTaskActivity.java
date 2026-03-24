package com.larson.productivitybylarson;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.larson.productivitybylarson.database.TaskDatabase;
import com.larson.productivitybylarson.dao.TaskDao;
import com.larson.productivitybylarson.model.Task;
import com.larson.productivitybylarson.util.NotificationHelper;
import com.larson.productivitybylarson.util.PriorityCalculator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText editTitle, editDescription;
    private ImageView taskImagePreview;
    private TextView addImageHint;
    private Slider sliderUrgency, sliderImportance, sliderDesire, sliderCreative;
    private TextView urgencyValueText, importanceValueText, desireValueText, creativeValueText;
    private SwitchMaterial switchDeadline;
    private LinearLayout deadlineSection;
    private TextView deadlinePreview;
    private LinearLayout wellbeingEncouragement;

    private String selectedImagePath = null;
    private String selectedColor = "#4CAF50";
    private Calendar deadlineCalendar = Calendar.getInstance();
    private boolean dateSet = false;
    private boolean timeSet = false;

    private TaskDao taskDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Color dot view references and their hex codes
    private final String[] colorHexValues = {
            "#F44336", "#FF9800", "#FFEB3B", "#4CAF50",
            "#2196F3", "#9C27B0", "#E91E63"
    };
    private final int[] colorViewIds = {
            R.id.colorRed, R.id.colorOrange, R.id.colorYellow, R.id.colorGreen,
            R.id.colorBlue, R.id.colorPurple, R.id.colorPink
    };
    private View selectedColorView;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImagePath = copyImageToAppStorage(imageUri);
                        if (selectedImagePath != null) {
                            Glide.with(this).load(new File(selectedImagePath)).centerCrop().into(taskImagePreview);
                            addImageHint.setVisibility(View.GONE);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskDao = TaskDatabase.getInstance(this).taskDao();

        initViews();
        setupSliders();
        setupColorPicker();
        setupDeadline();
        setupImagePicker();
        setupSaveButton();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        taskImagePreview = findViewById(R.id.taskImagePreview);
        addImageHint = findViewById(R.id.addImageHint);
        sliderUrgency = findViewById(R.id.sliderUrgency);
        sliderImportance = findViewById(R.id.sliderImportance);
        sliderDesire = findViewById(R.id.sliderDesire);
        sliderCreative = findViewById(R.id.sliderCreative);
        urgencyValueText = findViewById(R.id.urgencyValueText);
        importanceValueText = findViewById(R.id.importanceValueText);
        desireValueText = findViewById(R.id.desireValueText);
        creativeValueText = findViewById(R.id.creativeValueText);
        switchDeadline = findViewById(R.id.switchDeadline);
        deadlineSection = findViewById(R.id.deadlineSection);
        deadlinePreview = findViewById(R.id.deadlinePreview);
        wellbeingEncouragement = findViewById(R.id.wellbeingEncouragement);
    }

    private void setupSliders() {
        sliderUrgency.addOnChangeListener((slider, value, fromUser) -> {
            urgencyValueText.setText(String.valueOf((int) value));
            checkWellbeingEncouragement();
        });
        sliderImportance.addOnChangeListener((slider, value, fromUser) -> {
            importanceValueText.setText(String.valueOf((int) value));
        });
        sliderDesire.addOnChangeListener((slider, value, fromUser) -> {
            desireValueText.setText(String.valueOf((int) value));
            checkWellbeingEncouragement();
        });
        sliderCreative.addOnChangeListener((slider, value, fromUser) -> {
            creativeValueText.setText(String.valueOf((int) value));
            checkWellbeingEncouragement();
        });
    }

    private void checkWellbeingEncouragement() {
        boolean noDeadline = !switchDeadline.isChecked();
        boolean highDesire = sliderDesire.getValue() >= 7;
        boolean highCreative = sliderCreative.getValue() >= 7;

        if (noDeadline && (highDesire || highCreative)) {
            wellbeingEncouragement.setVisibility(View.VISIBLE);
        } else {
            wellbeingEncouragement.setVisibility(View.GONE);
        }
    }

    private void setupColorPicker() {
        for (int i = 0; i < colorViewIds.length; i++) {
            View colorView = findViewById(colorViewIds[i]);
            final String color = colorHexValues[i];
            final int index = i;

            colorView.setOnClickListener(v -> {
                selectedColor = color;
                // Update selection indicator
                if (selectedColorView != null) {
                    selectedColorView.setScaleX(1f);
                    selectedColorView.setScaleY(1f);
                }
                v.setScaleX(1.3f);
                v.setScaleY(1.3f);
                selectedColorView = v;
            });

            // Default selection (green)
            if (color.equals("#4CAF50")) {
                colorView.setScaleX(1.3f);
                colorView.setScaleY(1.3f);
                selectedColorView = colorView;
            }
        }
    }

    private void setupDeadline() {
        switchDeadline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deadlineSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                deadlinePreview.setVisibility(View.GONE);
                dateSet = false;
                timeSet = false;
            }
            checkWellbeingEncouragement();
        });

        MaterialButton btnPickDate = findViewById(R.id.btnPickDate);
        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                deadlineCalendar.set(Calendar.YEAR, year);
                deadlineCalendar.set(Calendar.MONTH, month);
                deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateSet = true;
                updateDeadlinePreview();
            }, deadlineCalendar.get(Calendar.YEAR),
                    deadlineCalendar.get(Calendar.MONTH),
                    deadlineCalendar.get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMinDate(System.currentTimeMillis());
            dpd.show();
        });

        MaterialButton btnPickTime = findViewById(R.id.btnPickTime);
        btnPickTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                deadlineCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                deadlineCalendar.set(Calendar.MINUTE, minute);
                deadlineCalendar.set(Calendar.SECOND, 0);
                timeSet = true;
                updateDeadlinePreview();
            }, deadlineCalendar.get(Calendar.HOUR_OF_DAY),
                    deadlineCalendar.get(Calendar.MINUTE), false).show();
        });
    }

    private void updateDeadlinePreview() {
        if (dateSet) {
            SimpleDateFormat sdf;
            if (timeSet) {
                sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.US);
            } else {
                sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            }
            deadlinePreview.setText("Deadline: " + sdf.format(deadlineCalendar.getTime()));
            deadlinePreview.setVisibility(View.VISIBLE);
        }
    }

    private void setupImagePicker() {
        taskImagePreview.setOnClickListener(v -> openImagePicker());
        addImageHint.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private String copyImageToAppStorage(Uri uri) {
        try {
            File imagesDir = new File(getFilesDir(), "task_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            File destFile = new File(imagesDir, "task_" + System.currentTimeMillis() + ".jpg");

            InputStream in = getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupSaveButton() {
        MaterialButton btnSave = findViewById(R.id.btnSaveTask);
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
        String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";

        if (title.isEmpty()) {
            editTitle.setError("Title is required");
            return;
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setImagePath(selectedImagePath);
        task.setCategoryColor(selectedColor);

        int urgency = (int) sliderUrgency.getValue();
        task.setBaseUrgency(urgency);
        task.setUrgency(urgency);
        task.setImportance((int) sliderImportance.getValue());
        task.setDesire((int) sliderDesire.getValue());
        task.setCreative((int) sliderCreative.getValue());

        if (switchDeadline.isChecked() && dateSet) {
            long deadlineMillis = deadlineCalendar.getTimeInMillis();
            task.setDeadlineMillis(deadlineMillis);

            // Calculate dynamic urgency based on deadline
            int effectiveUrgency = PriorityCalculator.getEffectiveUrgency(task);
            task.setUrgency(effectiveUrgency);
        }

        executor.execute(() -> {
            long taskId = taskDao.insert(task);

            // Schedule deadline alarms if applicable
            if (task.getDeadlineMillis() != null) {
                runOnUiThread(() -> NotificationHelper.scheduleDeadlineAlarm(
                        this, taskId, task.getTitle(), task.getDeadlineMillis()));
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Task created!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
