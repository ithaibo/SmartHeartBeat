package com.andy.mina_push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.andy.mina_push.service.PushService;
import com.andy.mina_push.util.NetworkUtil;


/**
 * Created by du123 on 2015/7/2.
 */
public class NetWorkReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "network changed");

        if (!NetworkUtil.isNetworkConnect(context)) {
            Log.e(TAG, "网络未链接");
            return;
        }

        //网络切换，必须重连mina
        Intent pushService = new Intent(context, PushService.class);
        context.startService(pushService);
    }
}
