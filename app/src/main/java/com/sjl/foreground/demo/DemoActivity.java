package com.sjl.foreground.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sjl.foreground.Foreground;

public abstract class DemoActivity extends ActionBarActivity implements Foreground.Listener {

    private Foreground.Binding listenerBinding;

    protected abstract void startAnotherActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnotherActivity();
            }
        });

        listenerBinding = Foreground.get(getApplication()).addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // not strictly necessary as Foreground only holds a weak reference
        // to the listener to defensively prevent leaks, but its always better
        // to be explicit and WR's play monkey with the Garbage Collector
        listenerBinding.unbind();
    }

    @Override
    public void onBecameForeground() {
        Log.i(Foreground.TAG, getClass().getName() + " became foreground");
    }

    @Override
    public void onBecameBackground() {
        Log.i(Foreground.TAG, getClass().getName() + " went background");
    }

}
