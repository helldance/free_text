/**
 * 
 */
package com.rayy.android.fake.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.rayy.android.fake.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author RAY
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LogFragment extends Fragment {
	private Context mContext;
	private View logView;
	private ListView lv;
	private SQLiteDatabase mDB;
	private CursorFactory curf;
	private Cursor cur;
	private final String TBL_CHECK = "select * from sqlite_master where type = 'table' and name = 'TBL_FAKE_LOG'";
	private final String C_TABLE = "create table TBL_FAKE_LOG (_id integer primary key autoincrement, type integer, addr text, time text, content text)";
	private final String Q_DATA = "select * from TBL_FAKE_LOG order by time desc";
	private String[] columns = { "type", "addr", "time" };
	private int[] layouts = { R.id.type, R.id.number, R.id.timestamp };
	private ArrayList<HashMap<String, Object>> logs;
	private HashMap<String, Object> log;
	private HashMap<String, Object> sel_log;
	private Dialog diag2;
	private int DONE = 0;  
	private SimpleAdapter sa = null;
	private boolean FIRST = true, log_delete_success = false;
	private static String tag = "LogFragment";
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		mContext = this.getActivity();
		
		if (container == null)
			return null;
		
		logView = inflater.inflate(R.layout.log, container, false);
		
		/*diag2 = new Dialog(mContext, R.style.dialog);
		diag2.setContentView(R.layout.diag_load);
		diag2.show();*/

		lv = (ListView) logView.findViewById(R.id.lv_log);

		//new PrepareTask().execute();

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int id, long position) {
				// TODO Auto-generated method stub
				sel_log = logs.get(id);

				ImageView iv = (ImageView) arg1.findViewById(R.id.type);
				iv.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (deleteLog()){
							logs.remove(id);
							lv.setAdapter(sa);
						}
					}

				});

				/*if (log_delete_success) {
					logs.remove(id);
					lv.setAdapter(sa);
				}*/

				return true;
			}

		});
		
		return logView;
	}
	
	private class PrepareTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			setupDB();
			loadHistory();

			return null;
		}

		protected void onPostExecute(Void result) {
			/*if (diag2.isShowing())
				diag2.dismiss();*/

			lv.setAdapter(sa);
		}
	}

	private void setupDB() {
		try {
			mDB = mContext.openOrCreateDatabase("mDB.db", Context.MODE_PRIVATE, curf);
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
			// mDB.close();
		}
		Log.v("Database", "DB Ready");
	}

	protected boolean loadHistory() {

		logs = new ArrayList<HashMap<String, Object>>();
		
		if (mDB.isOpen()) {
			cur = mDB.rawQuery(Q_DATA, null);

			if (cur.getCount() > 0) {

				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

				// Parse date format.
				//cur.moveToFirst();
				while (cur.moveToNext()) {
					log = new HashMap<String, Object>();

					for (int i = 0; i < cur.getColumnCount() - 1; i++) {
						if (cur.getColumnName(i).equalsIgnoreCase("addr")) {
							String addr = cur.getString(i);
							// if (!addr.equalsIgnoreCase("")) {
							// addr = matchContact(addr);
							// }
							log.put("addr", addr);
						} else if (cur.getColumnName(i)
								.equalsIgnoreCase("time")) {
							Date d = new Date(Long.parseLong(cur.getString(i)));
							log.put("time", sdf.format(d));
							// Log.v("TAG", sdf.format(d));
						} else if (cur.getColumnName(i)
								.equalsIgnoreCase("type")) {
							int type = (Integer.parseInt(cur.getString(i)) == 1) ? R.drawable.call_light
									: R.drawable.msg_light;
							log.put("type", type);
						} else {
							log.put(cur.getColumnName(i), cur.getString(i));
						}
					}

					logs.add(log);
				}
			}
		}

		sa = new SimpleAdapter(mContext, logs, R.layout.log_list, columns, layouts);

		if (mDB.isOpen())
			mDB.close();

		return true;
	}

	final Handler load_handler = new Handler() {
		public void handleMessage(Message msg) {

			if (DONE == 1) {
				diag2.dismiss();

				loadHistory();
			}
		}
	};

	public String matchContact(String num) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
		ContentResolver cr = mContext.getContentResolver();
		Cursor c = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);

		while (c.moveToNext()) {
			return c.getString(0);
		}

		return num;
	}

	private boolean deleteLog() {
		if (sel_log != null) {
			int sel_type = 0;
			//if (Integer.parseInt(sel_log.get("type").toString()) == R.drawable.call)
			//	sel_type = 1;

			String DEL_ENTRY = "delete from TBL_FAKE_LOG where addr = '" + sel_log.get("addr").toString() + "' and type =  " + sel_type + "";
			Log.d("TAG", DEL_ENTRY);

			try {
				if (!mDB.isOpen())
					mDB = mContext.openOrCreateDatabase("mDB.db", Context.MODE_PRIVATE, curf);
				mDB.beginTransaction();
				mDB.execSQL(DEL_ENTRY);
				mDB.setTransactionSuccessful();
				mDB.endTransaction();
				Log.v("TAG", "Entry is deleted");
			} catch (SQLException se) {
				se.printStackTrace();
			} finally {
				SQLiteDatabase.releaseMemory();
				mDB.close();
			}

			log_delete_success = true;
			return true;
		}
		return false;
	}

	private void deleteAll() {
		// TODO Auto-generated method stub

		String DEL_ENTRY = "delete from TBL_FAKE_LOG";
		Log.d(tag, DEL_ENTRY);

		try {
			if (!mDB.isOpen())
				mDB = mContext.openOrCreateDatabase("mDB.db", Context.MODE_PRIVATE, curf);
			mDB.beginTransaction();
			mDB.execSQL(DEL_ENTRY);
			mDB.setTransactionSuccessful();
			mDB.endTransaction();
			Log.v(tag, "Entry is deleted");
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			mDB.close();

			lv.setAdapter(null);

			//Toast.makeText(this, "All records are removed", Toast.LENGTH_SHORT).show();
		}
	}

	public void onPause (){
		super.onPause();
		
		Log.i(tag, "pause");
		
		if (mDB.isOpen())
			mDB.close();
	}
	
	public void onResume() {
		super.onResume();
		
		Log.i(tag, "resume");

		//if (null == sa) {
			new PrepareTask().execute();
		//}

	}

	public void onStop() {
		super.onStop();
		if (mDB.isOpen())
			mDB.close();
	}
}
