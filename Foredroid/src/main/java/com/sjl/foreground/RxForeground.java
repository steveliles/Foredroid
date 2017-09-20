/**
 * Copyright 2015 Steve Liles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sjl.foreground;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import io.reactivex.Completable;
import io.reactivex.subjects.PublishSubject;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Usage:
 *
 * 1. Get the Foreground Singleton, passing a Context or Application object unless you
 * are sure that the Singleton has definitely already been initialised elsewhere.
 *
 * 2.a) Perform a direct, synchronous check: Foreground.isForeground() / .isBackground()
 *
 * or
 *
 * 2.b) Register to be notified (useful in Service or other non-UI components):
 *
 *   Foreground.Listener myListener = new Foreground.Listener(){
 *       public void onBecameForeground(){
 *           // ... whatever you want to do
 *       }
 *       public void onBecameBackground(){
 *           // ... whatever you want to do
 *       }
 *   }
 *
 *   public void onCreate(){
 *      super.onCreate();
 *      Foreground.get(getApplication()).addListener(listener);
 *   }
 *
 *   public void onDestroy(){
 *      super.onCreate();
 *      Foreground.get(getApplication()).removeListener(listener);
 *   }
 */
public class RxForeground implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = RxForeground.class.getName();
    public static final long CHECK_DELAY = 2000;


    private static final PublishSubject<Void> becomeForegroundSubject = PublishSubject.create();
    private static final PublishSubject<Void> becomeBackgroundSubject = PublishSubject.create();

    public Completable foregroundObservable(){
        return becomeForegroundSubject.ignoreElements();
    }

  public Completable backgroundObservable(){
    return becomeBackgroundSubject.ignoreElements();
  }

  public interface Listener {

    }

    public interface Binding {
        public void unbind();
    }

    private static class Listeners {

        private List<WeakReference<Listener>> listeners = new CopyOnWriteArrayList<>();

        public Binding add(Listener listener){
            final WeakReference<Listener> wr = new WeakReference<>(listener);
            listeners.add(wr);
            return new Binding(){
                public void unbind() {
                    listeners.remove(wr);
                }
            };
        }
    }

    private static RxForeground instance;

    private boolean foreground;
    private WeakReference<Activity> currentActivity;
    private Listeners listeners = new Listeners();
    private Handler handler = new Handler();
    private Runnable check;

    public static RxForeground init(Application application){
        if (instance == null) {
            instance = new RxForeground();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
    }

    public static RxForeground get(Application application){
        if (instance == null) {
            init(application);
        }
        return instance;
    }

    public static RxForeground get(){
        if (instance == null) {
            throw new IllegalStateException(
                "Foreground is not initialised - first invocation must use parameterised init/get");
        }
        return instance;
    }

    public boolean isForeground(){
        return foreground;
    }

    public boolean isBackground(){
        return !foreground;
    }

    public Binding addListener(Listener listener){
        return listeners.add(listener);
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {
        // if we're changing configurations we aren't going background so
        // no need to schedule the check
        if (!activity.isChangingConfigurations()) {
            // don't prevent activity being gc'd
            final WeakReference<Activity> ref = new WeakReference<>(activity);
            handler.postDelayed(check = new Runnable() {
                @Override
                public void run() {
                    onActivityCeased(ref.get());
                }
            }, CHECK_DELAY);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = new WeakReference<>(activity);
        // remove any scheduled checks since we're starting another activity
        // we're definitely not going background
        if (check != null) {
            handler.removeCallbacks(check);
        }

        // check if we're becoming foreground and notify listeners
        if (!foreground && (activity != null && !activity.isChangingConfigurations())){
            foreground = true;
            Log.w(TAG, "became foreground");

          becomeForegroundSubject.onComplete();
            //listeners.each(becameForeground);
        } else {
            Log.i(TAG, "still foreground");
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (check != null) {
            handler.removeCallbacks(check);
        }
        onActivityCeased(activity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}


    private void onActivityCeased(Activity activity){
        if (foreground) {
            if ((activity == currentActivity.get())
                    && (activity != null && !activity.isChangingConfigurations())){
                foreground = false;
                Log.w(TAG, "went background");
              becomeBackgroundSubject.onComplete();
              //listeners.each(becameBackground);
            } else {
                Log.i(TAG, "still foreground");
            }
        } else {
            Log.i(TAG, "still background");
        }
    }
}