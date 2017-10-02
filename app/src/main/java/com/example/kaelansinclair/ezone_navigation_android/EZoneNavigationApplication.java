package com.example.kaelansinclair.ezone_navigation_android;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Kaelan Sinclair on 3/10/2017.
 */

public class EZoneNavigationApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
