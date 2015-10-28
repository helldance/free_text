package com.rayy.android.fake.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @Author yangw
 * @Date 8/7/15 10:13 PM.
 */
public class ToastUtil {
    static long lastShownTime;
    static String lastShowMessage = "";
    static long TOAST_SHOW_INTERVAL = 5000;

    public static void showToast(Context context, String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * delay or discard showing repeated toasts, e.g., very frequent request fails
     *
     * @param context
     * @param msg
     */
    public static void showRepeatToast(Context context, String msg) {
        long currentTime = System.currentTimeMillis();

        if (msg != null) {
            if (!msg.equalsIgnoreCase(lastShowMessage) || (currentTime - lastShownTime) > TOAST_SHOW_INTERVAL) {
                showToast(context, msg);

                lastShownTime = currentTime;
            }
        }
    }
}
