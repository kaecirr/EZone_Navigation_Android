package com.example.kaelansinclair.ezone_navigation_android;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * This class is to allow compatibility with older Android devices, which require an application
 * class to be defined.
 */

public class EZoneNavigationApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
