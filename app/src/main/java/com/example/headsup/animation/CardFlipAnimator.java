package com.example.headsup.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

public class CardFlipAnimator {
    private static final int DEFAULT_FLIP_DURATION = 400;
    private int flipDuration;
    private final View cardView;
    private final TextView wordText;
    private AnimatorSet currentAnimation;

    public CardFlipAnimator(View cardView, TextView wordText) {
        this.cardView = cardView;
        this.wordText = wordText;
        this.flipDuration = DEFAULT_FLIP_DURATION;
        setupInitialState();
    }

    private void setupInitialState() {
        cardView.setCameraDistance(8000);
    }

    public void setFlipDuration(int duration) {
        this.flipDuration = duration;
    }

    public void flipToNext(String nextWord, Runnable onHalfWay) {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.cancel();
        }

        // First half of the flip (current word rotates out)
        ObjectAnimator firstRotation = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f);
        firstRotation.setDuration(flipDuration / 2);
        firstRotation.setInterpolator(new DecelerateInterpolator());

        // Second half of the flip (new word rotates in)
        ObjectAnimator secondRotation = ObjectAnimator.ofFloat(cardView, "rotationY", -90f, 0f);
        secondRotation.setDuration(flipDuration / 2);
        secondRotation.setInterpolator(new AccelerateDecelerateInterpolator());

        // Scale animation
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.8f);
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(cardView, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(cardView, "scaleY", 0.8f, 1f);

        scaleXDown.setDuration(flipDuration / 2);
        scaleYDown.setDuration(flipDuration / 2);
        scaleXUp.setDuration(flipDuration / 2);
        scaleYUp.setDuration(flipDuration / 2);

        // Shadow animation
        ObjectAnimator shadowStart = ObjectAnimator.ofFloat(cardView, "elevation", 0f, 16f);
        ObjectAnimator shadowEnd = ObjectAnimator.ofFloat(cardView, "elevation", 16f, 0f);
        shadowStart.setDuration(flipDuration / 2);
        shadowEnd.setDuration(flipDuration / 2);

        AnimatorSet firstHalf = new AnimatorSet();
        firstHalf.playTogether(firstRotation, scaleXDown, scaleYDown, shadowStart);

        AnimatorSet secondHalf = new AnimatorSet();
        secondHalf.playTogether(secondRotation, scaleXUp, scaleYUp, shadowEnd);

        currentAnimation = new AnimatorSet();
        currentAnimation.playSequentially(firstHalf, secondHalf);

        // Update the text halfway through the animation
        firstHalf.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                wordText.setText(nextWord);
                if (onHalfWay != null) {
                    onHalfWay.run();
                }
            }
        });

        currentAnimation.start();
    }

    public void cancelAnimation() {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.cancel();
        }
    }
} 