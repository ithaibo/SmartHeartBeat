package com.andy.mina_push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.andy.mina_push.service.PushService;
import com.andy.mina_push.util.Constants;
import com.andy.mina_push.util.NetworkUtil;


/**
 * Created by du123 on 2015/7/2.
 */
public class AlarmReceiver extends BroadcastReceiver {
	private final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
    	String action = intent.getAction();
    	Log.d(TAG, "alarm receiver action : " + action);
    	if(TextUtils.equals(action, Constants.PPLINK_HEART_BEAT_MODE)){
			if (!NetworkUtil.isNetworkConnect(context)) {
				Log.e(TAG, "AlarmReceiver--网络未连接");
				return;
			}
	        Intent pushService = new Intent(context, PushService.class);
	        context.startService(pushService);
    	}else if(TextUtils.equals(action, Constants.PPLINK_ALARM_OFF)){
    		Intent pushService = new Intent(context, PushService.class);
	        context.startService(pushService);
    	}else if(TextUtils.equals(action, Constants.PPLINK_ALARM_ON)){
    		Intent pushService = new Intent(context, PushService.class);
	        context.startService(pushService);
    	}
    }
}
