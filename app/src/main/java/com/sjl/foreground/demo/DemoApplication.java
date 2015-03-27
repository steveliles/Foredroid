package com.sjl.foreground.demo;

import android.app.Application;

/**
 * We must register an application class in order to be able to register the Foreground instance
 * to receive activity-lifecycle callbacks.
 *
 * Using Google's recommended singleton pattern makes it nice and easy to get hold of your
 * Application object from anywhere.
 */
public class DemoApplication extends Application {

    private static DemoApplication instance;

    public static DemoApplication getInstance(){
        return instance;
    }

    public DemoApplication(){
        instance = this;
    }

}
