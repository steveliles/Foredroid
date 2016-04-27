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

import java.lang.ref.WeakReference;
import java.util.Iterator;
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
public class Foreground implements  Application.ActivityLifecycleCallbacks {

    public interface Listener {
        public void onBecameForeground();
        public void onBecameBackground();
    }

    private static Foreground sInstance;
    private List<WeakReference<Listener>> mListenerList;
    private boolean    mIsForeground;
    private Stack<Activity> mActivityStack;

    private Foreground(){
        mListenerList = new LinkedList<WeakReference<Listener>>();
        mActivityStack = new Stack<Activity>();
    }

    public static final void init(Application app){
        if(sInstance == null){
            sInstance = new Foreground();
            app.registerActivityLifecycleCallbacks(sInstance);
        }

    }
    public static final Foreground getInstance(){
        return sInstance;
    }

    private void notifyToForeground(){
        for(WeakReference<Listener> listenerRef : mListenerList){
            Listener listener = listenerRef.get();
            if(listener != null){
                listener.onBecameForeground();
            }
        }
    }

    private void notifyToBackground(){
        for(WeakReference<Listener> listenerRef : mListenerList){
            Listener listener = listenerRef.get();
            if(listener != null){
                listener.onBecameBackground();
            }
        }
    }

    public Activity getTopActivity(){

        return mActivityStack.peek();
    }
    public void addListener(Listener listener){
        WeakReference<Listener> listenerWeakRef = new WeakReference<Listener>(listener);
        mListenerList.add(listenerWeakRef);
    }
    public void removeListener(Listener listener){
        for(WeakReference<Listener> listenerRef : mListenerList){
            Listener tempListener = listenerRef.get();
            if(tempListener == listener){
                mListenerList.remove(listenerRef);
                return;
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

        if(!mIsForeground){
            mIsForeground = true;
            notifyToForeground();
        }
        mActivityStack.push(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivityStack.remove(activity);
        if((mActivityStack.isEmpty())&&(!activity.isChangingConfigurations())){
            mIsForeground = false;
            notifyToBackground();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }




}
