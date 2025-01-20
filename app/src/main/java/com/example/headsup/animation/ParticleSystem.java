package com.example.headsup.animation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem extends View {
    private static final int POOL_SIZE = 100;
    private static final int PARTICLE_LIFETIME = 1500; // milliseconds
    private static final float PARTICLE_SIZE = 15f;
    
    private List<Particle> activeParticles;
    private List<Particle> particlePool;
    private Random random;
    private Paint particlePaint;
    private boolean isConfetti;
    private Path xPath;

    public ParticleSystem(Context context) {
        super(context);
        init();
    }

    public ParticleSystem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleSystem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        activeParticles = new ArrayList<>();
        particlePool = new ArrayList<>(POOL_SIZE);
        random = new Random();
        
        // Initialize particle pool
        for (int i = 0; i < POOL_SIZE; i++) {
            particlePool.add(new Particle());
        }

        // Setup paint for particles
        particlePaint = new Paint();
        particlePaint.setAntiAlias(true);
        
        // Create X shape path for wrong answer particles
        xPath = new Path();
        float size = PARTICLE_SIZE;
        xPath.moveTo(-size, -size);
        xPath.lineTo(size, size);
        xPath.moveTo(size, -size);
        xPath.lineTo(-size, size);
    }

    public void emitParticles(float centerX, float centerY, boolean isCorrect) {
        if (!isAttachedToWindow()) {
            return;
        }
        
        isConfetti = isCorrect;
        int numParticles = isCorrect ? 50 : 20;
        
        for (int i = 0; i < numParticles; i++) {
            if (!particlePool.isEmpty()) {
                Particle particle = particlePool.remove(0);
                setupParticle(particle, centerX, centerY, isCorrect);
                activeParticles.add(particle);
                
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                animator.setDuration(PARTICLE_LIFETIME);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(animation -> {
                    float progress = (float) animation.getAnimatedValue();
                    updateParticle(particle, progress);
                    invalidate();
                });
                animator.start();
            }
        }
    }

    private void setupParticle(Particle particle, float centerX, float centerY, boolean isCorrect) {
        particle.x = centerX;
        particle.y = centerY;
        particle.initialX = centerX;
        particle.initialY = centerY;
        
        // Random angle and velocity
        float angle = random.nextFloat() * 360f;
        float velocity = random.nextFloat() * 500f + 200f;
        particle.velocityX = (float) (Math.cos(Math.toRadians(angle)) * velocity);
        particle.velocityY = (float) (Math.sin(Math.toRadians(angle)) * velocity);
        
        if (isCorrect) {
            // Confetti colors
            int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA};
            particle.color = colors[random.nextInt(colors.length)];
        } else {
            // Red X particles
            particle.color = Color.RED;
        }
        
        particle.rotation = random.nextFloat() * 360f;
        particle.rotationSpeed = random.nextFloat() * 720f - 360f;
    }

    private void updateParticle(Particle particle, float progress) {
        // Update position with physics
        float time = progress * (PARTICLE_LIFETIME / 1000f);
        particle.x = particle.initialX + particle.velocityX * time;
        particle.y = particle.initialY + particle.velocityY * time + (500f * time * time); // Add gravity
        particle.rotation += particle.rotationSpeed * time;
        particle.alpha = 1f - progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (Particle particle : new ArrayList<>(activeParticles)) {
            particlePaint.setColor(particle.color);
            particlePaint.setAlpha((int) (255 * particle.alpha));
            
            canvas.save();
            canvas.translate(particle.x, particle.y);
            canvas.rotate(particle.rotation);
            
            if (isConfetti) {
                // Draw confetti rectangle
                canvas.drawRect(-PARTICLE_SIZE/2, -PARTICLE_SIZE/4, 
                              PARTICLE_SIZE/2, PARTICLE_SIZE/4, particlePaint);
            } else {
                // Draw X shape
                canvas.drawPath(xPath, particlePaint);
            }
            
            canvas.restore();
        }
    }

    private static class Particle {
        float x, y;
        float initialX, initialY;
        float velocityX, velocityY;
        float rotation;
        float rotationSpeed;
        float alpha = 1f;
        int color;
    }

    public void clear() {
        // Return all active particles to pool
        particlePool.addAll(activeParticles);
        activeParticles.clear();
        invalidate();
    }
} 