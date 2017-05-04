package com.andy.mina_push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.andy.mina_push.service.PushService;
import com.andy.mina_push.util.AndroidUtils;
import com.andy.mina_push.util.NetworkUtil;


/**
 * Created by du123 on 2015/7/2.
 */
public class ReBootReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"system boot");

        if (!NetworkUtil.isNetworkConnect(context)) {
            return;
        }

        boolean b = AndroidUtils.isServiceRunning(context, "com.gidoor.runner.service.RunnerService");

        if(!b){
            Intent pushService = new Intent(context, PushService.class);
            context.startService(pushService);
        }
    }
}
