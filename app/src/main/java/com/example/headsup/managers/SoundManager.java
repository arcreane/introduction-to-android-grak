package com.example.headsup.managers;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.headsup.R;

public class SoundManager {
    private MediaPlayer countdownSound;
    private MediaPlayer correctSound;
    private MediaPlayer passSound;
    private MediaPlayer finalSound;
    private MediaPlayer startSound;
    private boolean shouldPlayFinalSound = false;
    private final Context context;

    public SoundManager(Context context) {
        this.context = context;
        initializeSounds();
    }

    private void initializeSounds() {
        countdownSound = MediaPlayer.create(context, R.raw.beep);
        startSound = MediaPlayer.create(context, R.raw.startgame);
        correctSound = MediaPlayer.create(context, R.raw.correct);
        passSound = MediaPlayer.create(context, R.raw.pass);
        finalSound = MediaPlayer.create(context, R.raw.final10);
    }
    public void playCountdownSound() {
        if (countdownSound != null) {
            countdownSound.start();
        }
    }

    public boolean isCountdownSoundPlaying() {
        return countdownSound != null && countdownSound.isPlaying();
    }

    public void playStartSound() {
        if (startSound != null) {
            startSound.start();
        }
    }

    public void playCorrectSound() {
        if (correctSound != null) {
            correctSound.start();
        }
    }

    public void playPassSound() {
        if (passSound != null) {
            passSound.start();
        }
    }

    public void playFinalSound() {
        if (finalSound != null && !finalSound.isPlaying()) {
            finalSound.start();
        }
    }


    public void pauseFinalSound() {
        if (finalSound != null && finalSound.isPlaying()) {
            finalSound.pause();
        }
    }

    public boolean isFinalSoundPlaying() {
        return finalSound != null && finalSound.isPlaying();
    }

    public void setShouldPlayFinalSound(boolean should) {
        this.shouldPlayFinalSound = should;
    }

    public boolean shouldPlayFinalSound() {
        return shouldPlayFinalSound;
    }

    public void release() {
        if (countdownSound != null) {
            countdownSound.release();
            countdownSound = null;
        }
        if (startSound != null) {
            startSound.release();
            startSound = null;
        }
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
        if (passSound != null) {
            passSound.release();
            passSound = null;
        }
        if (finalSound != null) {
            finalSound.release();
            finalSound = null;
        }
    }
}
