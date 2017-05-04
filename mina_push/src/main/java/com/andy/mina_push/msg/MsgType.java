package com.andy.mina_push.msg;

/**
 * Created by Andy on 2017/5/3.
 */

public class MsgType {
    //msg control
    public static final int MSG_CONNECT = 0;
    public static final int MSG_CONNECT_ACK = 1;
    public static final int MSG_HEART_BEAT = 2;
    public static final int MSG_HEART_BEAT_ACK = 3;
    public static final int MSG_DISCONNECT = 4;

    //order service
    public static final int MSG_ORDER = 0x10;
    public static final int MSG_ORDER_FRESH = 0x11;
    public static final int MSG_ORDER_STATUS_CHANGED = 0x12;

    //location service
    public static final int MSG_LOCATION = 0x20;
    public static final int MSG_LOCATION_POST = 0x21;

    //personal service
    public static final int MSG_PERSONAL = 0x40;

    //service ack
    public static final int MSG_SERVICE_ACK = 0x30; //COMMON ACK
    public static final int MSG_ORDER_CLIENT_ACK = 0x31; //ORDER ACK (send by client)
    public static final int MSG_ORDER_SERVER_ACK = 0x32; //ORDER ACK (send by server)
    public static final int MSG_LOCATION_SERVER_ACK = 0x33;
    public static final int MSG_PERSONAL_CLIENT_ACK = 0x34;
}
