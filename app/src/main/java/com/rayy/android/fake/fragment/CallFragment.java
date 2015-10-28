/**
 *
 */
package com.rayy.android.fake.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
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
import android.os.Handler;
import android.os.Message;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rayy.android.fake.NotificationUtil;
import com.rayy.android.fake.R;
import com.rayy.android.fake.adapter.AcContactListAdapter;
import com.rayy.android.fake.diag.DatePickerDialogFragment;
import com.rayy.android.fake.diag.TimePickerDialogFragment;
import com.rayy.android.fake.model.PhoneContact;
import com.rayy.android.fake.util.CalendarUtil;
import com.rayy.android.fake.util.CallUtil;
import com.rayy.android.fake.util.TelephoneUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author RAY
 */
public class CallFragment extends Fragment implements OnDateSetListener, OnTimeSetListener {
    private Context mContext;
    private View callView;
    private EditText time, date, duration, call_name;
    private AutoCompleteTextView from;
    private Spinner type;
    private Button add;
    private ImageButton pick, p_date, p_time;
    private final int PICK_CONTACT = 2, DATE_DIAG = 0, TIME_DIAG = 1, RESULT_FINE = -1;
    private int picked = -1;
    private int mYear, mMonth, mDay, mHour, mMin, mSec;
    private ArrayList<String> numList = new ArrayList<String>();
    private SQLiteDatabase mDB;
    private CursorFactory curf;
    private final String TBL_CHECK = "select * from sqlite_master where type = 'table' and name = 'TBL_FAKE_LOG'";
    private final String C_TABLE = "create table TBL_FAKE_LOG (_id integer primary key autoincrement, type integer, addr text, time text, content text)";
    private final String Q_DATA = "select * from TBL_FAKE_LOG order by _id desc";
    private Dialog diag2;
    private boolean log_on = true, notification_on = true, notification_need = false;
    private SharedPreferences spref;
    static final String tag = "FakeCall";
    private String dateStr, timeStr;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getActivity();

        if (container == null)
            return null;

        callView = inflater.inflate(R.layout.call, container, false);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMin = c.get(Calendar.MINUTE);

