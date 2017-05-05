package com.andy.mina_push.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Andy on 2017/5/5.
 */

public class LocalBroadcastUtils {
    public static <T extends Serializable> void sendLocalBroadMsg(T data, Context context, String action) {
        if (context==null || TextUtils.isEmpty(action)){
            return;
        }

        Intent intent = new Intent(action);
        intent.putExtra("data", data);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.sendBroadcast(intent);
    }

    public static void registerLBReceiver(BroadcastReceiver receiver, String action, Context context) {
        if (receiver == null) {
            return;
        }
        if (TextUtils.isEmpty(action)){
            return;
        }
        if (context == null) {
            return;
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter(action);
        lbm.registerReceiver(receiver, filter);
    }

    public static void unregisterLBReceiver(BroadcastReceiver receiver, Context context) {
        if (receiver == null) {
            return;
        }
        if (context == null) {
            return;
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.unregisterReceiver(receiver);
    }
}
