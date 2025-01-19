package com.example.headsup.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.headsup.R;
import com.example.headsup.managers.APIManager;
import com.example.headsup.managers.GameSensorManager;
import com.example.headsup.managers.SoundManager;



import java.util.ArrayList;


public class GameActivity extends AppCompatActivity{
    // text views
    private TextView wordText;
    private TextView timerText;


    // layouts
    private RelativeLayout mainLayout;

    // managers
    private GameSensorManager sensorManager;
    private SoundManager soundManager;

    private APIManager apiManager;
    // vibrators
    private Vibrator vibrator;
    // camera
    private static final int CAMERA_PERMISSION_REQUEST = 100;
//    private SurfaceView cameraPreview;

    private String videoFilePath;

    // game elements
    private ArrayList<String> words;
    private int currentWordIndex = 0;
    private boolean isGameRunning = true;
    private boolean timerRunning = false;
    private CountDownTimer gameTimer;
    private long timeLeftInMillis = 60000;
    private boolean gameStarted = false;
    private boolean countdownStarted = false;
    private CountDownTimer startCountdown;
    private boolean wordGuessed = false;
    private int score = 0;
    private long wordStartTime;
    private int correctWords = 0;
    private int passedWords = 0;

    // API
    private String binId;


    private FrameLayout cardContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // using game activity_game.xml file
        setContentView(R.layout.activity_game);
        binId = getIntent().getStringExtra("binId");

        // force landscape orientation during the game
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // setting up camera preview
//        cameraPreview = findViewById(R.id.cameraPreview);


