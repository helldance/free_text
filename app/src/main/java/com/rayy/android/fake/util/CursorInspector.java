package com.rayy.android.fake.util;

import android.database.Cursor;
import android.util.Log;

/**
 * @Author yangw
 * @Date 8/10/15 11:36 AM.
 */
public class CursorInspector {
    static final String tag = CursorInspector.class.getSimpleName();

    public static void printFirstEntry(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int cols = cursor.getColumnCount();

            for (int i = 0; i < cols; i++) {
                Log.i(tag, i + " " + cursor.getColumnName(i) + " : " + cursor.getString(i));
            }
        }
    }

    public static void printContent(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int cols = cursor.getColumnCount();

            do {
                for (int i = 0; i < cols; i++) {
                    Log.i(tag, i + " " + cursor.getColumnName(i) + " : " + cursor.getString(i));
                }
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    // applicable to cursor with single entry
    public static String getContentByColumnIndex(Cursor cursor, int index) {
        cursor.moveToFirst();

        return cursor.getString(index);
    }
}
