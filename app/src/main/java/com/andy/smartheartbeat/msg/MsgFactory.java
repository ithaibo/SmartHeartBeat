package com.andy.smartheartbeat.msg;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * Created by Andy on 2017/5/3.
 */

public class MsgFactory {

    public static<T extends Serializable> Msg createMsg(int msgType, T data) {
        String dataJson = JSON.toJSONString(data);
        Msg msg = new Msg();
        msg.setMsgType(msgType);
        msg.setMsgData(dataJson);

        return msg;
    }
}
