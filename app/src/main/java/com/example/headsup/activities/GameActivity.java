package com.example.headsup.activities;

import android.Manifest;
import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.headsup.R;
import com.example.headsup.managers.APIManager;
import com.example.headsup.managers.GameSensorManager;
import com.example.headsup.managers.SoundManager;
import com.example.headsup.models.WordResult;
import com.example.headsup.animation.CardFlipAnimator;
import com.example.headsup.animation.CircularTimerView;
import com.example.headsup.animation.ParticleSystem;
import com.example.headsup.animation.ShakeAnimator;
import com.example.headsup.managers.CameraManager;

import java.util.ArrayList;


public class GameActivity extends AppCompatActivity{
    // text views
    private TextView wordText;
    private TextView timerText;
    // layouts
    private RelativeLayout mainLayout;

    // managers
    private GameSensorManager sensorManager;
    private CameraManager cameraManager;
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
    private CountDownTimer gameTimer;
    private long timeLeftInMillis = 60000;
    private boolean countdownStarted = false;
    private CountDownTimer startCountdown;
    private boolean wordGuessed = false;
    private int score = 0;
    private final ArrayList<WordResult> wordResults = new ArrayList<>();
    private long wordStartTime;
    private int correctWords = 0;
    private int passedWords = 0;

    // API
    private String binId;

    // New animation components
    private CardFlipAnimator cardFlipAnimator;
    private CircularTimerView circularTimer;
    private ParticleSystem particleSystem;
    private ShakeAnimator shakeAnimator;
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
        PreviewView cameraPreview = findViewById(R.id.viewFinder);

        cameraManager = new CameraManager(this, cameraPreview);

        // Replace camera setup with CameraManager initialization
        if (checkPermissions()) {
        cameraManager.startCamera(this);
        } else{
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }

        // init other elements (sensors, apis, animations and views)
        initializeViews();
        initializeAnimations();
        setupSensors();
        apiManager = new APIManager();
        apiManager.setupRetrofitAsync(binId, wordList -> {
            words = wordList;
        });
    }

    private boolean checkPermissions() {
        boolean cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean audioGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!cameraGranted || !audioGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    },
                    CAMERA_PERMISSION_REQUEST
            );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cameraManager.startCamera(this);
            }
        }
    }

    private void initializeViews() {
        try {
            // initializing all relevant views
            mainLayout = findViewById(R.id.mainLayout);
            wordText = findViewById(R.id.wordText);
            timerText = findViewById(R.id.timerView);
            cardContainer = findViewById(R.id.cardContainer);
            circularTimer = findViewById(R.id.circularTimer);
            particleSystem = findViewById(R.id.particleSystem);

            // getting vibrator
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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




    private void initializeAnimations() {
        try {
            if (cardContainer != null && wordText != null) {
                cardFlipAnimator = new CardFlipAnimator(cardContainer, wordText);
                shakeAnimator = new ShakeAnimator(cardContainer);
                
                // Configure animations
                cardFlipAnimator.setFlipDuration(400);
                shakeAnimator.setDuration(500);
                shakeAnimator.setIntensity(25f);
            }
        } catch (Exception e) {
            Log.e("GameActivity", "Error initializing animations", e);
        }
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
                sensorManager.setGameStarted(true);
                startGame();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.startListening();
        sensorManager.resetTiltState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.stopListening();
        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showNextWord() {
        if (!isGameRunning || cardFlipAnimator == null || words == null || words.isEmpty()) {
            return;
        }

        mainLayout.setBackground(getResources().getDrawable(R.drawable.card_background));
        if (currentWordIndex < words.size()) {
            wordStartTime = System.currentTimeMillis();
            String nextWord = words.get(currentWordIndex);
            cardFlipAnimator.flipToNext(nextWord, null);
            currentWordIndex++;

            // Resume final sound if needed
            if (soundManager.shouldPlayFinalSound()  && !soundManager.isFinalSoundPlaying() && timeLeftInMillis / 1000 < 11) {
                soundManager.playFinalSound();
            }
        } else {
            showGameResults();
        }
    }

    private void startGame() {
        isGameRunning = true;
        currentWordIndex = 0;
        showNextWord();
        startTimer();
        if (cameraManager != null) {
            cameraManager.startRecording();
        }
    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new CountDownTimer(timeLeftInMillis, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                @SuppressLint("DefaultLocale") String timeStr = String.format("0:%02d", millisUntilFinished / 1000);
                timerText.setText(timeStr);
                circularTimer.setProgress(millisUntilFinished / (float) 60000);
                
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
    }

    private void pauseTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
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
            wordResults.add(new WordResult(word, true, timeTaken));
            score += 10;
            correctWords++;
        }
        
        // Emit confetti particles with post delay to ensure view is measured
        if (particleSystem != null) {
            particleSystem.post(() -> {
                float centerX = particleSystem.getWidth() / 2f;
                float centerY = particleSystem.getHeight() / 2f;
                particleSystem.emitParticles(centerX, centerY, true);
            });
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
            wordResults.add(new WordResult(word, false, timeTaken));
            passedWords++;
        }
        
        // Emit X particles and shake with post delay
        if (particleSystem != null && shakeAnimator != null) {
            particleSystem.post(() -> {
                float centerX = particleSystem.getWidth() / 2f;
                float centerY = particleSystem.getHeight() / 2f;
                particleSystem.emitParticles(centerX, centerY, false);
                shakeAnimator.shake();
            });
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
        circularTimer.setProgress(0f);
        circularTimer.stopPulseAnimation();
        if (cameraManager != null) {
            cameraManager.stopRecording();
            videoFilePath = cameraManager.getVideoFilePath();
        }

        new Handler().postDelayed(this::showGameResults, 1000);
    }

    private void showGameResults() {
        Intent intent = new Intent(this, GameResultsActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("wordsAttempted", correctWords + passedWords);
        intent.putExtra("correctWords", correctWords);
        intent.putExtra("passedWords", passedWords);
        intent.putExtra("wordResults", wordResults);
        intent.putExtra("binId", getIntent().getStringExtra("binId"));
        intent.putExtra("score", score); // Your existing score
        intent.putExtra("videoPath", videoFilePath); // Add the video path
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraManager != null) {
            cameraManager.shutdown();
        }
        // Release MediaPlayer resources
        if (soundManager != null) {
            soundManager.release();
        }
    }
}