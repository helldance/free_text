/**
 *
 */
package com.rayy.android.fake.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.rayy.android.fake.model.PhoneContact;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RAY
 */
public class TelephoneUtil {
    private Context mContext;
    public static final Uri uri = Uri.parse("content://sms");
    public static String[] CONTACT_PROJ = new String[]{"_id", "display_name", "normalized_number", "photo_uri", "photo_thumb_uri"};
    public static String[] PROFILE_PROJ = new String[]{"_id", "display_name", "photo_thumb_uri"};
    public static final String[] CONTACT_PROJ2 = new String[]{ContactsContract.Data.MIMETYPE, ContactsContract.Data.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI};
    public static final String SORT_ORDER =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.SORT_KEY_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME;

    public static final String MINE_PHONE = "vnd.android.cursor.item/phone_v2";
    public static final String MINE_EMAIL = "vnd.android.cursor.item/email_v2";
    public static final String MINE_NAME = "vnd.android.cursor.item/name";

    private static final String tag = TelephoneUtil.class.getSimpleName();

    public TelephoneUtil(Context context) {
        this.mContext = context;
    }

    public void createSms(String addr, String text, long time, int protocol, int read, int type) {
        ContentValues cv = new ContentValues();

        cv.put("body", text);
        cv.put("address", addr);
        cv.put("read", read);
        cv.put("type", type);
        cv.put("date", time);

        mContext.getContentResolver().insert(uri, cv);

        Log.i(tag, "created new sms");

        mContext.getContentResolver().delete(mContext.getContentResolver().insert(uri, cv), null, null);

        Log.i(tag, "refreshed list");
    }

    public String getThreadId(String dest) {
        Cursor c = mContext.getContentResolver().query(uri,
                new String[]{"thread_id"}, "address = ?",
                new String[]{dest}, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            return c.getString(0);
        }

        return null;
    }

    public static Cursor getContactProfile(Context context, String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }

        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        return cr.query(uri, CONTACT_PROJ, null, null, null);
    }

    public static Cursor getOwnProfile(Context context) {
        return getContactProfile(context, getOwnNumber(context));
    }

    public static String getOwnNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    public static String getOwnPhoto(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            cursor = cr.query(ContactsContract.Profile.CONTENT_URI, PROFILE_PROJ, null, null, null);
        }

        return CursorInspector.getContentByColumnIndex(cursor, 2);
    }

    public static List<PhoneContact> getPhoneContacts(Context context) {
        List<PhoneContact> contacts = new ArrayList<PhoneContact>();
        Map<Integer, PhoneContact> contactMap = new LinkedHashMap<Integer, PhoneContact>();
        PhoneContact contact;

        try {
            // Get all Contacts
            Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, CONTACT_PROJ2, null, null, SORT_ORDER);

            //CursorInspector.printContent(cursor);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String mineType = cursor.getString(0);
                    int idAsKey = cursor.getInt(1);
                    String supposedName = cursor.getString(2);
                    String supposedNumber = cursor.getString(3);
                    String supposedEmail = cursor.getString(4);
                    String supposedAvatar = cursor.getString(5);

                    contact = contactMap.get(idAsKey) != null ? contactMap.get(idAsKey) : new PhoneContact();

                    contact.contactId = String.valueOf(idAsKey);

                    if (mineType.equals(MINE_PHONE)) {
                        contact.name = supposedName;
                        contact.phone1 = supposedNumber;
                        contact.avatar = supposedAvatar;
                    } else if (mineType.equals(MINE_EMAIL)) {
                        // email addr.
                        contact.email = supposedEmail;
                    } else if (mineType.equals(MINE_NAME)) {
                        contact.name = supposedName;
                    }

                    if (!TextUtils.isEmpty(contact.phone1)) {
                        contactMap.put(idAsKey, contact);
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(tag, e.getMessage());
        }

        contacts.addAll(contactMap.values());

        return contacts;
    }
}
