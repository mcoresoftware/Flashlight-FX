/////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2017 MCORE Innovation Software. All rights reserved.
//
// Licensed under the MIT License (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the
// License at
//
// http://opensource.org/licenses/MIT
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations
// under the License.
/////////////////////////////////////////////////////////////////////////////
package com.mcoresoftware.flashlight_fx.screen.splash;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.mcoresoftware.flashlight_fx.R;
import com.mcoresoftware.flashlight_fx.app.FlashlightApplication;

public class SplashAnimView extends View {
    /**
     * Logging TAG.
     */
    private static final String LOG_TAG = SplashAnimView.class.getName();

    private int   mCanvasWidth  = -1;
    private int   mCanvasHeight = -1;
    private float mCenterX;
    private float mCenterY;

    private float mCircleRadius;
    private float mRotationRadius;

    private int[] mCircleColors;
    private float mCurrentRotationAngle = 0.F;
    private float mCurrentRotationRadius;

    private ValueAnimator mAnimator;

    private Paint mPaint           = new Paint();
    private Paint mPaintBackground = new Paint();

    /**
     * Context constructor.
     * @param context
     */
    public SplashAnimView(Context context) {
        super(context);
        initAnimView();
    }

    /**
     * Context and attributes constructor.
     * @param context
     * @param attrs
     */
    public SplashAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnimView();
        initAnimAttr(attrs);
    }

    /**
     * Context, attributes, and style constructor.
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public SplashAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnimView();
        initAnimAttr(attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        mCanvasWidth  = w;
        mCanvasHeight = h;
        mCenterX      = w * 0.5F;
        mCenterY      = h * 0.5F;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCircleColors == null) {
            mCircleColors = new int[3];

            mCircleColors[0] = Color.parseColor("#C70039");
            mCircleColors[1] = Color.parseColor("#FF5733");
            mCircleColors[2] = Color.parseColor("#FFC30F");
        }

        drawBackground(canvas);
        drawCircles(canvas);
    }

    private void drawCircles(Canvas canvas) {
        int numCircles = 3;
        float rotationAngle = (float)(2 * Math.PI / numCircles);
        for (int i = 0; i < numCircles; ++i) {
            double angle = mCurrentRotationAngle + (i * rotationAngle);
            double circleX = mCenterX + mCurrentRotationRadius * Math.sin(angle);
            double circleY = mCenterY - mCurrentRotationRadius * Math.cos(angle);

            mPaint.setColor(mCircleColors[i]);
            canvas.drawCircle((float)circleX, (float)circleY,
                    mCircleRadius, mPaint);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#F8F6EC"));
    }

    private void initAnimAttr(AttributeSet attrs) {
        TypedArray ta = FlashlightApplication.getContext()
                .obtainStyledAttributes(attrs, R.styleable.SplashAnimView);

        mCircleRadius = ta.getDimensionPixelSize(
                R.styleable.SplashAnimView_circleRadius, 6);
        mRotationRadius = ta.getDimensionPixelSize(
                R.styleable.SplashAnimView_rotationRadius, 20);

        ta.recycle();

        mCurrentRotationRadius = mRotationRadius;
    }

    private void initAnimView() {
        setBackgroundColor(Color.TRANSPARENT);

        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setColor(getContext().getResources()
                .getColor(R.color.splash_base));

        mPaint.setAntiAlias(true);

        // set-up animation:
        mAnimator = ValueAnimator.ofFloat(0, (float)(Math.PI * 2));
        mAnimator.setDuration(750);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentRotationAngle = (Float)animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.start();
    }
}
