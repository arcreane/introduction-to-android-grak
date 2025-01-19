package com.example.headsup.activities;

import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.headsup.R;
import com.example.headsup.adapters.WordHistoryAdapter;
import com.example.headsup.models.WordResult;
import com.example.headsup.animation.ParticleSystem;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class GameResultsActivity extends AppCompatActivity {
    private TextView scoreText;
    private TextView wordsAttemptedText;
    private TextView correctWordsText;
    private TextView passedWordsText;
    private RecyclerView wordHistoryList;
    private ParticleSystem confettiView;
    private MaterialButton playAgainButton;
    private MaterialButton backToDecksButton;
    private String videoFilePath;
    private Button saveVideoButton;
    private WordHistoryAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_results);

        // Initialize views
        initializeViews();
        
        // Get game results from intent
        int finalScore = getIntent().getIntExtra("score", 0);
        int wordsAttempted = getIntent().getIntExtra("wordsAttempted", 0);
        int correctWords = getIntent().getIntExtra("correctWords", 0);
        int passedWords = getIntent().getIntExtra("passedWords", 0);
        ArrayList<WordResult> wordResults = getIntent().getParcelableArrayListExtra("wordResults");

        // Set up RecyclerView
        adapter = new WordHistoryAdapter(wordResults != null ? wordResults : new ArrayList<>());
        wordHistoryList.setLayoutManager(new LinearLayoutManager(this));
        wordHistoryList.setAdapter(adapter);

        // Animate score and stats
        animateNumbers(scoreText, 0, finalScore);
        animateNumbers(wordsAttemptedText, 0, wordsAttempted);
        animateNumbers(correctWordsText, 0, correctWords);
        animateNumbers(passedWordsText, 0, passedWords);

        // Get the video path from intent
        videoFilePath = getIntent().getStringExtra("videoPath");

        // Initialize save video button
        saveVideoButton = findViewById(R.id.saveVideoButton);

        // Only show the button if we have a video
        if (videoFilePath != null) {
            saveVideoButton.setVisibility(View.VISIBLE);
            saveVideoButton.setOnClickListener(v -> saveRecording());
        } else {
            saveVideoButton.setVisibility(View.GONE);
        }

        // Start confetti after a short delay
        handler.postDelayed(() -> {
            if (confettiView != null && !isFinishing()) {
                float centerX = confettiView.getWidth() / 2f;
                float centerY = confettiView.getHeight() / 2f;
                confettiView.emitParticles(centerX, centerY, true);
            }
        }, 500);

        // Set up button clicks
        playAgainButton.setOnClickListener(v -> {
            animateButtonClick(v);
            handler.postDelayed(this::playAgain, 200);
        });

        backToDecksButton.setOnClickListener(v -> {
            animateButtonClick(v);
            handler.postDelayed(this::backToDecks, 200);
        });
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        wordsAttemptedText = findViewById(R.id.wordsAttemptedText);
        correctWordsText = findViewById(R.id.correctWordsText);
        passedWordsText = findViewById(R.id.passedWordsText);
        wordHistoryList = findViewById(R.id.wordHistoryList);
        confettiView = findViewById(R.id.confettiView);
        playAgainButton = findViewById(R.id.playAgainButton);
        backToDecksButton = findViewById(R.id.backToDecksButton);
    }

    private void saveRecording() {
        if (videoFilePath == null) return;

        try {
            File sourceFile = new File(videoFilePath);
            if (!sourceFile.exists()) {
                Toast.makeText(this, "Video file not found", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, "HeadsUp_" + System.currentTimeMillis());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/HeadsUp");

            Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    Files.copy(sourceFile.toPath(), os);
                    Toast.makeText(this, "Video saved successfully!", Toast.LENGTH_SHORT).show();
                    openVideoFolder(); // Optional: open the folder after saving
                }
            }
        } catch (Exception e) {
            Log.e("ScoresActivity", "Error saving video: " + e.getMessage());
            Toast.makeText(this, "Error saving video", Toast.LENGTH_SHORT).show();
        }
        saveVideoButton.setVisibility(View.GONE);
    }

    private void openVideoFolder() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app found to view videos", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateNumbers(TextView textView, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1500);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(value));
        });
        animator.start();
    }

    private void animateButtonClick(View view) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> 
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start())
            .start();
    }

    private void playAgain() {
        String binId = getIntent().getStringExtra("binId");
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("binId", binId);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void backToDecks() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
} 