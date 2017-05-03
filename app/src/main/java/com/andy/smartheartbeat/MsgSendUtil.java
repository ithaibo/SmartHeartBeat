package com.andy.smartheartbeat;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.andy.smartheartbeat.msg.Msg;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;

/**
 * Created by Andy on 2017/5/3.
 */

public class MsgSendUtil {
    private static final String TAG = "MsgSendUtil";

    public static void sendMsg(final Msg msg2Send, AsyncSocket socket, CompletedCallback callback) {
        if (msg2Send == null) {
            Log.e(TAG, "msg is null");
            return;
        }

        if (socket == null || !socket.isOpen()) {
            Log.e(TAG, "socket is not available");
            return;
        }

        String msgJson = JSON.toJSONString(msg2Send);
        Util.writeAll(socket, msgJson.getBytes(), callback);
    }
}
