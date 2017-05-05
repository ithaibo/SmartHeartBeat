package com.andy.smartheartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.andy.mina_push.service.PushService;
import com.andy.mina_push.util.LocalBroadcastUtils;
import com.andy.smartheartbeat.msg.Msg;
import com.andy.smartheartbeat.msg.MsgType;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ConnectCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.WritableCallback;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "SocketService";
    private LocalBroadcastManager lbm;

    private AsyncSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnConnect = (Button) findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);

        Button btnSendHeartBeat = (Button) findViewById(R.id.btn_send_heart_beat);
        btnSendHeartBeat.setOnClickListener(this);

        Button btnSendLocation = (Button) findViewById(R.id.btn_send_location_msg);
        btnSendLocation.setOnClickListener(this);

        Button btnSendOrder = (Button) findViewById(R.id.btn_send_order_msg);
        btnSendOrder.setOnClickListener(this);

        Button btnSendPersonal = (Button) findViewById(R.id.btn_send_personal_msg);
        btnSendPersonal.setOnClickListener(this);

        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(new RunnerSocketMsgReceiver(), new IntentFilter("com.gidoor.runner.socket"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
//                connect();
                break;
            case R.id.btn_send_heart_beat:
//                sendMsgHearBeat();
                Intent intentHeartBeat = new Intent("com.gidoor.runner.socket");
                lbm.sendBroadcast(intentHeartBeat);
                break;
            case R.id.btn_send_location_msg:
                sendMsgLocation();
                break;
            case R.id.btn_send_order_msg:
                sendMsgOrder();
                break;
            case R.id.btn_send_personal_msg:
                sendMsgPersonal();
                break;
        }
    }

    private ConnectCallback connectCallback = new ConnectCallback() {
        @Override
        public void onConnectCompleted(Exception ex, AsyncSocket socket) {
            if (ex != null) {
                Log.d(TAG, "连接出错");
                return;
            }

            MainActivity.this.socket = socket;

            socket.setDataCallback(new DataCallback() {
                @Override
                public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                    Log.d(TAG, new String(bb.getAllByteArray()));
                }
            });

            socket.setClosedCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) {
                        Log.d(TAG, "setClosedCallback出错");
                        return;
                    }
                    Log.d(TAG, "setClosedCallback");
                }
            });

            socket.setEndCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) {
                        Log.d(TAG, "setEndCallback出错");
                        return;
                    }
                    Log.d(TAG, "setEndCallback");
                }
            });

            socket.setWriteableCallback(new WritableCallback() {
                @Override
                public void onWriteable() {
                    Log.d(TAG, "onWriteable");
                }
            });

            final Msg msg = new Msg();
            msg.setMsgType(MsgType.MSG_HEART_BEAT);
            final String msgJson = JSON.toJSONString(msg);

        }
    };

    private void sendMsg2Server(final Msg msg2Send) {

        Intent intent = new Intent();
        intent.setAction(PushService.ACTION_PUSH_MSG_2_SERVER);
        Bundle dataBundle = new Bundle();
        dataBundle.putSerializable("msg", msg2Send);
        intent.putExtra("data", dataBundle);

        lbm.sendBroadcast(intent);

        LocalBroadcastUtils.sendLocalBroadMsg(msg2Send, MainActivity.this, PushService.ACTION_PUSH_MSG_2_SERVER);
    }

    private void connect() {
        AsyncServer.getDefault()
                .connectSocket("10.0.0.48", 30714, connectCallback);

    }

    private void sendMsgOrder() {
        Msg msg = new Msg();
        msg.setMsgType(MsgType.MSG_ORDER_SERVER_ACK);
        sendMsg2Server(msg);
        System.out.println("order----------");
    }

    private void sendMsgLocation() {
        Msg msg = new Msg();
        msg.setMsgType(MsgType.MSG_LOCATION_POST);
        sendMsg2Server(msg);
        System.out.println("location post----------");
    }

    private void sendMsgHearBeat() {
//        Msg msg = new Msg();
//        msg.setMsgType(MsgType.MSG_HEART_BEAT);
//        sendMsg2Server(msg);
//        System.out.println("heart beat----------");

        HeartBeat heartBeat = new HeartBeat(socket);
        heartBeat.initHeartBeat();
    }

    private void sendMsgConnect() {
        Msg msg = new Msg();
        msg.setMsgType(MsgType.MSG_CONNECT_ACK);
        sendMsg2Server(msg);
        System.out.println("connect----------");
    }

    private void sendMsgDisconnect() {
        Msg msg = new Msg();
        msg.setMsgType(MsgType.MSG_DISCONNECT);
        sendMsg2Server(msg);
        System.out.println("disconnect msg----------");
    }

    private void sendMsgPersonal() {
        Msg msg = new Msg();
        msg.setMsgType(MsgType.MSG_PERSONAL);
        sendMsg2Server(msg);
        System.out.println("personal msg----------");
    }


    private class RunnerSocketMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendMsgHearBeat();
        }
    }
}
