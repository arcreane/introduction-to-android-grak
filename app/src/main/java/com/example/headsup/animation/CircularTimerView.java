package com.example.headsup.animation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class CircularTimerView extends View {
    private static final float STROKE_WIDTH = 20f;
    private static final long PULSE_DURATION = 500;
    private static final float MAX_PULSE_SCALE = 1.2f;

    private Paint progressPaint;
    private Paint backgroundPaint;
    private RectF circleRect;
    private float progress = 1f;
    private int currentColor;
    private ValueAnimator pulseAnimator;
    private float pulseScale = 1f;
    private boolean isPulsing = false;

    public CircularTimerView(Context context) {
        super(context);
        init();
    }

    public CircularTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(STROKE_WIDTH);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(STROKE_WIDTH);
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setAlpha(100);
        backgroundPaint.setAntiAlias(true);

        circleRect = new RectF();
        setProgress(1f); // Start at full
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateCircleRect();
    }

    private void updateCircleRect() {
        float padding = STROKE_WIDTH / 2f;
        circleRect.set(padding, padding, getWidth() - padding, getHeight() - padding);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        updateColor();
        invalidate();
    }

    private void updateColor() {
        if (progress > 0.6f) {
            currentColor = Color.GREEN;
        } else if (progress > 0.3f) {
            currentColor = Color.YELLOW;
        } else {
            currentColor = Color.RED;
            if (progress <= 0.2f && !isPulsing) {
                startPulseAnimation();
            }
        }
        progressPaint.setColor(currentColor);
    }

    private void startPulseAnimation() {
        isPulsing = true;
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }

        pulseAnimator = ValueAnimator.ofFloat(1f, MAX_PULSE_SCALE);
        pulseAnimator.setDuration(PULSE_DURATION);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            pulseScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    public void stopPulseAnimation() {
        isPulsing = false;
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        pulseScale = 1f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isPulsing) {
            canvas.save();
            float scaleFactor = (pulseScale - 1f) / 2f;
            canvas.scale(pulseScale, pulseScale, getWidth() / 2f, getHeight() / 2f);
        }

        // Draw background circle
        canvas.drawArc(circleRect, 0, 360, false, backgroundPaint);

        // Draw progress arc
        float sweepAngle = progress * 360;
        canvas.drawArc(circleRect, -90, sweepAngle, false, progressPaint);

        if (isPulsing) {
            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPulseAnimation();
    }
}
