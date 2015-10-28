/**
 *
 */
package com.rayy.android.fake.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rayy.android.fake.NotificationUtil;
import com.rayy.android.fake.R;
import com.rayy.android.fake.adapter.AcContactListAdapter;
import com.rayy.android.fake.diag.DatePickerDialogFragment;
import com.rayy.android.fake.diag.TimePickerDialogFragment;
import com.rayy.android.fake.model.PhoneContact;
import com.rayy.android.fake.util.CalendarUtil;
import com.rayy.android.fake.util.MultiPathImageLoader;
import com.rayy.android.fake.util.SmsUtil;
import com.rayy.android.fake.util.TelephoneUtil;
import com.rayy.android.fake.util.ToastUtil;
import com.rayy.android.fake.widget.CircleImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author RAY
 */
public class SmsFragment extends Fragment implements OnDateSetListener, OnTimeSetListener {
    private Context mContext;
    private View smsView;
    private EditText time, body, date;
    private AutoCompleteTextView from;
    private CircleImageView pick;
    private RadioButton read, unread, in, out, fail, success;
    private Button send;
    private ImageView p_date, p_time;
    private int isRead = -1, isIn = -1, isFail = 0;
    private final int PICK_CONTACT = 2, DATE_DIAG = 0, TIME_DIAG = 1, RESULT_FINE = -1;
    private int picked = -1;
    private int mYear, mMonth, mDay, mHour, mMin, mSec;
    private ArrayList<String> numList = new ArrayList<String>();
    private ProgressDialog diag;
    private SQLiteDatabase mDB;
    private CursorFactory curf;
    private Cursor cur;
    private final String TBL_CHECK = "select * from sqlite_master where type = 'table' and name = 'TBL_FAKE_LOG'";
    private final String C_TABLE = "create table TBL_FAKE_LOG (_id integer primary key autoincrement, type integer, addr text, time text, content text)";
    private final String Q_DATA = "select * from TBL_FAKE_LOG order by _id desc";
    private Dialog diag2;
    private boolean log_on = true, notification_on = true, notification_need = false;
    private SharedPreferences spref;
    private String thread_on_modify;
    private long thread_date_new;
    private String timeStr, dateStr;
    private PhoneContact selectedContact;
    private final String tag = "SmsFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getActivity();

        if (container == null)
            return null;

        smsView = inflater.inflate(R.layout.sms, container, false);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMin = c.get(Calendar.MINUTE);

        from = (AutoCompleteTextView) smsView.findViewById(R.id.from);
        from.setAdapter(new AcContactListAdapter(mContext, TelephoneUtil.getPhoneContacts(mContext)));
        from.setThreshold(1);
        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedContact = (PhoneContact) adapterView.getAdapter().getItem(i);
                from.setText(selectedContact.name + " " + selectedContact.phone1);
                from.setSelection(from.getText().length());

