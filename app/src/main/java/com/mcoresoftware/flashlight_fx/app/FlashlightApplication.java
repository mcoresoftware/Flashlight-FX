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