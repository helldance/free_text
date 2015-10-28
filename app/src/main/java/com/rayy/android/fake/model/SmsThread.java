package com.rayy.android.fake.model;

/**
 * @Author yangw
 * @Date 7/10/15 3:17 PM.
 */
public class SmsThread {
    public int theadId;
    public String avatar;
    public String from;
    public String displayName;
    public String content;
    public long timestamp;

    public SmsThread(int theadId, String from, String content, long timestamp) {
        this.theadId = theadId;
        this.from = from;
        this.content = content;
        this.timestamp = timestamp;
    }
}
