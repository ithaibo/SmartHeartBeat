package com.andy.mina_push.msg;

import java.io.Serializable;

/**
 * Created by Andy on 2017/5/3.
 */

public class Msg implements Serializable {
    private int msgType;
    private String ticket;
    private String sign;

    private String msgData;

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getMsgData() {
        return msgData;
    }

    public void setMsgData(String msgData) {
        this.msgData = msgData;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "msgType=" + msgType +
                ", msgData='" + msgData + '\'' +
                '}';
    }
}
