package com.andy.smartheartbeat;

import android.app.Application;

import com.andy.mina_push.nat.NatIntervalService;

/**
 * Created by Andy on 2017/5/5.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Intent intent = new Intent(this, PushService.class);
//        startService(intent);
        NatIntervalService.starNatTrail(getApplicationContext());
    }
}
