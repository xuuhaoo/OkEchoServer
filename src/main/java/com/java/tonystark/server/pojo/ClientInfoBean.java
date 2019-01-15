package com.java.tonystark.server.pojo;

import com.java.tonystark.server.IAdminCtrl;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClient;

public class ClientInfoBean {
    private long mCreateTime;

    private long mHandShakeTime;

    private IClient mIClient;

    private long mLastActionTime;

    private boolean isAdministrator;

    private IAdminCtrl mIAdminCtrl;

    public IAdminCtrl getAdminCtrl() {
        return mIAdminCtrl;
    }

    public ClientInfoBean(IClient IClient, IAdminCtrl ctrl) {
        mIClient = IClient;
        mIAdminCtrl = ctrl;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        mCreateTime = createTime;
    }

    public long getHandShakeTime() {
        return mHandShakeTime;
    }

    public void setHandShakeTime(long handShakeTime) {
        mHandShakeTime = handShakeTime;
    }

    public boolean isAdministrator() {
        return isAdministrator;
    }

    public void setAdministrator(boolean administrator) {
        isAdministrator = administrator;
    }

    public IClient getIClient() {
        return mIClient;
    }

    public long getLastActionTime() {
        return mLastActionTime;
    }

    public void setLastActionTime(long lastActionTime) {
        mLastActionTime = lastActionTime;
    }
}