        from = (AutoCompleteTextView) callView.findViewById(R.id.from);
        from.setAdapter(new AcContactListAdapter(mContext, TelephoneUtil.getPhoneContacts(mContext)));
        from.setThreshold(1);
        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PhoneContact selectedContact = (PhoneContact) adapterView.getAdapter().getItem(i);
                from.setText(selectedContact.name + " " + selectedContact.phone1);
                from.setSelection(from.getText().length());
            }
        });
        time = (EditText) callView.findViewById(R.id.time);
        date = (EditText) callView.findViewById(R.id.date);
        duration = (EditText) callView.findViewById(R.id.duration);

        String type0 = getResources().getString(R.string.type);
        String type1 = getResources().getString(R.string.type1);
        String type2 = getResources().getString(R.string.type2);
        String type3 = getResources().getString(R.string.type3);

        type = (Spinner) callView.findViewById(R.id.type);
        type.setPrompt(type0);
        String[] call_type = new String[]{type1, type2, type3};

        if (mContext == null) {
            Log.i(tag, "mContext is null");

            return null;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, call_type);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        pick = (ImageButton) callView.findViewById(R.id.pick);
        pick.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                pickContact();
            }

        });

        p_date = (ImageButton) callView.findViewById(R.id.p_date);
        p_date.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                //CallFragment.this.getActivity().showDialog(DATE_DIAG);
                showDateDialog();
            }

        });

        p_time = (ImageButton) callView.findViewById(R.id.p_time);
        p_time.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                //CallFragment.this.getActivity().showDialog(TIME_DIAG);
                showTimeDialog();
            }

        });

        add = (Button) callView.findViewById(R.id.add);
        add.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                addCalllog();
            }
        });

        return callView;
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
        // TODO Auto-generated method stub
        mHour = hour;
        mMin = minute;
        String mH, mM;
        mH = mHour < 10 ? "0" + hour : "" + hour;
        mM = mMin < 10 ? "0" + mMin : "" + mMin;
        time.setText(new StringBuilder().append(mH).append(":")
                .append(mM));
    }

    protected void pickContact() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    protected void numberChooser(ArrayList<String> list, final String name) {

        final String[] temp = new String[list.size()];

        for (int i = 0; i < list.size(); i++)
            temp[i] = list.get(i);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.title_pick_num));
        builder.setSingleChoiceItems(temp, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                picked = id;
                from.setText("<" + name + "> " + temp[picked]);
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

    public void onActivityResult(int requstCode, int resultCode, Intent data) {

        if (requstCode == PICK_CONTACT) {
            if (resultCode == RESULT_FINE) {
                Uri contactData = data.getData();
                Cursor c = this.getActivity().managedQuery(contactData, null, null, null, null);
                ContentResolver cr = mContext.getContentResolver();

                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                    if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                + " = ?", new String[]{id}, null);

                        Log.i("TAG", "Size: " + pCur.getCount());
                        numList.clear();

                        while (pCur.moveToNext()) {
                            for (int i = 0; i < pCur.getColumnCount(); i++) {
                                if (pCur.getColumnName(i).equalsIgnoreCase("data1")) {
                                    numList.add((pCur.getString(i).replace("-", ""))); // ???
                                    Log.v(tag, "Added: " + pCur.getString(i));
                                }
                            }
                        }

                        if (numList.size() == 1) {
                            from.setText("<" + name + "> " + numList.get(0));
                        } else {
                            numberChooser(numList, name);
                        }
                        if (call_name.getVisibility() == View.VISIBLE) {
                            call_name.setText(name);
                        }

                        pCur.close();
                    }
                }
            }
        }
    }

    protected void addCalllog() {
        // TODO Auto-generated method stub
        Uri uri = Uri.parse("content://call_log/calls");

        ContentValues cv = new ContentValues();

        if (from.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(mContext, getResources().getString(R.string.tst_call_nullnum), Toast.LENGTH_SHORT).show();
            return;
        }

        String temp = from.getText().toString();
        String dest = "";

        if (temp.contains(">"))
            dest = temp.substring(temp.indexOf(">") + 2);
        else
            dest = temp;

        Log.i(tag, "dest " + dest);
        //String dest = temp.substring(temp.indexOf("-") + 1);

        String callDuration = null == String.valueOf(duration.getText()) ? "0" : String.valueOf(duration.getText());

        cv.put("duration", callDuration);
        cv.put("number", null == temp ? "No Name" : dest);

        int callType = type.getSelectedItemPosition() + 1;

        cv.put("type", callType);

        //notification_need = callType == 3 ? true : false;

        // cv.put("name", "Mr. ....");
        String callName = "";
        if (call_name.getVisibility() == View.VISIBLE) {
            callName = String.valueOf(call_name.getText());
            cv.put("name", callName);
        }

        Date calldate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

        if (time.getText().toString().equalsIgnoreCase("") || date.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(mContext, getResources().getString(R.string.tst_null_time), Toast.LENGTH_SHORT).show();
            return;
        }

        if (time.getText().toString().equals(getString(R.string.time))) {
            timeStr = CalendarUtil.timeNow();
        }

        if (date.getText().toString().equals(getString(R.string.date))) {
            dateStr = CalendarUtil.dateNow();
        }

        long callTime = 0;
        try {
            calldate = sdf.parse((time.getText().toString().equals(getString(R.string.time)) ? timeStr : time.getText().toString()) + " "
                    + (date.getText().toString().equals(getString(R.string.date)) ? dateStr : date.getText().toString()));
            callTime = calldate.getTime();
            cv.put("date", callTime);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (calldate == null) {
            Toast.makeText(mContext, "Date or time is of wrong format", Toast.LENGTH_SHORT).show();
            return;
        }

        diag2 = new Dialog(mContext, R.style.dialog);
        diag2.setContentView(R.layout.diag_load);
        diag2.show();

        Timer tm = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message msg = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("total", 999);
                msg.setData(b);
                handler.sendMessage(msg);
            }

        };

        tm.schedule(task, 1500);

        //this.getActivity().getContentResolver().insert(uri, cv);

        new CallUtil(mContext).createCall(dest, callName, callTime, callDuration, callType);

        //if (!notification_on)
        //new CallUtil(mContext).createCall(dest, callName, callTime, callDuration, callType);
        if (notification_on && callType == 3) {
            String callFrom = matchContact(dest);
            int icon = R.drawable.stat_notify_missed_call;

            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://call_log/calls"));
            notificationIntent.setType("vnd.android.cursor.dir/calls");

            new NotificationUtil(mContext).createNotification(icon, null, callTime, notificationIntent, getString(R.string.miss_call), callFrom);
        }

		/*if (notification_on) {
            String callFrom = matchContact(dest);
			
			Intent ni = new Intent(mContext, AlarmReceiver.class);
			
			Bundle bundle = new Bundle();
			bundle.putInt("which", 1);
			bundle.putString("from", callFrom);
			bundle.putLong("callDate", callTime);
			bundle.putInt("callType", callType);

			bundle.putString("address", dest);
			bundle.putString("name", callName);
			bundle.putString("duration", callDuration);
			
			ni.putExtras(bundle);

			PendingIntent pi = PendingIntent.getBroadcast(mContext, 12580, ni, PendingIntent.FLAG_UPDATE_CURRENT);

			AlarmManager am = (AlarmManager) mContext.getSystemService(Service.ALARM_SERVICE);

			am.set(AlarmManager.RTC_WAKEUP, callTime, pi);
		}*/

        //Log.i(tag, "future notification set");

