package com.sjl.foreground.demo;

import android.content.Intent;

public class ActivityA extends DemoActivity {

    @Override
    protected void startAnotherActivity() {
        startActivity(new Intent(getApplicationContext(), ActivityB.class));
    }
}
