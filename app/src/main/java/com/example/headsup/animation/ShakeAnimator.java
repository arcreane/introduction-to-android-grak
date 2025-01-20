package com.example.headsup.animation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ShakeAnimator {
    private static final int DEFAULT_DURATION = 500;
    private static final float DEFAULT_INTENSITY = 25f;
    private static final int DEFAULT_SHAKES = 4;
    
    private int duration;
    private float intensity;
    private int numShakes;
    private View targetView;
    private ObjectAnimator currentAnimation;

    public ShakeAnimator(View view) {
        this.targetView = view;
        this.duration = DEFAULT_DURATION;
        this.intensity = DEFAULT_INTENSITY;
        this.numShakes = DEFAULT_SHAKES;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public void setNumShakes(int numShakes) {
        this.numShakes = numShakes;
    }

    public void shake() {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.cancel();
        }

        // Store original position
        float originalX = targetView.getTranslationX();
        float originalY = targetView.getTranslationY();

        // Create shake animation
        float[] xValues = new float[numShakes * 2 + 1];
        float[] yValues = new float[numShakes * 2 + 1];
        
        xValues[0] = originalX;
        yValues[0] = originalY;
        
        for (int i = 1; i <= numShakes * 2; i++) {
            float progress = (float) i / (numShakes * 2);
            float offset = (float) Math.sin(progress * Math.PI * numShakes) * intensity;
            
            xValues[i] = originalX + offset;
            yValues[i] = originalY + (offset * 0.5f); // Smaller vertical movement
        }

        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, xValues);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, yValues);

        currentAnimation = ObjectAnimator.ofPropertyValuesHolder(targetView, pvhX, pvhY);
        currentAnimation.setDuration(duration);
        currentAnimation.setInterpolator(new DecelerateInterpolator());
        
        // Add rotation for more natural feel
        PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofFloat(
            View.ROTATION,
            0f, -2f, 2f, -2f, 2f, -1f, 1f, -1f, 1f, 0f
        );
        currentAnimation.setValues(pvhX, pvhY, pvhRotation);

        // Reset to original position when done
        currentAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                targetView.setTranslationX(originalX);
                targetView.setTranslationY(originalY);
                targetView.setRotation(0f);
            }
        });

        currentAnimation.start();
    }

    public void cancel() {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.cancel();
        }
    }
} 