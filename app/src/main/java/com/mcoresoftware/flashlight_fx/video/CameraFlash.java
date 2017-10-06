package com.mcoresoftware.flashlight_fx.video;

import android.hardware.Camera;
import android.os.Looper;
import android.util.Log;

import com.mcoresoftware.flashlight_fx.video.exceptions.CameraInUseException;

import java.util.concurrent.Semaphore;

public class CameraFlash {
    private final static String LOG_TAG = CameraFlash.class.getName();

    private int    mCameraId = 0;
    private Camera mCamera;
    private Thread mCameraThread;
    private Looper mCameraLooper;

    private boolean mUnlocked = false;

    public CameraFlash() {
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
                    mCamera = Camera.open(mCameraId);
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

    public synchronized void destroyCamera() {
        if (mCamera != null) {
            lockCamera();
            mCamera.stopPreview();
            try {
                mCamera.release();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage() != null ?
                        e.getMessage() : "unknown error");
            }
            mCamera = null;
            mCameraLooper.quit();
            mUnlocked = false;
        }
    }

    private void lockCamera() {
        if (mUnlocked) {
            Log.d(LOG_TAG, "Locking camera");
            try {
                mCamera.reconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            mUnlocked = false;
        }
    }

    private void unlockCamera() {
        if (!mUnlocked) {
            Log.d(LOG_TAG, "Unlocking camera");
            try {
                mCamera.unlock();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            mUnlocked = true;
        }
    }
}
