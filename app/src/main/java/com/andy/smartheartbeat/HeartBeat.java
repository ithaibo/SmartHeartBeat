package com.andy.smartheartbeat;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.andy.smartheartbeat.msg.Msg;
import com.andy.smartheartbeat.msg.MsgType;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.callback.CompletedCallback;


/**
 * Created by Andy on 2017/5/3.
 */

public class HeartBeat {

    private final int TIMES_HEART_BEAT_INIT = 3; //连接建立，初始化心跳3次，连接成功
    private final int TIMES_TRY_FAIL = 5;   //失败次数总计5次则连接失败，5次以内任何一个成功都为成功

    private final int MIN_DELAY_STEP_HEAR_BEAT = 1 * 60 * 1000; //最小心跳时间间隔
    private final int MAX_DELAY_STEP_HEAR_BEAT = 5 * 60 * 1000;//最大心跳时间间隔

    private final int MIN_CREDIT_STEP = 1 * 60 * 1000;

    /**
     * 试探步长 = credibleStep + (trailStepRatio * MIN_DELAY_STEP_HEAR_BEAT)
     * 初始化心跳时间间隔为最小时间间隔;
     * 试探成功，trailStepRatio+=1；
     * 试探失败，次数超过5次，回退到credit；未到5次，继续试探当前步长
     */
    private int trailStep = MIN_CREDIT_STEP;

    /**
     * 试探步长系数
     */
    private int trailStepRatio = 0;
    private final int MAX_RATIO = MAX_DELAY_STEP_HEAR_BEAT / MIN_DELAY_STEP_HEAR_BEAT;

    /**
     * 稳定时步长，初始为0.
     * 当初始化心跳3次，连接成功时，对其进行赋值
     * <p>
     * 当连续5次试探失败，currentStep回退到到credibleStep。
     */
    private int credibleStep = MIN_DELAY_STEP_HEAR_BEAT;

    //失败次数计数器
    private int countDownFail = TIMES_TRY_FAIL;
    //心跳初始化计数器
    private int countDownInitHeartBeat = TIMES_HEART_BEAT_INIT;

    private AsyncSocket socket;

    /**
     * 当前Socket状态
     * 0-初始
     * 1-自适应计算态
     * 2-后台稳定态
     * 3-活跃态
     * 4-IDEL
     */
    private int socketState;

    public static final int STATE_INIT = 0;
    public static final int STATE_TRAIL = 1;
    public static final int STATE_BACK_CREDIT = 2;
    public static final int STATE_FRONT_ACTIVE = 3;
    public static final int STATE_IDEL = 4;
    private final Handler handler;


    private final int WHAT_SOCKET_DISCONNECT = 0;
    private final int WHAT_TRAIL_FAIL = 1;
    private final int WHAT_TRAIL_SUCCESS = 2;
    private final int WHAT_TRAIL_BACK = 3;

    private final int WHAT_SHORT_SUCCCESS = 0x21;
    private final int WHAT_SHORT_FAIL = 0x22;

    private final int WHAT_CREDIT_FAIL = 0x11;
    private final int WHAT_CREDIT_SUCCESS = 0x12;


