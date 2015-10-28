package com.rayy.android.fake.model;

/**
 * @Author yangw
 * @Date 7/10/15 3:21 PM.
 */
public class SmsMessage {
    public int msgId;
    public int threadId;
    public int type;
    public String addr;
    public String msg;
    public long timestamp;

    public SmsMessage(int msgId, int threadId, int type, String addr, String msg, long timestamp) {
        this.msgId = msgId;
        this.threadId = threadId;
        this.type = type;
        this.addr = addr;
        this.msg = msg;
        this.timestamp = timestamp;
    }
}
