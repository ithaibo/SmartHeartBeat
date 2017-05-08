package com.andy.smartheartbeat;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andy.mina_push.nat.NatIntervalService;
import com.andy.mina_push.service.PushService;
import com.andy.mina_push.util.LocalBroadcastUtils;
import com.andy.smartheartbeat.msg.Msg;
import com.andy.smartheartbeat.msg.MsgType;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "SocketService";
    private LocalBroadcastManager lbm;


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
    }

    @Override
    protected void onStart() {
        super.onStart();
        NatIntervalService.starNatTrail(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
//                connect();
                break;
            case R.id.btn_send_heart_beat:
//                sendMsgHearBeat();
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


    private void sendMsg2Server(final Msg msg2Send) {

        Intent intent = new Intent();
        intent.setAction(PushService.ACTION_PUSH_MSG_2_SERVER);
        Bundle dataBundle = new Bundle();
        dataBundle.putSerializable("msg", msg2Send);
        intent.putExtra("data", dataBundle);

        lbm.sendBroadcast(intent);

        LocalBroadcastUtils.sendLocalBroadMsg(msg2Send, MainActivity.this, PushService.ACTION_PUSH_MSG_2_SERVER);
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


    private void sendMsgPersonal() {
        Msg msg = new Msg();
        msg.setMsgType(MsgType.MSG_PERSONAL);
        sendMsg2Server(msg);
        System.out.println("personal msg----------");
    }

}
