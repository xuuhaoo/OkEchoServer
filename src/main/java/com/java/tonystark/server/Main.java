package com.java.tonystark.server;

import com.java.tonystark.server.callback.ServerReceiver;
import com.xuhao.didi.core.utils.SLog;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerManager;

public class Main {

    private static final int DEFAULT_PORT = 7070;

    public static void main(String... args) {
        SLog.setIsDebug(true);
        ServerReceiver serverReceiver = new ServerReceiver(DEFAULT_PORT);
        IServerManager serverManager = OkSocket.server(DEFAULT_PORT)
                .registerReceiver(serverReceiver);
        serverReceiver.setServerManager(serverManager);
        serverManager.listen();
    }

}
