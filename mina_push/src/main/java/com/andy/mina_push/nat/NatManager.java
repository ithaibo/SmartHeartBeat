package com.andy.mina_push.nat;

import android.content.Context;
import android.util.Log;

import com.andy.mina_push.push.ClientKeepAliveMessageFactoryImp;
import com.andy.mina_push.push.ClientSessionHandler;
import com.andy.mina_push.push.Config;
import com.andy.mina_push.push.PushEventListener;
import com.andy.mina_push.util.NetworkUtil;

import org.apache.mina.core.future.ConnectFuture;
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
 * Created by Andy on 2017/5/5.
 */

public class NatManager {

    private final String TAG = this.getClass().getSimpleName();
    private static NatManager natManager;
    private NioSocketConnector connector;
    private ConnectFuture connectFuture;
    private IoSession session;

    public static final String FILTER_NAME_CODEC = "codec";
    public static final String FILTER_NAME_KEPP_ALIVE = "keepalive";

    private int heartBeatInterval = Config.KEEP_ALIVE_TIME_INTERVAL;


    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Context context;
    private KeepAliveFilter keepAliveFilter;

    private NatManager(Context context) {
        this.context = context;
    }

    public static NatManager getInstance(Context context) {
        if (natManager == null) {
            natManager = new NatManager(context);
        }
        return natManager;
    }

    public void setPushEventListener(PushEventListener pushEventListener) {
        if (connector != null && connector.getHandler() != null) {
            if (connector.getHandler() instanceof ClientSessionHandler) {
                ((ClientSessionHandler) connector.getHandler()).setPushEventListener(pushEventListener);
            }
        }

    }

    public void openPush() {
        if (connector != null) {
            return;
        }
        connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(Config.SOCKET_CONNECT_TIMEOUT);
        connector.setHandler(new ClientSessionHandler());

        connector.setDefaultLocalAddress(new InetSocketAddress(52120)); //设置心跳的端口

        connector.getFilterChain()
                .addLast(FILTER_NAME_CODEC,
                        new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        if (keepAliveFilter == null) {
            keepAliveFilter = new KeepAliveFilter(new ClientKeepAliveMessageFactoryImp(),
                    IdleStatus.READER_IDLE,
                    KeepAliveRequestTimeoutHandler.DEAF_SPEAKER,
                    heartBeatInterval,
                    Config.KEEP_ALIVE_RESPONSE_TIMEOUT);
        }

        connector.getFilterChain()
                .addLast(FILTER_NAME_KEPP_ALIVE, keepAliveFilter);

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
                    Log.i(TAG, "manager connect" + android.os.Process.myPid() + '-' + android.os.Process.myTid());
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
            e.printStackTrace();
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

    public void setInterval(int interval) {
        if (session == null || !session.isConnected() || !connector.isActive()) {
            return;
        }
        if (keepAliveFilter == null) {
            return;
        }

        keepAliveFilter.setRequestInterval(interval);
    }
}
