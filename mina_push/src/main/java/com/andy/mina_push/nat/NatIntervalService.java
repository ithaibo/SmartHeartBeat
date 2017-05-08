package com.andy.mina_push.nat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.andy.mina_push.push.PushEventListener;
import com.andy.mina_push.service.PushService;
import com.andy.mina_push.util.Constants;
import com.andy.mina_push.util.DateUtils;
import com.andy.mina_push.util.LocalBroadcastUtils;

import org.apache.mina.core.session.IoSession;

import java.util.Calendar;
import java.util.TimeZone;


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
    private static final String TAG = NatIntervalService.class.getSimpleName();
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

    public static final String ACTION_START_TRAIL = "com.ithaibo.START_TRAIL";

    private boolean isSuicide = false;


    /**
     * 在设定的时间内进行NAT探测
     * @param context
     */
    public static void starNatTrail(Context context) {
        Log.i(TAG, "try to start NatIntervalService");
        SharedPreferences sp = context.getSharedPreferences(Constants.NAME_SP_NAT, MODE_PRIVATE);
        long lastNatTime = sp.getLong(Constants.KEY_SP_LAST_NAT_TIME, Constants.DEFAULT_NAT_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        long timeNow = DateUtils.getTimeMilisGMT8();

        Log.i(TAG, "time now: " + timeNow);
        Log.i(TAG, "time last nat: " + lastNatTime);
        if ((timeNow - lastNatTime) < Constants.VALID_NAT_TRACE_PEROID) {
            Log.i(TAG, "nat peroid is not time out.");
            return;
        }

        Log.i(TAG, "nat peroid time out. trail nat interval again.");
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_START_TRAIL)) {
            Log.i(TAG, "nat service started");
            if (natManager == null){
                natManager = NatManager.getInstance(NatIntervalService.this);
            }
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
        Log.e(TAG, "init trail again");

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
        Log.e(TAG, "finish interval");
        //save time to SharedPreference
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        long time2Save = DateUtils.getTimeMilisGMT8();

        synchronized (this) {
            SharedPreferences sp = getSharedPreferences(Constants.NAME_SP_NAT, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(Constants.KEY_SP_LAST_NAT_TIME, time2Save);
            editor.commit();
        }

        Log.i(TAG, "finished this nat trail, time= " + time2Save);

        isSuicide = true;
        //disconnect
        natManager.disConnect();

        //notice push service to change heart interval.
        sendMsgChangePushHearInterval(successHear);

        //stop service by self
        stopSelf();
    }

    private synchronized void increaseInterval() {
        successHear = currHeart;
        natManager.setInterval(successHear);
        Log.e(TAG, "heart interval= " + successHear);
        int delatInterval = 1 * STEP_DELTA_UNIT;

        if ((delatInterval + currHeart)< maxHeart) {
            currHeart += delatInterval;
            Log.e(this.getClass().getSimpleName(), "increase interval");
        } else {
            changeState2Credit();
        }
    }

    private void changeState2Trail() {
        Log.e(TAG, "change state to trail");
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

    private void sendMsgChangePushHearInterval(Integer interval) {
        LocalBroadcastUtils.sendLocalBroadMsg(interval, this, PushService.ACTION_NAT_INTERVAL);
    }

    @Override
    public void onDestroy() {
        natManager.disConnect();
        super.onDestroy();
    }
}
