package com.addie.xcall.ui;

import android.app.Application;

import com.addie.xcall.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Required to apply font to the application
 */
public class xCallApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/ubuntu_light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
