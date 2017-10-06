package com.mcoresoftware.flashlight_fx.app;

import android.app.Application;
import android.content.Context;

import com.mcoresoftware.flashlight_fx.R;

/**
 * This class provides Android application context.
 */
public class FlashlightApplication extends Application {
    /**
     * Logging TAG.
     */
    private static final String LOG_TAG =
            FlashlightApplication.class.getName();

    private static Context               mContext  = null;
    private static FlashlightApplication mInstance = null;

    @Override
    public void onCreate() {
        if (null == mInstance) {
            mInstance = this;
            mContext  = mInstance.getApplicationContext();
        }
        super.onCreate();
    }

    public static void setContext(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Context getContext() {
        if (null == mInstance || null == mContext) {
            throw new RuntimeException(String.valueOf(R.string.error_context));
        }
        return mContext;
    }
}