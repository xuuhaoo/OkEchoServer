package com.java.tonystark.server.callback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java.tonystark.server.pojo.ClientInfoBean;
import com.java.tonystark.server.pojo.MsgWriteBean;
import com.java.tonystark.server.pojo.OriginalWriteBean;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClient;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientIOCallback;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientPool;

import java.nio.charset.Charset;

public class ClientIOCallback implements IClientIOCallback {

    private boolean mNeedDisconnect;

    private ClientInfoBean mClientInfoBean;

    public ClientIOCallback(ClientInfoBean bean) {
        mClientInfoBean = bean;
        mNeedDisconnect = false;
    }

    @Override
    public void onClientRead(OriginalData originalData, IClient client, IClientPool<IClient, String> clientPool) {
        String str = new String(originalData.getBodyBytes(), Charset.forName("utf-8"));
        try {
            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            int cmd = jsonObject.get("cmd").getAsInt();
            switch (cmd) {
                case 54: {
                    if (mClientInfoBean.getHandShakeTime() != 0) {
                        client.send(new MsgWriteBean("Illegal handshake, error message: duplicate handshake"));
                        mNeedDisconnect = true;
                        return;
                    }
                    String handshake = jsonObject.get("handshake").getAsString();
                    if ("admin".equals(handshake)) {
                        mClientInfoBean.setAdministrator(true);
                    } else {
                        mClientInfoBean.setAdministrator(false);
                    }

                    mClientInfoBean.setHandShakeTime(System.currentTimeMillis());
                    break;
                }
                case 911: {
                    if (mClientInfoBean.isAdministrator()) {
                        int port = jsonObject.get("port").getAsInt();
                        mClientInfoBean.getAdminCtrl().restart(port);
                    } else {
                        client.send(new MsgWriteBean("Illegal cmd, your not administrator"));
                        mNeedDisconnect = true;
                    }
                    break;
                }
                case 912: {
                    if (mClientInfoBean.isAdministrator()) {
                        String who = jsonObject.get("who").getAsString();
                        IClient whoClient = clientPool.findByUniqueTag(who);
                        if (whoClient != null) {
                            mClientInfoBean.getAdminCtrl().sendToAdmin(new MsgWriteBean("成功踢出:"+who));
                            whoClient.disconnect();
                        }else{
                            mClientInfoBean.getAdminCtrl().sendToAdmin(new MsgWriteBean("踢出失败:"+who));
                        }
                    } else {
                        client.send(new MsgWriteBean("Illegal cmd, your not administrator"));
                        mNeedDisconnect = true;
                    }
                    break;
                }
            }
            mClientInfoBean.setLastActionTime(System.currentTimeMillis());
        } catch (Exception e) {
            if (mClientInfoBean.getHandShakeTime() == 0) {
                client.send(new MsgWriteBean("Illegal handshake, you need handshake first"));
                mNeedDisconnect = true;
                return;
            }
        }
        if (!mClientInfoBean.isAdministrator()) {
            client.send(new OriginalWriteBean(originalData));
            mClientInfoBean.getAdminCtrl().sendToAdmin(new MsgWriteBean(client.getUniqueTag() + "@ " + str));
        }

    }

    @Override
    public void onClientWrite(ISendable sendable, IClient client, IClientPool<IClient, String> clientPool) {
        mClientInfoBean.setLastActionTime(System.currentTimeMillis());
        if (mNeedDisconnect) {
            client.disconnect(new Exception("主动断开,因为判定非法"));
            mClientInfoBean.getAdminCtrl().sendToAdmin(new MsgWriteBean(client.getUniqueTag() + "非法操作,服务器断开他"));
        }
    }
}
