package com.java.tonystark.server.callback;

import com.java.tonystark.server.IAdminCtrl;
import com.java.tonystark.server.WatchdogThread;
import com.java.tonystark.server.pojo.ClientInfoBean;
import com.java.tonystark.server.pojo.MsgWriteBean;
import com.java.tonystark.server.utils.Log;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerReceiver implements IServerActionListener, IAdminCtrl {

    private volatile IServerManager mIServerManager;

    private volatile int mPort;

    private ConcurrentHashMap<String, ClientInfoBean> mClientInfoBeanList = new ConcurrentHashMap<>();

    private WatchdogThread mWatchdogThread = null;


    public ServerReceiver(int port) {
        mPort = port;
    }

    public void setServerManager(IServerManager serverManager) {
        mIServerManager = serverManager;
    }

    @Override
    public void onServerListening(int serverPort) {
        mClientInfoBeanList.clear();
        if (mWatchdogThread != null && !mWatchdogThread.isShutdown()) {
            mWatchdogThread.shutdown();
        }
        Log.i("服务器启动完成.正在监听端口:" + serverPort);
        mWatchdogThread = new WatchdogThread(mClientInfoBeanList);
        mWatchdogThread.start();
    }

    @Override
    public void onClientConnected(IClient client, int serverPort, IClientPool clientPool) {
        ClientInfoBean bean = new ClientInfoBean(client, ServerReceiver.this);
        bean.setCreateTime(System.currentTimeMillis());
        client.addIOCallback(new ClientIOCallback(bean));

        synchronized (mClientInfoBeanList) {
            mClientInfoBeanList.put(client.getUniqueTag(), bean);
        }
        Log.i(client.getUniqueTag() + " 上线,客户端数量:" + getClientNum());
        sendToAdmin(new MsgWriteBean(client.getHostIp() + " 主机已上线, 客户端数量" + getClientNum()));
    }

    @Override
    public void onClientDisconnected(IClient client, int serverPort, IClientPool clientPool) {
        client.removeAllIOCallback();

        synchronized (mClientInfoBeanList) {
            mClientInfoBeanList.remove(client.getUniqueTag());
        }
        Log.i(client.getUniqueTag() + " 下线,客户端数量:" + getClientNum());
        sendToAdmin(new MsgWriteBean(client.getHostIp() + " 主机已下线, 客户端数量" + getClientNum()));
    }

    @Override
    public void onServerWillBeShutdown(int serverPort, IServerShutdown shutdown, IClientPool clientPool, Throwable throwable) {
        clientPool.sendToAll(new MsgWriteBean("服务器即将关闭"));
        if (throwable == null) {
            Log.i("服务器即将关闭,没有异常");
        } else {
            Log.i("服务器即将关闭,异常信息:" + throwable.getMessage());
            throwable.printStackTrace();
        }
        mWatchdogThread.shutdown();
        shutdown.shutdown();
    }

    @Override
    public void onServerAlreadyShutdown(int serverPort) {
        mClientInfoBeanList.clear();
        OkSocket.server(serverPort).unRegisterReceiver(this);
        Log.i("服务器已经关闭");
    }

    public int getClientNum() {
        synchronized (mClientInfoBeanList) {
            int num = 0;
            Map<String, ClientInfoBean> map = new HashMap<>(mClientInfoBeanList);
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                ClientInfoBean clientInfoBean = map.get(key);
                if (!clientInfoBean.isAdministrator()) {
                    num++;
                }
            }
            return num;
        }
    }


    @Override
    public void sendToAdmin(ISendable sendable) {
        synchronized (mClientInfoBeanList) {
            Map<String, ClientInfoBean> map = new HashMap<>(mClientInfoBeanList);
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                ClientInfoBean clientInfoBean = map.get(key);
                if (clientInfoBean.isAdministrator()) {
                    clientInfoBean.getIClient().send(sendable);
                }
            }
        }
    }

    @Override
    public void restart(final int port) {
        synchronized (this) {
            Log.i("服务器重启中...");
            mIServerManager.shutdown();

            new Thread(() -> {
                Log.i("5秒后启动服务器...");
                while (true) {
                    try {
                        Thread.sleep(5000);
                        break;
                    } catch (InterruptedException e) {
                    }
                }
                mIServerManager = OkSocket.server(port).registerReceiver(ServerReceiver.this);
                mPort = port;
                mIServerManager.listen();
            }).start();
        }
    }


}
