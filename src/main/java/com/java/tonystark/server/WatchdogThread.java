package com.java.tonystark.server;

import com.java.tonystark.server.pojo.ClientInfoBean;
import com.java.tonystark.server.utils.Log;
import com.xuhao.didi.socket.common.interfaces.basic.AbsLoopThread;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class WatchdogThread extends AbsLoopThread {


    private static final long ONE_MINUTE = 60 * 1000;

    private ConcurrentHashMap<String, ClientInfoBean> mMap = null;

    public WatchdogThread(ConcurrentHashMap<String, ClientInfoBean> map) {
        mMap = map;
    }

    @Override
    protected void beforeLoop() throws Exception {
        super.beforeLoop();
    }

    @Override
    protected void runInLoopThread() throws Exception {
        HashMap<String, ClientInfoBean> tempMap = new HashMap<>();
        tempMap.putAll(mMap);

        long nowMills = System.currentTimeMillis();
        Iterator<String> it = tempMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            ClientInfoBean bean = tempMap.get(key);
            long lastMills = bean.getLastActionTime();
            long handshakeMills = bean.getHandShakeTime();
            long createMills = bean.getCreateTime();
            if (lastMills == 0) {
                lastMills = handshakeMills;
                if (handshakeMills == 0) {
                    lastMills = createMills;
                }
            }
            long timeDiffer = nowMills - lastMills;
            if (timeDiffer > ONE_MINUTE * 2) {//大于2分钟了表明已死
                bean.getIClient().disconnect(new RuntimeException("超时断开.超时时间为:" + timeDiffer / ONE_MINUTE + "分钟"));
                it.remove();
            }
        }
        Thread.sleep(10 * 1000);//每10秒一次检查
    }

    @Override
    protected void loopFinish(Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
    }
}
