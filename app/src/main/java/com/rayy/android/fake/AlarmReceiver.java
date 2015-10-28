/**
 * Copyright @2010 Rayy.
 * AlarmReceiver.java
 */
package com.rayy.android.fake;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.rayy.android.fake.util.CallUtil;
import com.rayy.android.fake.util.SmsUtil;

/**
 * @author rayy
 * @date Jan 25, 2011
 */
public class AlarmReceiver extends BroadcastReceiver {
  
	private String from, smsText;
	private String addr, callName, callDuration;
	private long smsDate, callDate;
	private int smsType, read, callType = -3, which;
	private static final String tag = "AlarmReceiver";
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		// TODO Auto-generated method stub
		Bundle bundle = intent.getExtras();

		which = bundle.getInt("which");
		from = bundle.getString("from");
		addr = bundle.getString("address");
		
		NotificationManager mgnr = (NotificationManager) ctx.getSystemService(Service.NOTIFICATION_SERVICE);
		
		int icon;
		Notification notification = null; 
		Intent notificationIntent = null;
		
		if (which == 0){
			smsDate = bundle.getLong("smsDate");
			smsText = bundle.getString("smsText");
			smsType = bundle.getInt("smsType");
		
			read = bundle.getInt("read");
			
			new SmsUtil(ctx).createSms(addr, smsText, smsDate, 0, read, smsType);
			
			if (smsType != 2){
				icon = (smsType == 1) ? R.drawable.stat_notify_sms: R.drawable.stat_notify_sms_failed;
				notification = new Notification(icon, from + " : " + smsText, smsDate);
				notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://mms-sms/conversations/" + new SmsUtil(ctx).getThreadId(addr)));
			}
		}
	
		else if (which == 1){
			callName = bundle.getString("name");
			callDate = bundle.getLong("callDate");
			callDuration = bundle.getString("duration");
			callType = bundle.getInt("callType");
			
			new CallUtil(ctx).createCall(addr, callName, callDate, callDuration, callType);
			
			if (callType == 3){
				icon = R.drawable.stat_notify_missed_call;
				notification = new Notification(icon, null, callDate);

				smsText = from;
				from = "Missed call";
				
				notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://call_log/calls"));
				notificationIntent.setType("vnd.android.cursor.dir/calls");
			}
		}		
		
		if (notification != null) {
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,	notificationIntent, 0);

			notification.setLatestEventInfo(ctx, from, smsText, contentIntent);

			mgnr.notify(0, notification);
		}
	}
}
