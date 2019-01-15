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

    private ClientInfoBean mClientInfoBean;

    public ClientIOCallback(ClientInfoBean bean) {
        mClientInfoBean = bean;
    }

    @Override
    public void onClientRead(OriginalData originalData, IClient client, IClientPool<IClient, String> clientPool) {
        clientPool.sendToAll(new OriginalWriteBean(originalData));
    }

    @Override
    public void onClientWrite(ISendable sendable, IClient client, IClientPool<IClient, String> clientPool) {
        mClientInfoBean.setLastActionTime(System.currentTimeMillis());
    }
}