                // set right drawable
                MultiPathImageLoader.loadImage(pick, selectedContact.avatar);
            }
        });

        time = (EditText) smsView.findViewById(R.id.time);
        date = (EditText) smsView.findViewById(R.id.date);
        body = (EditText) smsView.findViewById(R.id.body);

        final RadioGroup rg = (RadioGroup) smsView.findViewById(R.id.radio);
        read = (RadioButton) smsView.findViewById(R.id.read);
        unread = (RadioButton) smsView.findViewById(R.id.unread);

        final RadioGroup rg_out = (RadioGroup) smsView.findViewById(R.id.radio_fail);
        fail = (RadioButton) smsView.findViewById(R.id.out_f);
        success = (RadioButton) smsView.findViewById(R.id.out_s);

        in = (RadioButton) smsView.findViewById(R.id.in);
        in.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    rg.setVisibility(View.VISIBLE);
                    rg_out.setVisibility(View.GONE);
                }
            }

        });
        /*
         * if (in.isChecked()){ }
		 */
        out = (RadioButton) smsView.findViewById(R.id.out);
        out.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    rg.setVisibility(View.GONE);
                    rg_out.setVisibility(View.VISIBLE);
                }
            }

        });

        pick = (CircleImageView) smsView.findViewById(R.id.pick);
        pick.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                pickContact();
            }

        });

        p_date = (ImageView) smsView.findViewById(R.id.p_date);
        p_date.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                //SmsFragment.this.getActivity().showDialog(DATE_DIAG);
                showDateDialog();
            }

        });

        p_time = (ImageView) smsView.findViewById(R.id.p_time);
        p_time.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                //SmsFragment.this.getActivity().showDialog(TIME_DIAG);
                showTimeDialog();
            }

        });

        send = (Button) smsView.findViewById(R.id.send);
        send.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                sendMessage();
            }
        });

        return smsView;
    }

    private void showDateDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment dateFragment = new DatePickerDialogFragment(this);
        dateFragment.show(ft, "d_dialog");
    }

    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        // TODO Auto-generated method stub
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        String mY, mM, mD;
        mY = mYear + "";
        mM = mMonth + 1 < 10 ? "0" + (mMonth + 1) : (mMonth + 1)
                + "";
        mD = mDay < 10 ? "0" + mDay : mDay + "";
        date.setText(new StringBuilder().append(mD).append("-")
                .append(mM).append("-").append(mY).append(" "));
    }

    private void showTimeDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment timeFragment = new TimePickerDialogFragment(this);
        timeFragment.show(ft, "t_dialog");
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        mHour = hour;
        mMin = minute;
        String mH, mM;
        mH = mHour < 10 ? "0" + hour : "" + hour;
        mM = mMin < 10 ? "0" + mMin : "" + mMin;
        time.setText(new StringBuilder().append(mH).append(":")
                .append(mM));
    }

    protected void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void onActivityResult(int requstCode, int resultCode, Intent data) {

        if (requstCode == PICK_CONTACT) {
            if (resultCode == RESULT_FINE) {
                Uri contactData = data.getData();
                Cursor c = this.getActivity().managedQuery(contactData, null, null, null, null);
                ContentResolver cr = mContext.getContentResolver();

                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String avatar = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                    if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},
                                null);

                        numList.clear();

                        while (pCur.moveToNext()) {
                            for (int i = 0; i < pCur.getColumnCount(); i++) {
                                if (pCur.getColumnName(i).equalsIgnoreCase("data1")) {
                                    String num = (pCur.getString(i).replace("-", ""));
                                    if (!numList.contains(num)) {
                                        numList.add(num); // ???
                                    }
                                }
                            }
                        }

                        selectedContact = new PhoneContact();
                        selectedContact.name = name;
                        selectedContact.avatar = avatar;
                        selectedContact.contactId = id;

                        MultiPathImageLoader.loadImage(pick, avatar);

                        if (numList.size() == 1) {
                            selectedContact.phone1 = numList.get(0);
                            from.setText(name + " " + numList.get(0));
                        } else {
                            numberChooser(numList, name);
                        }

                        pCur.close();
                    }
                }
            }
        }
    }

    protected void numberChooser(ArrayList<String> list, final String name) {

        final String[] temp = new String[list.size()];

        for (int i = 0; i < list.size(); i++)
            temp[i] = list.get(i);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.title_pick_num));
        builder.setSingleChoiceItems(temp, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        picked = id;
                        from.setText(name + " " + temp[picked]);
                        selectedContact.phone1 = temp[picked];
                    }
                });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Log.v("TAG", String.valueOf(picked));
            }
        });

        builder.create().show();
    }

    protected void sendMessage() {
        String temp = from.getText().toString();
        String dest = "";

        if (!temp.equalsIgnoreCase("")) {
            if (temp.contains(">"))
                dest = temp.substring(temp.indexOf(">") + 2);
            else
                dest = temp;

            Log.i(tag, "dest " + dest);
        }

        Uri uri = Uri.parse("content://sms");

		/*
         * c.moveToFirst(); for (int i = 0; i < c.getColumnCount(); i ++){
		 * Log.i(tag, c.getColumnName(i) +" " + c.getString(i)); }
		 */

        ContentValues cv = new ContentValues();

        String smsText = body.getText().toString();
        int smsType = -1;

        cv.put("body", smsText);
        cv.put("address", dest);
        if (read.isChecked())
            isRead = 1;
        if (unread.isChecked()) {
            isRead = 0;
        }
        if (in.isChecked()) {
            isIn = 1;
            cv.put("protocol", 0);
            //notification_need = true;
            smsType = 0;
        }
        if (out.isChecked()) {
            // Added on 08/03/2011 for Fail Send function
            if (fail.isChecked()) {
                isIn = 5;
                smsType = 1;
                //notification_need = true;
            } else {
                isIn = 2;
                smsType = 0;
                //notification_need = false;
            }
        }

        cv.put("read", isRead);
        cv.put("type", isIn);

        Date calldate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

        if (time.getText().toString().equalsIgnoreCase("")
                || date.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(mContext,
                    getResources().getString(R.string.tst_null_time),
                    Toast.LENGTH_SHORT).show();
            /*Drawable errorIcon = getResources().getDrawable(R.drawable.edit_error_2);
            errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
			time.setError(null, errorIcon);*/

            return;
        }

        if (time.getText().toString().equals(getString(R.string.time))) {
            timeStr = CalendarUtil.timeNow();
        }

        if (date.getText().toString().equals(getString(R.string.date))) {
            dateStr = CalendarUtil.dateNow();
        }

        try {
            calldate = sdf.parse((time.getText().toString().equals(getString(R.string.time)) ? timeStr : time.getText().toString()) + " "
                    + (date.getText().toString().equals(getString(R.string.date)) ? dateStr : date.getText().toString()));
            thread_date_new = calldate.getTime();
            cv.put("date", thread_date_new);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        diag2 = new Dialog(mContext, R.style.dialog);
        diag2.setContentView(R.layout.diag_load);
        diag2.show();

//        Timer tm = new Timer();
//        TimerTask task = new TimerTask() {
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                Message msg = handler.obtainMessage();
//                Bundle b = new Bundle();
//                b.putInt("total", 999);
//                msg.setData(b);
//                handler.sendMessage(msg);
//            }
//
//        };
//
//        tm.schedule(task, 1500);

        //mContext.getContentResolver().insert(uri, cv);

        new SmsUtil(mContext).createSms(dest, smsText, thread_date_new, 1, isRead, isIn);

        ToastUtil.showToast(mContext, getResources().getString(R.string.tst_msg_succ));

        //if (!notification_on)
        //new SmsUtil (mContext).createSms(dest, smsText, thread_date_new, 1, isRead, isIn);

        if (notification_on && isIn != 2) {
            String smsFrom = matchContact(dest);
            String ticker = isIn == 5 ? getString(R.string.sms_fail) : smsFrom + " : " + smsText;
            String title = isIn == 5 ? getString(R.string.sms_fail) : smsFrom;
            String content = isIn == 5 ? null : smsText;

            int icon = (isIn == 1) ? R.drawable.stat_notify_sms : R.drawable.stat_notify_sms_failed;
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://mms-sms/conversations/" + new SmsUtil(mContext).getThreadId(dest)));

            new NotificationUtil(mContext).createNotification(icon, ticker, thread_date_new, notificationIntent, title, content);
        }

		/*else {
			String smsFrom = matchContact(dest);
			
			Intent ni = new Intent(mContext, AlarmReceiver.class);
			
			Bundle bundle = new Bundle();
			bundle.putInt("which", 0);
			bundle.putString("from", smsFrom);
			bundle.putString("smsText", smsText);
			bundle.putLong("smsDate", thread_date_new);
			bundle.putInt("smsType", isIn);
			
			bundle.putString("address", dest);
			bundle.putInt("read", isRead);
			bundle.putString("thread", thread_on_modify);

			ni.putExtras(bundle);

			PendingIntent pi = PendingIntent.getBroadcast(mContext, 12580, ni,
					PendingIntent.FLAG_UPDATE_CURRENT);

			AlarmManager am = (AlarmManager) mContext
					.getSystemService(Service.ALARM_SERVICE);

			am.set(AlarmManager.RTC_WAKEUP, thread_date_new, pi);
		}*/

        //Toast.makeText(this, "Task is scheduled", Toast.LENGTH_SHORT).show();

        Log.i(tag, "future notification set");

//		if (log_on) {
//			setupDB();
//
//			if (calldate != null)
//				addLog(0, from.getText().toString(), calldate.getTime() + "",
//						body.getText().toString());
//		}

    }

//    final Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            int total = msg.getData().getInt("total");
//            Log.v("TAG", "2. Handler message get.." + total);
//            if (total == 999) {
//                diag2.dismiss();
//                Toast.makeText(mContext,
//                        getResources().getString(R.string.tst_msg_succ),
//                        Toast.LENGTH_SHORT).show();
//                Log.v(tag, "2. Handler message finished..");
//            }
//        }
//    };

    private void setupDB() {
        try {
            // Open database for transcation or create if not exists.
            // mDB = SQLiteDatabase.openOrCreateDatabase("sdcard/mDB.db", curf);
            // User context to create database. Default path under app folder.
            mDB = mContext.openOrCreateDatabase("mDB.db",
                    Context.MODE_PRIVATE, curf);

            // If not already exists, create snap message data table.
            if (mDB.rawQuery(TBL_CHECK, null).getCount() == 0) {
                mDB.beginTransaction();
                mDB.execSQL(C_TABLE);
                mDB.setTransactionSuccessful();
                mDB.endTransaction();

                Log.v("TAG", "Created Message Table");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            SQLiteDatabase.releaseMemory();
        }
        Log.v("Database", "DB Ready");
    }

    private void addLog(int i, String addr, String date, String content) {
        String addr2;
        if (!addr.equalsIgnoreCase(""))
            addr2 = matchContact(addr);
        else
            addr2 = addr;

        String INS = "insert into TBL_FAKE_LOG (type, addr, time, content) values ("
                + i + ",'" + addr2 + "','" + date + "','" + content + "')";
        Log.d("TAG", INS);

        try {
            mDB.beginTransaction();
            mDB.execSQL(INS);
            mDB.setTransactionSuccessful();
            mDB.endTransaction();

            Log.v("Database", "New Record Created");
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            mDB.close();
        }
    }

    public String matchContact(String num) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(num));
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME},
                null, null, null);

        while (c.moveToNext()) {
            return c.getString(0);
        }

        return num;
    }

    public void showSMSNotification(String smsFrom, String smsText, long smsDate) {
        NotificationManager mgnr = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);

        int icon = R.drawable.stat_notify_sms;

        Notification notification = new Notification(icon, smsText, smsDate);

        Intent notificationIntent = new Intent(mContext, SmsFragment.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

        notification.setLatestEventInfo(mContext, smsFrom, smsText, contentIntent);

        mgnr.notify(0, notification);
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();

        spref = PreferenceManager.getDefaultSharedPreferences(mContext);
        notification_on = spref.getBoolean("notification_on", false);
    }

}
