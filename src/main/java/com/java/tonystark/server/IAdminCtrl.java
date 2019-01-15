package com.java.tonystark.server;

import com.xuhao.didi.core.iocore.interfaces.ISendable;

public interface IAdminCtrl {

    void sendToAdmin(ISendable sendable);

    void restart(int port);

}