//        if (log_on) {
//            setupDB();
//
//            if (calldate != null)
//                addLog(1, from.getText().toString(), calldate.getTime() + "", duration.getText().toString());
//        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.getData().getInt("total");
            Log.v("TAG", "2. Handler message get.." + total);
            if (total == 999) {
                // dismissDialog(PROGRESS_DIALOG);
                diag2.dismiss();
                Toast.makeText(mContext, getResources().getString(R.string.tst_call_succ), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void setupDB() {
        try {
            // Open database for transcation or create if not exists.
            // mDB = SQLiteDatabase.openOrCreateDatabase("sdcard/mDB.db", curf);
            // User context to create database. Default path under app folder.
            mDB = this.getActivity().openOrCreateDatabase("mDB.db", Context.MODE_PRIVATE, curf);

            // If not already exists, create snap message data table.
            if (mDB.rawQuery(TBL_CHECK, null).getCount() == 0) {
                mDB.beginTransaction();
                mDB.execSQL(C_TABLE);
                mDB.setTransactionSuccessful();
                mDB.endTransaction();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            SQLiteDatabase.releaseMemory();
        }
        Log.v("Database", "DB Ready");
    }

    private void addLog(int i, String addr, String date, String content) {
        String addr2 = matchContact(addr);
        String INS = "insert into TBL_FAKE_LOG (type, addr, time, content) values (" + i + ",'" + addr2 + "','" + date + "','" + content + "')";
        Log.d("TAG", INS);

        try {
            mDB.beginTransaction();
            mDB.execSQL(INS);
            mDB.setTransactionSuccessful();
            mDB.endTransaction();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            if (mDB != null)
                mDB.close();
        }
    }

    public String matchContact(String num) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
        ContentResolver cr = this.getActivity().getContentResolver();
        Cursor c = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);

        while (c.moveToNext()) {
            return c.getString(0);
        }

        return num;
    }

    @Override
    public void onResume() {
        super.onResume();

        call_name = (EditText) callView.findViewById(R.id.name);
        spref = PreferenceManager.getDefaultSharedPreferences(mContext);
        call_name.setVisibility(spref.getBoolean("name_on", false) ? View.VISIBLE : View.GONE);
        notification_on = spref.getBoolean("notification_on", false);
    }
}
