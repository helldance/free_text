/**
 * 
 */
package com.rayy.android.fake.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * @author RAY
 *
 */
public class SmsUtil {
	private Context mContext;
	private static final Uri uri = Uri.parse("content://sms");
	private static final String tag = "SmsUtil";
	
	public SmsUtil (Context context){
		this.mContext = context;
	}
	
	public void createSms (String addr, String text, long time, int protocol, int read, int type){
		ContentValues cv = new ContentValues();
		
		cv.put("body", text);
		cv.put("address", addr);
		cv.put("read", read);
		cv.put("type", type);
		cv.put("date", time);
		
		mContext.getContentResolver().insert(uri, cv);
		
		Log.i(tag, "created new sms");
		
		mContext.getContentResolver().delete(mContext.getContentResolver().insert(uri, cv),	null, null);
		
		Log.i(tag, "refreshed list");
	}
	
	public String getThreadId (String dest){
		Cursor c = mContext.getContentResolver().query(uri,
				new String[] { "thread_id" }, "address = ?",
				new String[] { dest }, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			return c.getString(0);
		}
		
		return null;
	}
}
