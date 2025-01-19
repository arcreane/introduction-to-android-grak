package com.example.headsup.managers;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraManager {
    private static final String TAG = "CameraManager";
    private final Context context;
    private final PreviewView previewView;
    private final ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;
    private String videoFilePath;
    private boolean isRecording = false;

    public CameraManager(Context context, PreviewView previewView) {
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void startCamera(LifecycleOwner lifecycleOwner) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(lifecycleOwner);
            } catch (Exception e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindPreview(LifecycleOwner lifecycleOwner) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build();
        videoCapture = VideoCapture.withOutput(recorder);

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
            );

        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    @SuppressLint("MissingPermission")
    public void startRecording() {
        if (isRecording || videoCapture == null) {
            Log.e(TAG, "Can't start recording - recorder not initialized or already recording");
            return;
        }

        try {
            File movieDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "HeadsUp");
            if (!movieDir.exists()) {
                movieDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            videoFilePath = new File(movieDir, "HeadsUp_" + timestamp + ".mp4").getAbsolutePath();

            FileOutputOptions fileOutputOptions = new FileOutputOptions.Builder(new File(videoFilePath))
                    .build();

            currentRecording = videoCapture.getOutput()
                    .prepareRecording(context, fileOutputOptions)
                    .withAudioEnabled()
                    .start(ContextCompat.getMainExecutor(context), videoRecordEvent -> {
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            isRecording = true;
                            Log.d(TAG, "Recording started successfully");
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                            if (!finalizeEvent.hasError()) {
                                Log.d(TAG, "Recording completed successfully");
                            } else {
                                String msg = "Error recording video: " + finalizeEvent.getError();
                                Log.e(TAG, msg);
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                            isRecording = false;
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error starting recording: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        currentRecording.stop();
        currentRecording = null;
        isRecording = false;
    }

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void shutdown() {
        
        cameraExecutor.shutdown();
    }
} 