    public HeartBeat(final AsyncSocket socket) {
        this.socket = socket;
        this.socketState = STATE_INIT;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (!socket.isOpen()) {
                    sendEmptyMessage(WHAT_SOCKET_DISCONNECT);
                } else {

                    switch (msg.what) {
                        case WHAT_TRAIL_FAIL: //试探失败
                            countDownFail--;
                            if (countDownFail > 0) {
                                trailHeartBeatStep(socket);
                            } else {
                                sendEmptyMessage(WHAT_TRAIL_BACK);
                            }
                            break;
                        case WHAT_TRAIL_SUCCESS: //试探成功
                            countDownFail = TIMES_TRY_FAIL;
                            credibleStep = trailStep;

                            if ((1 + trailStepRatio) < MAX_RATIO) {
                                trailStepRatio++;
                                trailHeartBeatStep(socket);
                            } else {
                                socketState = STATE_BACK_CREDIT;
                                sendEmptyMessage(WHAT_CREDIT_SUCCESS);
                            }
                            break;


                        case WHAT_TRAIL_BACK: //回退
                            trailStepRatio--;
                            if (trailStepRatio>0) {
                                credibleStep = trailStepRatio * MIN_DELAY_STEP_HEAR_BEAT;
                            } else {
                                credibleStep = MIN_DELAY_STEP_HEAR_BEAT;
                            }
                            creditHeartBeatStep(socket);
                            break;


                        case WHAT_CREDIT_FAIL: //
                            countDownFail--;
                            if (countDownFail <= 0) {
                                //to trail
                                trailStepRatio = 0;
                                credibleStep = 0;
                                trailHeartBeatStep(socket);
                            } else {
                                //go on
                                creditHeartBeatStep(socket);
                            }
                            break;
                        case WHAT_CREDIT_SUCCESS:
                            countDownFail = TIMES_TRY_FAIL;
                            //go on
                            creditHeartBeatStep(socket);
                            break;


                        case WHAT_SHORT_SUCCCESS:
                            synchronized (this) {
                                countDownInitHeartBeat--;
                            }
                            if (countDownInitHeartBeat > 0 ) {
                                shortHeartBeat(socket);
                            } else {
                                socketState = STATE_TRAIL;
                                trailStepRatio = 0;
                                trailHeartBeatStep(socket);
                            }
                            break;
                        case WHAT_SHORT_FAIL:
                            //
                            break;


                        case WHAT_SOCKET_DISCONNECT:
                            //连接已断开，需要重连
                            handler.getLooper().quit();
                            return;
                    }
                }
            }
        };
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Looper.prepare();
        }

    }

    public void initHeartBeat() {
        if (socket.isOpen()) {
            shortHeartBeat(socket);
        }
    }

    /**
     * 初始化时短心跳
     *
     * @param socket
     */
    private void shortHeartBeat(final AsyncSocket socket) {
        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                Log.i("HeartBeat", "short heart beat");
                Msg msg = new Msg();
                msg.setMsgType(MsgType.MSG_HEART_BEAT);
                MsgSendUtil.sendMsg(msg, socket, new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (ex != null) {
                            Log.e("HeartBeat", ex.getMessage());
                            handler.sendEmptyMessage(WHAT_SHORT_FAIL);
                            return;
                        }
                        handler.sendEmptyMessage(WHAT_SHORT_SUCCCESS);
                    }
                });
            }
        }, SystemClock.elapsedRealtime()+500);
//        handler.postDelayed(, 100);

    }

    /**
     * 试探心跳
     *
     * @param socket
     */
    private void trailHeartBeatStep(final AsyncSocket socket) {
        trailStep = (trailStepRatio + 1) * MIN_DELAY_STEP_HEAR_BEAT;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("HeartBeat", "trailHeartBeatStep");
                Msg msg = new Msg();
                msg.setMsgType(MsgType.MSG_HEART_BEAT);
                MsgSendUtil.sendMsg(msg, socket, new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (ex != null) {
                            Log.e("HeartBeat exception", ex.getMessage());
                            handler.sendEmptyMessage(WHAT_TRAIL_BACK);
                            return;
                        }

                        handler.sendEmptyMessage(WHAT_TRAIL_SUCCESS);
                    }
                });

            }
        }, trailStep);
    }

    /**
     * 稳定心跳
     *
     * @param socket
     */
    private void creditHeartBeatStep(final AsyncSocket socket) {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("HeartBeat", "credit heart beat. step: " + credibleStep);
                Msg msg = new Msg();
                msg.setMsgType(MsgType.MSG_HEART_BEAT);
                MsgSendUtil.sendMsg(msg, socket, new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (ex != null) {
                            handler.sendEmptyMessage(WHAT_CREDIT_FAIL);
                            return;
                        }

                        handler.sendEmptyMessage(WHAT_CREDIT_SUCCESS);
                    }
                });
            }
        }, credibleStep);

    }

    public int getSocketState() {
        return socketState;
    }

    public void setSocketState(int socketState) {
        this.socketState = socketState;
    }

}
