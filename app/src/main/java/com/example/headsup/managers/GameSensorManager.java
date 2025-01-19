package com.example.headsup.managers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


// this file handles the game sensors for tilting up and down during the game
public class GameSensorManager implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final SensorCallback callback;

    // Constants for tilt states
    public static final int STATE_NORMAL = 0;
    public static final int STATE_BETWEEN = -1;
    public static final int STATE_TILTED_UP = 1;
    public static final int STATE_TILTED_DOWN = 2;
    private static final float ROTATION_THRESHOLD = 8f;

    private int currentTiltState = STATE_NORMAL;
    private long lastUpdateTime = 0;
    private static final long MINIMUM_UPDATE_INTERVAL = 500;
    private boolean isEnabled = true;
    private boolean isGameStarted = false;
    private boolean isCountdownStarted = false;

//    this callback just handles different states and countdowns
    public interface SensorCallback {
        void onTiltUp();
        void onTiltDown();
        void onNormalPosition();
        void onInvalidPosition(float currentTilt);
        void onStartCountdown();
        void onCancelCountdown();
        void onPreGameTilt();
    }

    public GameSensorManager(Context context, SensorCallback callback) {
        this.callback = callback;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

//    these are used to start and stop the sensor (during resume and pause states)
    public void startListening() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("HeadsUpSensorManager", "Accelerometer sensor not available on this device");
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public void setGameStarted(boolean started) {
        this.isGameStarted = started;
    }

    public void setCountdownStarted(boolean started) {
        this.isCountdownStarted = started;
    }

    public void resetTiltState() {
        currentTiltState = STATE_NORMAL;
    }

    // main function that checks for tilt angle every time phone moves
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isEnabled || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float z = event.values[2];
        long currentTime = System.currentTimeMillis();

        // Handle pre-game state
        if (!isGameStarted) {
            handlePreGameState(z);
            return;
        }

        // Determine new tilt state
        int newTiltState;
        if (z > ROTATION_THRESHOLD) {
            newTiltState = STATE_TILTED_UP;
        } else if (z < -ROTATION_THRESHOLD) {
            newTiltState = STATE_TILTED_DOWN;
        } else if (z <= 3.5 || z >= -3.5) {
            newTiltState = STATE_NORMAL;
        } else {
            newTiltState = STATE_BETWEEN;
        }

        // Only process if enough time has passed since last update
        if ((currentTime - lastUpdateTime) >= MINIMUM_UPDATE_INTERVAL) {
            if (newTiltState != currentTiltState) {
                switch (newTiltState) {
                    case STATE_TILTED_UP:
                        callback.onTiltUp();
                        break;
                    case STATE_TILTED_DOWN:
                        callback.onTiltDown();
                        break;
                    case STATE_NORMAL:
                        callback.onNormalPosition();
                        break;
                    case STATE_BETWEEN:
                        callback.onInvalidPosition(z);
                        break;
                }
                lastUpdateTime = currentTime;
            }
            currentTiltState = newTiltState;
        }
    }

    // this is to check whether the phone is on the user's forehead to start the game or not
    private void handlePreGameState(float z) {
        if (z > 5 || z < -3) {
            callback.onPreGameTilt();
            if (isCountdownStarted) {
                callback.onCancelCountdown();
                isCountdownStarted = false;
            }
        } else if (!isCountdownStarted) {
            callback.onStartCountdown();
            isCountdownStarted = true;
        }
    }

    // can be used to tweak the sensor changes better (maybe in the future)
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    public int getCurrentTiltState() {
        return currentTiltState;
    }
}