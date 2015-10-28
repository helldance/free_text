/**
 * 
 */
package com.rayy.android.fake.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * @author RAY
 *
 */
public class CallUtil {
	private Context mContext;
	private static final Uri uri = Uri.parse("content://call_log/calls");
	private static final String tag = "CallUtil";
	
	public CallUtil (Context context){
		this.mContext = context;
	}
	
	public void createCall (String addr, String name, long time, String duration, int type){
		ContentValues cv = new ContentValues();
		
		cv.put("duration", null == duration ? "0" : duration);
		cv.put("number", addr);		
		cv.put("type", type);
		if (name != "")
			cv.put("name", name);
		cv.put("date", time);
		
		mContext.getContentResolver().insert(uri, cv);
		
		Log.i(tag, "created new call");
	}
}
