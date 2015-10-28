/**
 * 
 */
package com.rayy.android.fake;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

/**
 * @author RAY
 *
 */
public class NotificationUtil {
	
	private NotificationManager mgnr; 
	private Context mContext;
	
	public NotificationUtil (Context mContext){
		this.mContext = mContext;
		mgnr = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);
	}
	
	public void createNotification (int icon, CharSequence tickerText, long when, Intent notificationIntent, String contentTitle, String contentContent){
		Notification notification = new Notification(icon, tickerText, when);
		
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

		notification.setLatestEventInfo(mContext, contentTitle, contentContent, contentIntent);

		mgnr.notify(0, notification);
	
	}
}
