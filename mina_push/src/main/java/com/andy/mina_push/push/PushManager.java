package com.andy.mina_push.push;

import android.content.Context;
import android.util.Log;

import com.andy.mina_push.msg.Msg;
import com.andy.mina_push.util.NetworkUtil;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


/**
 * Created by neal on 2014/12/2.
 */
public class PushManager {
    private final String TAG = this.getClass().getSimpleName();
    private static PushManager pushManager;
    private NioSocketConnector connector;
    private ConnectFuture connectFuture;
    private IoSession session;

    public static final String FILTER_NAME_CODEC = "codec";
    public static final String FILTER_NAME_KEPP_ALIVE = "keepalive";

    private int heartBeatInterval = Config.KEEP_ALIVE_TIME_INTERVAL;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Context context;

    public PushManager(Context context) {
        this.context = context;
    }

    public static PushManager getInstance(Context context) {
        if (pushManager == null) {
            pushManager = new PushManager(context);
        }
        return pushManager;
    }

    public void setPushEventListener(PushEventListener pushEventListener) {
        if (connector != null && connector.getHandler() != null) {
            if (connector.getHandler() instanceof ClientSessionHandler) {
                ((ClientSessionHandler) connector.getHandler()).setPushEventListener(pushEventListener);
            }
        }

    }

    /**
     * 用于动态设置心跳步长
     *
     * @param heartBeatInterval
     */
    public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
        //改变心跳步长
        ((KeepAliveFilter) connector.getFilterChain()
                .getEntry(FILTER_NAME_KEPP_ALIVE)
                .getFilter()).setRequestInterval(heartBeatInterval);
    }

    public void openPush() {
        if (connector != null) {
            return;
        }
        connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(Config.SOCKET_CONNECT_TIMEOUT);
        connector.setHandler(new ClientSessionHandler());

        connector.getFilterChain()
                .addLast(FILTER_NAME_CODEC,
                        new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        connector.getFilterChain()
                .addLast(FILTER_NAME_KEPP_ALIVE,
                        new KeepAliveFilter(new ClientKeepAliveMessageFactoryImp(),
                                IdleStatus.READER_IDLE,
                                KeepAliveRequestTimeoutHandler.DEAF_SPEAKER,
                                heartBeatInterval,
                                Config.KEEP_ALIVE_RESPONSE_TIMEOUT));

    }

    /**
     * 开始连接
     *
     * @return
     */
    public boolean connect() {
        if (!NetworkUtil.isNetworkConnect(context) || connector == null) {
            return false;
        }
        if (connector != null && connector.isActive() && connectFuture != null && connectFuture.isConnected() && session != null && session.isConnected()) {
            return true;
        }
        FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    connectFuture = connector.connect(new InetSocketAddress(
                            Config.HOSTNAME, Config.PORT));
                    connectFuture.awaitUninterruptibly();
                    session = connectFuture.getSession();
                    Log.i(this.getClass().getSimpleName(), "manager connect" + android.os.Process.myPid() + '-' + android.os.Process.myTid());
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        });

        executorService.submit(futureTask);
        try {
            return futureTask.get();
        } catch (Exception e) {
            return false;
        }

    }


    public boolean sendMessage(Msg msg) {
        if (session == null || !session.isConnected()) {
            return false;
        }
        Log.i(TAG, "send a message: " + msg);
        WriteFuture writeFuture = session.write(msg);
        if (writeFuture == null) {
            return false;
        }
        writeFuture.awaitUninterruptibly();
        if (writeFuture.isWritten()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 关闭连接
     */
    public void disConnect() {
        if (session != null && session.isConnected()) {
            session.close(false);
        }
        if (connectFuture != null && connectFuture.isConnected()) {
            connectFuture.cancel();
        }
    }

    public void reConnect() {
        disConnect();
        connect();
    }

    public NioSocketConnector getConnector() {
        return connector;
    }
}
