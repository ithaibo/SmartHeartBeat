package com.andy.mina_push.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.andy.mina_push.msg.Msg;
import com.andy.mina_push.msg.MsgType;
import com.andy.mina_push.push.PushEventListener;
import com.andy.mina_push.push.PushManager;
import com.andy.mina_push.util.LocalBroadcastUtils;

import org.apache.mina.core.session.IoSession;


public class PushService extends Service implements PushEventListener {
    public static final String ACTION_PUSH_MSG_2_SERVER = "com.ithaibo.msg.emitter";
    public static final String ACTION_NAT_INTERVAL = "com.ithaibo.heart_beat.interval";

    private final String TAG = this.getClass().getSimpleName();

    PushManager pushManager = PushManager.getInstance(this);
    MsgEmitterReceiver msgEmitterReceiver;
    private NatIntervalReceiver natReceiver;

    @Override
    public void onCreate() {
        Log.i(TAG, "service start. process_id = " + android.os.Process.myPid() + " & tid = " + android.os.Process.myTid());
        super.onCreate();

        msgEmitterReceiver = new MsgEmitterReceiver();
        LocalBroadcastUtils.registerLBReceiver(msgEmitterReceiver, ACTION_PUSH_MSG_2_SERVER, PushService.this);

        natReceiver = new NatIntervalReceiver();
        LocalBroadcastUtils.registerLBReceiver(natReceiver, ACTION_NAT_INTERVAL, PushService.this);

        pushManager.openPush();
        pushManager.setPushEventListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "service start command, process_id = " + android.os.Process.myPid() + " & tid = " + android.os.Process.myTid());
        super.onStartCommand(intent, flags, startId);

        boolean isConnected = pushManager.connect();
        Log.i(TAG, "is connected = " + isConnected);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastUtils.unregisterLBReceiver(msgEmitterReceiver, PushService.this);
        LocalBroadcastUtils.unregisterLBReceiver(natReceiver, PushService.this);

        pushManager.disConnect();
    }

    @Override
    public void onPushConnected() {
        Log.i(TAG, "service push open" + android.os.Process.myPid() + " & tid = " + android.os.Process.myTid());
    }

    @Override
    public void onPushExceptionCaught(IoSession session, Throwable cause) {
        Log.i(TAG, "service push exception" + android.os.Process.myPid() + " & tid = " + android.os.Process.myTid());
    }

    @Override
    public void onPushMessageSent(Object message) {
        Log.i(TAG, "service push sent" + android.os.Process.myPid() + " & tid = " + android.os.Process.myTid());
        Log.i(TAG, "msg send: " +message);
    }

    @Override
    public void onPushMessageReceived(Object message) {
        Log.i(TAG, "service push received" + android.os.Process.myPid() + " & tid = " + android.os.Process.myTid());
        Log.i(TAG, "msg received: " +message);
        //在这里处理消息
    }

    @Override
    public void onPushDisConnected() {
        Log.i(TAG, "service push close, process_id = " + android.os.Process.myPid() + " & tid = " + android.os.Process.myTid());
        if (pushManager!=null) {
            pushManager.connect();
        }
    }

    public class MsgEmitterReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Msg msg = (Msg) intent.getSerializableExtra("data");
            if (msg.getMsgType() == MsgType.MSG_HEART_BEAT) {
                Log.i(TAG, "change the hear beat interval to 30 seconds");
                pushManager.setHeartBeatInterval(1*30);
            } else {
                pushManager.sendMessage(msg);
            }

        }
    }

    public class NatIntervalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int interval = intent.getIntExtra("interval", 0);
            if (pushManager!=null && interval > 0) {
                pushManager.setHeartBeatInterval(interval);
            }
        }
    }
}
