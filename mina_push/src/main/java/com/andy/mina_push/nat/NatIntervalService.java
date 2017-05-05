package com.andy.mina_push.nat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.andy.mina_push.push.PushEventListener;
import com.andy.mina_push.service.PushService;
import com.andy.mina_push.util.LocalBroadcastUtils;

import org.apache.mina.core.session.IoSession;

/**
 * Created by Andy on 2017/5/5.
 *
 * <li>目的</li>
 * 检测当前设备所在网络的NAT时间<br/>
 *
 * <ul>说明
 * <li>step-1 试探最大interval</li>
 * <li>step-2 稳定观察</li>
 * </ul>
 *
 * <p>首先3次短心跳；
 * 进入NAT试探阶段，在发送失败及达到设定的最大步长时，进入稳定观察阶段；
 * 在稳定期连续设置的成功心跳内，本次试探结束</p>
 */

public class NatIntervalService extends Service implements PushEventListener {
    private NatManager natManager;

    private int countSend = 0;
    private int countReceive = 0;

    private int currState = -1;

    private final int NAT_INIT = 0;
    private final int NAT_TRAIL = 1;
    private final int NAT_CREDIT = 2;
    private final int NAT_IDEL = 4;


    private int minHeart = 1; //seconds TODO need to set
    private int successHear = minHeart; //credit heart
    private int currHeart = successHear;
    private int maxHeart = 10; //max heart: 1 hour TODO need to set
    private final int STEP_DELTA_UNIT = 5; //TODO need to set

    private int countDownFail = 5; //TODO need to set
    private int countDownCredit = 3; //TODO need to set

    private static final String ACTION_START_TRAIL = "com.ithaibo.START_TRAIL";

    private boolean isSuicide = false;

    public static void starNatTrail(Context context) {
        Intent intent = new Intent(context, NatIntervalService.class);
        intent.setAction(ACTION_START_TRAIL);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        natManager = NatManager.getInstance(NatIntervalService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_START_TRAIL)) {
            Log.i(this.getClass().getSimpleName(), "nat service started");
            natManager.openPush();
            natManager.setPushEventListener(this);

            natManager.connect();
        }

        return Service.START_STICKY;
    }

    @Override
    public void onPushConnected() {
        currState = NAT_INIT;
        natManager.setInterval(currHeart);
    }

    @Override
    public void onPushExceptionCaught(IoSession session, Throwable cause) {
        if (cause != null) {
            countDownFail --;

            switch (currState) {
                case NAT_TRAIL:
                    if (countDownFail<=0) {
                        changeState2Credit();
                    }
                    break;
                case NAT_CREDIT:
                    if (countDownFail <=0) {
                        reInitHeart();
                    }
                    break;
                case NAT_IDEL:
                    break;
            }
        }
    }

    private void reInitHeart() {
        Log.e(this.getClass().getSimpleName(), "init trail again");

        natManager.disConnect();
        natManager.connect();

        currState = NAT_INIT;
        currHeart = minHeart;
        countReceive = countSend = 0;
        countDownCredit = 10;
    }

    @Override
    public void onPushMessageSent(Object message) {
        if (message != null && message instanceof String) {
            String msg = (String) message;
            if (!TextUtils.isEmpty(msg) && msg.equals("ping")) {
                countSend++;
            }
        }
    }

    @Override
    public void onPushMessageReceived(Object message) {
        if (message != null && message instanceof String) {
            String msg = (String) message;
            if (!TextUtils.isEmpty(msg) && msg.equals("pong")) {
                countReceive++;
                countDownFail = 5;

                switch (currState) {
                    case NAT_INIT: {
                        if (countSend >= 3 || countReceive >= 3) {
                            changeState2Trail();
                        }
                    }
                    break;

                    case NAT_TRAIL: {
                        if (countReceive >= countSend) {
                            increaseInterval();
                        }
                    }
                    break;

                    case NAT_CREDIT: {
                        countDownCredit --;
                        if (countDownFail>=5 && countDownCredit<=0) {
                            finishNatTrail();
                        }
                    }
                    break;

                    case NAT_IDEL:
                        break;
                }
            }
        }
    }

    private void finishNatTrail() {
        Log.e(this.getClass().getSimpleName(), "finish interval");
        isSuicide = true;
        natManager.disConnect();
        stopSelf();
    }

    private synchronized void increaseInterval() {
        successHear = currHeart;
        natManager.setInterval(successHear);
        Log.e(this.getClass().getSimpleName(), "heart interval= " + successHear);
        int delatInterval = 1 * STEP_DELTA_UNIT;

        if ((delatInterval + currHeart)< maxHeart) {
            currHeart += delatInterval;
            Log.e(this.getClass().getSimpleName(), "increase interval");
        } else {
            changeState2Credit();
        }
    }

    private void changeState2Trail() {
        Log.e(this.getClass().getSimpleName(), "change state to trail");
        currState = NAT_TRAIL;
        countSend = countReceive = 0;
    }

    private void changeState2Credit() {
        Log.e(this.getClass().getSimpleName(), "change state to credit");
        currState = NAT_CREDIT;
        countDownFail = 5;
        countSend = 0;
        countReceive = 0;
    }

    @Override
    public void onPushDisConnected() {
        if (!isSuicide) {
            reInitHeart();
        }
    }

    private void sendNatInterval(Integer interval) {
        LocalBroadcastUtils.sendLocalBroadMsg(interval, this, PushService.ACTION_NAT_INTERVAL);
    }
}