        // init other elements (sensors, apis, animations and views)
        initializeViews();
        setupSensors();
        apiManager = new APIManager();
        apiManager.setupRetrofitAsync(binId, wordList -> {
            words = wordList;
        });
    }

    private void initializeViews() {
        try {
            // initializing all relevant views
            mainLayout = findViewById(R.id.mainLayout);
            wordText = findViewById(R.id.wordText);
            timerText = findViewById(R.id.timerView);
            cardContainer = findViewById(R.id.cardContainer);

            // getting vibrator
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            // initialize sound effects
//            countdownSound = MediaPlayer.create(this, R.raw.beep);
//            startSound = MediaPlayer.create(this, R.raw.startgame);
//            correctSound = MediaPlayer.create(this, R.raw.correct);
//            passSound = MediaPlayer.create(this, R.raw.pass);
//            finalSound = MediaPlayer.create(this, R.raw.final10);
            soundManager = new SoundManager(this);

            // set default welcome message
            wordText.setText(getString(R.string.welcome_messsage_game));

            // shadow effect for card
            cardContainer.setElevation(0f);

        } catch (Exception e) {
            Log.e("GameActivity", "Error initializing views", e);
            Toast.makeText(this, "Error initializing game", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private void setupSensors() {
        sensorManager = new GameSensorManager(this, new GameSensorManager.SensorCallback() {
            @Override
            public void onTiltUp() {
                if (!wordGuessed && isGameRunning) {
                    handleWrongGuess();
                    wordGuessed = true;
                }
            }

            @Override
            public void onTiltDown() {
                if (!wordGuessed && isGameRunning) {
                    handleCorrectGuess();
                    wordGuessed = true;
                }
            }

            @Override
            public void onNormalPosition() {
                if (wordGuessed && isGameRunning) {
                    showNextWord();
                    startTimer();
                    wordGuessed = false;
                }
            }

            @Override
            public void onInvalidPosition(float currentTilt) {
                // Handle invalid position if needed
            }

            @Override
            public void onStartCountdown() {
                if (!countdownStarted) {
                    startCountdownTimer();
                }
            }

            @Override
            public void onCancelCountdown() {
                if (startCountdown != null) {
                    startCountdown.cancel();
                    countdownStarted = false;
                    wordText.setText(getString(R.string.welcome_messsage_game));
                }
            }

            @Override
            public void onPreGameTilt() {
                wordText.setText(getString(R.string.welcome_messsage_game));
            }
        });
    }

    private void startCountdownTimer() {
        if (countdownStarted) {
            return; // Prevent multiple countdown timers
        }

        countdownStarted = true;
        sensorManager.setCountdownStarted(true);
        wordText.setText("3");
        soundManager.playCountdownSound();

        startCountdown = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);
                if (second > 0) {
                    wordText.setText(String.valueOf(second));
                    if (!soundManager.isCountdownSoundPlaying() && second <= 3) {
                        soundManager.playCountdownSound();
                    }
                }
            }

            @Override
            public void onFinish() {
                soundManager.playStartSound();
                gameStarted = true;
                sensorManager.setGameStarted(true);
                startGame();
            }
        }.start();
    }

    // Update onResume():
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.startListening();
        sensorManager.resetTiltState();
    }

    // Update onPause():
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.stopListening();
        if (gameTimer != null) {
            gameTimer.cancel();
            timerRunning = false;
        }
    }

    private void showNextWord() {
        if (!isGameRunning || words == null || words.isEmpty()) {
            return;
        }

        mainLayout.setBackground(getResources().getDrawable(R.drawable.card_background));
        if (currentWordIndex < words.size()) {
            wordStartTime = System.currentTimeMillis();
            String nextWord = words.get(currentWordIndex);
            currentWordIndex++;

            // Resume final sound if needed
            if (soundManager.shouldPlayFinalSound()  && !soundManager.isFinalSoundPlaying() && timeLeftInMillis / 1000 < 11) {
                soundManager.playFinalSound();
            }
        }
    }

    private void startGame() {
        isGameRunning = true;
        currentWordIndex = 0;
        showNextWord();
        startTimer();
    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new CountDownTimer(timeLeftInMillis, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                String timeStr = String.format("0:%02d", millisUntilFinished / 1000);
                timerText.setText(timeStr);

                if (millisUntilFinished / 1000 < 11) {
                    soundManager.setShouldPlayFinalSound(true);
                    if (!wordGuessed && !soundManager.isFinalSoundPlaying()) {
                        soundManager.playFinalSound();
                    }
                }
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
        timerRunning = true;
    }

    private void pauseTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
            timerRunning = false;
        }
    }

    private void handleCorrectGuess() {
        if (!isGameRunning) return;

        pauseTimer();

        soundManager.playCorrectSound();

        if (soundManager.isFinalSoundPlaying()) {
            soundManager.pauseFinalSound();
        }
        mainLayout.setBackground(getResources().getDrawable(R.drawable.correct_overlay));
        wordText.setText(getString(R.string.correct_message));

        // Record word result
        if (currentWordIndex > 0) {
            String word = words.get(currentWordIndex - 1);
            long timeTaken = System.currentTimeMillis() - wordStartTime;
            score += 10;
            correctWords++;
        }


        if (vibrator != null) {
            long[] pattern = {0, 250, 100, 250};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }

    private void handleWrongGuess() {
        if (!isGameRunning) return;


        pauseTimer();
        soundManager.playPassSound();
        if (soundManager.isFinalSoundPlaying()) {
            soundManager.pauseFinalSound();
        }
        wordText.setText(getString(R.string.pass_message));
        mainLayout.setBackground(getResources().getDrawable(R.drawable.wrong_overlay));

        // Record word result
        if (currentWordIndex > 0) {
            String word = words.get(currentWordIndex - 1);
            long timeTaken = System.currentTimeMillis() - wordStartTime;
            passedWords++;
        }

        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void endGame() {
        isGameRunning = false;
        long[] pattern = {0, 100, 100, 100, 250};
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        timerText.setText(getString(R.string.endgame_timer));
        wordText.setText(getString(R.string.endgame_message));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release MediaPlayer resources
        if (soundManager != null) {
            soundManager.release();
        }
    }
}