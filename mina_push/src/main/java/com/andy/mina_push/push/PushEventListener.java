package com.andy.mina_push.push;

import org.apache.mina.core.session.IoSession;

/**
 * Created by neal on 2014/12/7.
 */
public interface PushEventListener {
    public abstract void onPushConnected();
    public abstract void onPushExceptionCaught(IoSession session, Throwable cause);
    public abstract void onPushMessageSent(Object message);
    public abstract void onPushMessageReceived(Object message);
    public abstract void onPushDisConnected();
}
