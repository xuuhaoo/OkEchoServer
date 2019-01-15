package com.java.tonystark.server.pojo;

import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;

import java.util.Arrays;

public class OriginalWriteBean implements ISendable {
    private OriginalData mOriginalData;

    public OriginalWriteBean(OriginalData originalData) {
        mOriginalData = originalData;
    }

    @Override
    public byte[] parse() {
        byte[] head = mOriginalData.getHeadBytes();
        byte[] body = mOriginalData.getBodyBytes();
        int headLength = head.length;
        int bodyLength = body.length;

        head = Arrays.copyOf(head, headLength + bodyLength);
        System.arraycopy(body, 0, head, headLength, bodyLength);
        return head;
    }
}
