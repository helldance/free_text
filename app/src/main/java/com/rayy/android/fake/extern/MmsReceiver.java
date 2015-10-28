package com.rayy.android.fake.extern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Yang Wei
 * @Date Mar 21, 2014
 */
public class MmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("", "Rcve Sms, doing nothing");
	}

}
