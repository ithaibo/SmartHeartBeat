package com.andy.mina_push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


/**
 * Created by Administrator on 2015/12/1.
 */
public class ScreenActionReceiver extends BroadcastReceiver {
    private String TAG = "ScreenActionReceiver";
    private boolean isRegisterReceiver = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG," action : " + action);
        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            Log.d(TAG, "屏幕解锁广播...");
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d(TAG, "屏幕加锁广播...");
        }else if (action.equals(Intent.ACTION_USER_PRESENT)) {
            Log.d(TAG, "ACTION_USER_PRESENT...");
        }
    }

    public void registerScreenActionReceiver(Context mContext) {
        if (!isRegisterReceiver) {
            isRegisterReceiver = true;

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            Log.d(TAG, "注册屏幕解锁、加锁广播接收者...");
            mContext.registerReceiver(ScreenActionReceiver.this, filter);
        }
    }

    public void unRegisterScreenActionReceiver(Context mContext) {
        if (isRegisterReceiver) {
            isRegisterReceiver = false;
            Log.d(TAG, "注销屏幕解锁、加锁广播接收者...");
            mContext.unregisterReceiver(ScreenActionReceiver.this);
        }
    }

}
