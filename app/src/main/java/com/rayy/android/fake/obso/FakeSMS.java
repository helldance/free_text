package com.rayy.android.fake.obso;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rayy.android.fake.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FakeSMS extends Activity {
	/** Called when the activity is first created. */

	private EditText from, time, body, date;
	private RadioButton read, unread, in, out, fail, success;
	private Button send;
	private ImageButton pick, p_date, p_time;
	private int isRead = -1, isIn = -1, isFail = 0;
	private final int PICK_CONTACT = 2, DATE_DIAG = 0, TIME_DIAG = 1;
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
	private boolean log_on = true;
	private SharedPreferences spref;
	private String thread_on_modify;
	private long thread_date_new;
	private String dest;
	private final String tag = "FakeSMS";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sms);

		spref = PreferenceManager.getDefaultSharedPreferences(this);

		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMin = c.get(Calendar.MINUTE);

		from = (EditText) findViewById(R.id.from);
		time = (EditText) findViewById(R.id.time);
		date = (EditText) findViewById(R.id.date);
		body = (EditText) findViewById(R.id.body);

		final RadioGroup rg = (RadioGroup) findViewById(R.id.radio);
		read = (RadioButton) findViewById(R.id.read);
		unread = (RadioButton) findViewById(R.id.unread);

		final RadioGroup rg_out = (RadioGroup) findViewById(R.id.radio_fail);
		fail = (RadioButton) findViewById(R.id.out_f);
		success = (RadioButton) findViewById(R.id.out_s);

		in = (RadioButton) findViewById(R.id.in);
		in.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					rg.setVisibility(View.VISIBLE);
					rg_out.setVisibility(View.GONE);
				}
			}

		});
		/*
		 * if (in.isChecked()){ }
		 */
		out = (RadioButton) findViewById(R.id.out);
		out.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					rg.setVisibility(View.GONE);
					rg_out.setVisibility(View.VISIBLE);
				}
			}

		});

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

		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendMessage();
			}
		});
	}

	protected void pickContact() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);
	}

	protected void onActivityResult(int requstCode, int resultCode, Intent data) {

		if (requstCode == PICK_CONTACT) {
			if (resultCode == RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				ContentResolver cr = getContentResolver();

				if (c.moveToFirst()) {
					String id = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String name = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

					if (Integer
							.parseInt(c.getString(c
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor pCur = cr
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?", new String[] { id },
										null);

						Log.v("TAG", "Size: " + pCur.getCount());
						numList.clear();

						while (pCur.moveToNext()) {
							for (int i = 0; i < pCur.getColumnCount(); i++) {
								if (pCur.getColumnName(i).equalsIgnoreCase(
										"data1")) {
									numList.add((pCur.getString(i).replace("-",
											""))); // ???
									Log.v("TAG", "Added: " + pCur.getString(i));
								}
							}
						}

						if (numList.size() == 1) {
							from.setText("<" + name + ">-" + numList.get(0));
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

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.title_pick_num));
		builder.setSingleChoiceItems(temp, -1,
				new DialogInterface.OnClickListener() {
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
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
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
			};
			DatePickerDialog dpDiag = new DatePickerDialog(this,
					mDateSetListener, mYear, mMonth, mDay);
			return dpDiag;

		case TIME_DIAG:
			TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hour, int minute) {
					mHour = hour;
					mMin = minute;
					String mH, mM;
					mH = mHour < 10 ? "0" + hour : "" + hour;
					mM = mMin < 10 ? "0" + mMin : "" + mMin;
					time.setText(new StringBuilder().append(mH).append(":")
							.append(mM));
				}
			};
			TimePickerDialog tpDiag = new TimePickerDialog(this,
					mTimeSetListener, mHour, mMin, true);
			return tpDiag;
		}

		return null;
	}

	protected void sendMessage() {
		// TODO Auto-generated method stub

		String temp = from.getText().toString();
		if (!temp.equalsIgnoreCase("")) {
			dest = temp.substring(temp.indexOf("-") + 1);
			Log.i(tag, dest);
		}

		Uri uri = Uri.parse("content://sms");

		/*
		 * c.moveToFirst(); for (int i = 0; i < c.getColumnCount(); i ++){
		 * Log.i(tag, c.getColumnName(i) +" " + c.getString(i)); }
		 */

		ContentValues cv = new ContentValues();

		cv.put("body", body.getText().toString());
		cv.put("address", dest);
		if (read.isChecked())
			isRead = 1;
		if (unread.isChecked()) {
			isRead = 0;
		}
		if (in.isChecked()) {
			isIn = 1;
			cv.put("protocol", 0);
		}
		if (out.isChecked()) {
			// Added on 08/03/2011 for Fail Send function
			if (fail.isChecked())
				isIn = 5;
			else
				isIn = 2;
		}

		cv.put("read", isRead);
		cv.put("type", isIn);

		Date calldate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

		if (time.getText().toString().equalsIgnoreCase("")
				|| date.getText().toString().equalsIgnoreCase("")) {
			Toast.makeText(FakeSMS.this,
					getResources().getString(R.string.tst_null_time),
					Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			calldate = sdf.parse(time.getText().toString() + " "
					+ date.getText().toString());
			thread_date_new = calldate.getTime();
			cv.put("date", thread_date_new);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		diag2 = new Dialog(FakeSMS.this, R.style.dialog);
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
		
		//TODO: Get thread_on_modify here.
		Cursor c = getContentResolver().query(uri,
				new String[] { "thread_id" }, "address = ?",
				new String[] { dest }, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			thread_on_modify = c.getString(0);

			Log.i(tag, thread_on_modify + " will refresh");
		}

		// RefreshConversationListbyInsertingTempMsg
		if (thread_on_modify != null) {
			ContentValues tcv = new ContentValues();
			tcv.put("address", "");
			tcv.put("date", Long.valueOf("1002265663222"));
			tcv.put("type", isIn);
			tcv.put("read", isRead);
			tcv.put("thread_id", thread_on_modify);
			tcv.put("body", "some msg");
			getContentResolver().delete(getContentResolver().insert(uri, tcv),
					null, null);
			
			Log.i(tag, "refreshed list");
		}
		/*
		 * queryThread();
		 * 
		 * Log.i(tag, thread_date_new + " " + thread_date);
		 * 
		 * // update thread date. if (thread_update_required) { Log.i(tag,
		 * "thread_update_required");
		 * 
		 * Uri uri_thread =
		 * Uri.withAppendedPath(Uri.parse("content://mms-sms/"),
		 * "conversations"); ContentValues cv_thread = new ContentValues();
		 * 
		 * if (thread_date_new > new Long(thread_date)) { cv_thread.put("date",
		 * thread_date_new); Log.i(tag, "put new date"); } else {
		 * cv_thread.put("date", thread_date); Log.i(tag, "put old date"); }
		 * 
		 * Log.i(tag, "update thread count: " +
		 * getContentResolver().update(uri_thread, cv_thread, "thread_id = ?",
		 * new String[] { thread_on_modify })); Log.i(tag,
		 * "thread_date roll back complete"); }
		 */

		if (log_on) {
			setupDB();

			if (calldate != null)
				addLog(0, from.getText().toString(), calldate.getTime() + "",
						body.getText().toString());
		}

	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int total = msg.getData().getInt("total");
			Log.v("TAG", "2. Handler message get.." + total);
			if (total == 999) {
				diag2.dismiss();
				Toast.makeText(FakeSMS.this,
						getResources().getString(R.string.tst_msg_succ),
						Toast.LENGTH_SHORT).show();
				Log.v("TAG", "2. Handler message finished..");
			}
		}
	};

	private void setupDB() {
		try {
			// Open database for transcation or create if not exists.
			// mDB = SQLiteDatabase.openOrCreateDatabase("sdcard/mDB.db", curf);
			// User context to create database. Default path under app folder.
			mDB = FakeSMS.this.openOrCreateDatabase("mDB.db",
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
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			mDB.close();
		}
	}

	public String matchContact(String num) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(num));
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME },
				null, null, null);

		while (c.moveToNext()) {
			return c.getString(0);
		}

		return num;
	}

	protected void getListProviders() {
		for (PackageInfo pack : getPackageManager().getInstalledPackages(
				PackageManager.GET_PROVIDERS)) {
			ProviderInfo[] providers = pack.providers;
			if (providers != null) {
				for (ProviderInfo provider : providers) {
					Log.d("Example", "provider: " + provider.authority);
				}
			}
		}
	}
	
	public void onPause (){
		super.onPause();
	}

	public void onResume() {
		super.onResume();

		log_on = spref.getBoolean("log_on", true);
	}
}