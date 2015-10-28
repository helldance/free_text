/**
 * Copyright @2010 Rayy.
 * FakeCall.java
 */
package com.rayy.android.fake.obso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rayy.android.fake.R;

/**
 * @author rayy
 * @date Jan 14, 2011
 */
public class FakeCall extends Activity {

	private EditText from, time, date, duration, call_name;
	private Spinner type;
	private Button add;
	private ImageButton pick, p_date, p_time;
	private final int PICK_CONTACT = 2, DATE_DIAG = 0, TIME_DIAG = 1;
	private int picked = -1;
	private int mYear, mMonth, mDay, mHour, mMin, mSec;
	private ArrayList<String> numList = new ArrayList<String>();
	private SQLiteDatabase mDB;
	private CursorFactory curf;
	private final String TBL_CHECK = "select * from sqlite_master where type = 'table' and name = 'TBL_FAKE_LOG'";
	private final String C_TABLE = "create table TBL_FAKE_LOG (_id integer primary key autoincrement, type integer, addr text, time text, content text)";
	private final String Q_DATA = "select * from TBL_FAKE_LOG order by _id desc";
	private Dialog diag2;
	private boolean log_on = true;
	private SharedPreferences spref;
	static final String tag = "FakeCall";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.call);

		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMin = c.get(Calendar.MINUTE);

		from = (EditText) findViewById(R.id.from);
		time = (EditText) findViewById(R.id.time);
		date = (EditText) findViewById(R.id.date);
		duration = (EditText) findViewById(R.id.duration);

		String type0 = getResources().getString(R.string.type);
		String type1 = getResources().getString(R.string.type1);
		String type2 = getResources().getString(R.string.type2);
		String type3 = getResources().getString(R.string.type3);

		type = (Spinner) findViewById(R.id.type);
		type.setPrompt(type0);
		String[] call_type = new String[] { type1, type2, type3 };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, call_type);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		type.setAdapter(adapter);

		pick = (ImageButton) findViewById(R.id.pick);
		pick.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				pickContact();
			}

		});

		p_date = (ImageButton) findViewById(R.id.p_date);
		p_date.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DATE_DIAG);
			}

		});

		p_time = (ImageButton) findViewById(R.id.p_time);
		p_time.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(TIME_DIAG);
			}

		});

		add = (Button) findViewById(R.id.add);
		add.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				addCalllog();
			}
		});
	}

	protected void pickContact() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);
	}

	protected void onActivityResult(int requstCode, int resultCode, Intent data) {

		if (requstCode == PICK_CONTACT) {
			if (resultCode == RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				ContentResolver cr = getContentResolver();

				if (c.moveToFirst()) {
					String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

					if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { id }, null);

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
							from.setText("<" + name + ">-" + numList.get(0));
						} else {
							numberChooser(numList, name);
						}
						if (call_name.getVisibility() == View.VISIBLE){
							call_name.setText(name);
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

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.title_pick_num));
		builder.setSingleChoiceItems(temp, -1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				picked = id;
				from.setText("<" + name + ">-" + temp[picked]);
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

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIAG:
			DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					mYear = year;
					mMonth = monthOfYear;
					mDay = dayOfMonth;
					String mY, mM, mD;
					mY = mYear + "";
					mM = mMonth + 1 < 10 ? "0" + (mMonth + 1) : (mMonth + 1) + "";
					mD = mDay < 10 ? "0" + mDay : mDay + "";
					date.setText(new StringBuilder().append(mD).append("-").append(mM).append("-").append(mY).append(" "));
				}
			};
			DatePickerDialog dpDiag = new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
			return dpDiag;

		case TIME_DIAG:
			TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hour, int minute) {
					mHour = hour;
					mMin = minute;
					String mH, mM;
					mH = mHour < 10 ? "0" + hour : "" + hour;
					mM = mMin < 10 ? "0" + mMin : "" + mMin;
					time.setText(new StringBuilder().append(mH).append(":").append(mM));
				}
			};
			TimePickerDialog tpDiag = new TimePickerDialog(this, mTimeSetListener, mHour, mMin, true);
			return tpDiag;
		}

		return null;
	}

	protected void addCalllog() {
		// TODO Auto-generated method stub
		Uri uri = Uri.parse("content://call_log/calls");

		ContentValues cv = new ContentValues();

		if (from.getText().toString().equalsIgnoreCase("")) {
			Toast.makeText(FakeCall.this, getResources().getString(R.string.tst_call_nullnum), Toast.LENGTH_SHORT).show();
			return;
		}
		
		String temp = from.getText().toString();
		String dest = temp.substring(temp.indexOf("-") + 1);

		cv.put("duration", null == duration.getText().toString() ? "0" : duration.getText().toString());
		cv.put("number", null == temp ? "No Name" : dest);
		cv.put("type", type.getSelectedItemPosition() + 1);
		// cv.put("name", "Mr. ....");
		if (call_name.getVisibility() == View.VISIBLE){
			cv.put("name", String.valueOf(call_name.getText()));
		}

		Date calldate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

		if (time.getText().toString().equalsIgnoreCase("") || date.getText().toString().equalsIgnoreCase("")) {
			Toast.makeText(FakeCall.this, getResources().getString(R.string.tst_null_time), Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			calldate = sdf.parse(time.getText().toString() + " " + date.getText().toString());
			cv.put("date", calldate.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (calldate == null) {
			Toast.makeText(this, "Date or time is of wrong format and can not be parsed", Toast.LENGTH_SHORT).show();
			return;
		}

		diag2 = new Dialog(FakeCall.this, R.style.dialog);
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

		getContentResolver().insert(uri, cv);

		if (log_on) {
			setupDB();

			if (calldate != null)
				addLog(1, from.getText().toString(), calldate.getTime() + "", duration.getText().toString());
		}
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int total = msg.getData().getInt("total");
			Log.v("TAG", "2. Handler message get.." + total);
			if (total == 999) {
				// dismissDialog(PROGRESS_DIALOG);
				diag2.dismiss();
				Toast.makeText(FakeCall.this, getResources().getString(R.string.tst_call_succ), Toast.LENGTH_SHORT).show();
				Log.v("TAG", "2. Handler message finished..");
			}
		}
	};

	private void setupDB() {
		try {
			// Open database for transcation or create if not exists.
			// mDB = SQLiteDatabase.openOrCreateDatabase("sdcard/mDB.db", curf);
			// User context to create database. Default path under app folder.
			mDB = FakeCall.this.openOrCreateDatabase("mDB.db", Context.MODE_PRIVATE, curf);

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
		String addr2 = matchContact(addr);
		String INS = "insert into TBL_FAKE_LOG (type, addr, time, content) values (" + i + ",'" + addr2 + "','" + date + "','" + content + "')";
		Log.d("TAG", INS);

		try {
			mDB.beginTransaction();
			mDB.execSQL(INS);
			mDB.setTransactionSuccessful();
			mDB.endTransaction();

			Log.v("Database", "New Record Created");
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			mDB.close();
		}
	}

	public String matchContact(String num) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);

		while (c.moveToNext()) {
			return c.getString(0);
		}

		return num;
	}

	protected void viewCall() {

		Uri uri = Uri.parse(CallLog.CONTENT_URI.toString() + "/calls");

		Cursor c = getContentResolver().query(uri, null, null, null, null);
		
		c.moveToFirst();
		
		int i = c.getColumnCount();
		for (int x = 0; x < i; x ++){
			Log.i(tag, c.getColumnName(x) + " " + c.getString(x));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		call_name = (EditText) findViewById(R.id.name);
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		call_name.setVisibility(spref.getBoolean("name_on", false)?View.VISIBLE:View.GONE);
		log_on = spref.getBoolean("log_on", true);
	}
}
