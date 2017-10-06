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
package com.mcoresoftware.flashlight_fx;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.mcoresoftware.flashlight_fx.app.FlashlightApplication;
import com.mcoresoftware.flashlight_fx.video.exceptions.CameraInUseException;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    /**
     * Logging TAG.
     */
    private final static String LOG_TAG = MainActivity.class.getName();

    // Camera.
    private Camera mCamera;
    private Thread mCameraThread;
    private Looper mCameraLooper;

    SurfaceHolder mHolder;
    SurfaceView   preview;

    private boolean mFlashEnabled;

    private SwitchButton   mFlashlightControl           = null;
    private boolean        mDoubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == mFlashlightControl) {
            mFlashlightControl = (SwitchButton)findViewById(
                    R.id.sb_flashlight_control);
        }

        if (!checkCameraAndFlashlightHardware()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(
                    MainActivity.this);
            alert.setTitle("Camera not supported?");
            alert.setMessage("Camera feature is not available on this device!");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }

        createCamera();

        mFlashlightControl.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mCamera != null) {
                        toggleFlash();
                        mCamera.startPreview();
                    }
                } else {
                    if (mCamera != null) {
                        toggleFlash();
                        mCamera.stopPreview();
                    }
                }
            }
        });
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mHolder = null;
    }

    /** Toggle the LED of the phone if it has one. */
    private synchronized void toggleFlash() {
        setFlashState(!mFlashEnabled);
    }

    /** Indicates whether or not the flash of the phone is on. */
    private boolean getFlashState() {
        return mFlashEnabled;
    }

    private synchronized void setFlashState(boolean state) {
        // If the camera has already been opened, we apply the change
        // immediately.
        if (mCamera != null) {
            Parameters parameters = mCamera.getParameters();

            // We test if the phone has a flash.
            if (parameters.getFlashMode() == null) {
                // The phone has no flash.
                throw new RuntimeException("Cannot turn the flash on!");
            } else {
                parameters.setFlashMode(state?Parameters.FLASH_MODE_TORCH:Parameters.FLASH_MODE_OFF);
                try {
                    mCamera.setParameters(parameters);
                    mFlashEnabled = state;
                } catch (RuntimeException e) {
                    mFlashEnabled = false;
                    throw new RuntimeException("Cannot turn the flash on!");
                } finally {
                }
            }
        } else {
            mFlashEnabled = state;
        }
    }

    /**
     * Check if this device has a camera and camera flashlight.
     * @return
     */
    private boolean checkCameraAndFlashlightHardware() {
        if (FlashlightApplication.getContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera.
            if (FlashlightApplication.getContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                // this device has a camera and camera flashlight.
                return true;
            } else {
                return false;
            }
        } else {
            // no camera on this device.
            return false;
        }
    }

    private synchronized void createCamera() throws RuntimeException {
        preview = (SurfaceView)findViewById(R.id.PREVIEW);
        mHolder = preview.getHolder();
        mHolder.addCallback(MainActivity.this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (mCamera == null) {
            openCamera();

            try {
                Parameters parameters = mCamera.getParameters();
                try {
                    mCamera.setPreviewDisplay(mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (RuntimeException e) {
                destroyCamera();
                throw e;
            }
        }
    }

    private void openCamera() throws RuntimeException {
        final Semaphore lock = new Semaphore(0);
        final RuntimeException[] exceptions = new RuntimeException[1];
        mCameraThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mCameraLooper = Looper.myLooper();
                try {
                    mCamera = Camera.open();
                } catch (RuntimeException e) {
                    exceptions[0] = e;
                } finally {
                    lock.release();
                    Looper.loop();
                }
            }
        });
        mCameraThread.start();
        lock.acquireUninterruptibly();
        if (exceptions[0] != null) {
            throw new CameraInUseException(exceptions[0].getMessage());
        }
    }

    private synchronized void destroyCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            try {
                mCamera.release();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage() != null ? e.getMessage()
                    : "unknown error");
            }
            mCamera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash.
        if (mCamera != null) {
            setFlashState(false);
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // on resume turn on the flash.
        if (checkCameraAndFlashlightHardware()) {
            setFlashState(getFlashState());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        createCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyCamera();
    }

    @Override
    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit",
                Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
