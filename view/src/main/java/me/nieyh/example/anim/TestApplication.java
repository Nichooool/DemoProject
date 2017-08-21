package me.nieyh.example.anim;

import android.app.Application;
import android.content.Context;

/**
 * Created by nieyh on 17-6-22.
 */

public class TestApplication extends Application {

    private static TestApplication sInstance;

    public static TestApplication getInstance() {
        return sInstance;
    }

    public TestApplication() {
        sInstance = this;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

}
