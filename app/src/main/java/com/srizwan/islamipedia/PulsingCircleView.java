package com.srizwan.islamipedia;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class PulsingCircleView extends View {
    private static final int INNER_CIRCLE_RADIUS = 8;
    private static final int OUTER_CIRCLE_RADIUS = 10;
    private static final int MAX_ALPHA = 255;
    private static final long ANIMATION_DURATION = 1000;

    private Paint innerCirclePaint;
    private Paint outerCirclePaint;
    private int outerCircleRadius;
    private int outerCircleAlpha;
    private ValueAnimator pulseAnimator;

    public PulsingCircleView(Context context) {
        super(context);
        init();
    }

    public PulsingCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(Color.WHITE);
        innerCirclePaint.setStyle(Paint.Style.FILL);

        outerCirclePaint = new Paint();
        outerCirclePaint.setColor(Color.WHITE);
        outerCirclePaint.setAlpha(MAX_ALPHA);
        outerCirclePaint.setStyle(Paint.Style.STROKE);

        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(ANIMATION_DURATION);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                outerCircleRadius = (int) (OUTER_CIRCLE_RADIUS + (OUTER_CIRCLE_RADIUS * fraction));
                outerCircleAlpha = (int) (MAX_ALPHA - (MAX_ALPHA * fraction));
                invalidate();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        pulseAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pulseAnimator.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Draw inner circle
        canvas.drawCircle(centerX, centerY, INNER_CIRCLE_RADIUS, innerCirclePaint);

        // Draw outer pulsing circle
        outerCirclePaint.setAlpha(outerCircleAlpha);
        outerCirclePaint.setStrokeWidth(3f);
        canvas.drawCircle(centerX, centerY, outerCircleRadius, outerCirclePaint);
    }
}
