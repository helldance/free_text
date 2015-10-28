package com.rayy.android.fake.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

/**
 * helper class wrapped with common activity operations
 *
 * @Author yangw
 * @Date 18/7/15 11:01 AM.
 */
public class ActivityHelper {
    private static final String tag = ActivityHelper.class.getSimpleName();

    public static void startAndFinish(Activity activity, Class<?> clazz, boolean finish) {
        start(activity, clazz);

        if (finish) {
            activity.finish();
        }
    }

    public static void start(Activity activity, Class<?> clazz) {
        Intent intent = new Intent(activity, clazz);
        activity.startActivity(intent);
    }

    public static void startWithBundle(Activity activity, Class<?> clazz, Bundle bundle, boolean finish) {
        Intent intent = new Intent(activity, clazz);
        intent.putExtras(bundle);

        activity.startActivity(intent);

        if (finish) {
            activity.finish();
        }
    }

    public static void startIntentActivityAndFinish(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.finish();
    }

    protected static void delayFinish(final Activity activity, long delay) {
        new CountDownTimer(delay, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                // do nothing
            }

            @Override
            public void onFinish() {
                activity.finish();
            }
        }.start();
    }
}
