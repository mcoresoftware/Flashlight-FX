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

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.mcoresoftware.flashlight_fx.MainActivity;
import com.mcoresoftware.flashlight_fx.R;

public class SplashActivity extends Activity {
    /**
     * Logging TAG.
     */
    private static final String LOG_TAG = SplashActivity.class.getName();

    private static final long DEF_SPLASH_DURATION = 2500L;

    private Handler  mHandler;
    private Runnable mRunnable;

    private SplashAnimView mSplashView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the Android version is lower than Jellybean use this call
        // to hide the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();

            // Hide the status bar.
            int ui = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(ui);
        }

        if (null == mSplashView) {
            mSplashView = (SplashAnimView)findViewById(R.id.splash_view);
        }

        // show the splash screen.
        setContentView(R.layout.activity_splash);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                dismissSplash();
            }
        };

        // allow user to click and dismiss the splash screen prematurely.
        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSplash();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, DEF_SPLASH_DURATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    private void dismissSplash() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
