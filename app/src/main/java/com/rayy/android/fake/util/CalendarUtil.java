package com.rayy.android.fake.util;

import java.util.Calendar;

/**
 * @Author yangw
 * @Date 19/10/15 7:50 PM.
 */
public class CalendarUtil {
    static int mYear, mMonth, mDay, mHour, mMin;

    static {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMin = c.get(Calendar.MINUTE);
    }

    public static String timeNow() {
        return mHour + ":" + mMin;
    }

    public static String dateNow() {
        String mM = mMonth + 1 < 10 ? "0" + (mMonth + 1) : (mMonth + 1)
                + "";
        String mD = mDay < 10 ? "0" + mDay : mDay + "";

        return mD + "-" + mM + "-" + mYear;
    }
}
