package com.sjl.foreground.demo;

import android.content.Intent;

public class ActivityB extends DemoActivity {

    @Override
    protected void startAnotherActivity() {
        startActivity(new Intent(getApplicationContext(), ActivityA.class));
    }

}
