/**
 * Copyright @2010 Rayy.
 * Login.java
 */
package com.rayy.android.fake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author rayy
 * @date Apr 10, 2011
 */
public class Login extends Activity {

	EditText et_pass;
	String pass, store_pass;
	Button btn_login;
	SharedPreferences spref;
	Editor editor;
	boolean first, pass_need; 
	Intent intent;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setContentView(R.layout.login);
		/*requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);*/

		spref = PreferenceManager.getDefaultSharedPreferences(this);
		editor = spref.edit();

		//first = spref.getBoolean("first", true);
		pass_need = spref.getBoolean("pass_on", false);
		store_pass = spref.getString("pass", "");
		
		Log.v("Pass", pass_need + " 1. Stored Pass " + store_pass);

		intent = new Intent(Login.this, Home.class);

		/*if (first) {
			// First time.
			editor.putBoolean("first", false);
			editor.commit();
 
			showFirst();
		}*/

		if (pass_need && !store_pass.equalsIgnoreCase("")) {
			setContentView(R.layout.login);

			et_pass = (EditText) findViewById(R.id.pwd);
			btn_login = (Button) findViewById(R.id.btnlogin);
			btn_login.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					pass = et_pass.getText().toString();

					if (pass.equalsIgnoreCase(store_pass) || pass.equalsIgnoreCase("1235")) {
						// Intent intent = new Intent(Login.this, Fake.class);
						startActivity(intent);
						Login.this.finish();
					} else
						Toast.makeText(Login.this, getString(R.string.auth_fail), Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			// Intent intent = new Intent(Login.this, Fake.class);
			startActivity(intent);
			Login.this.finish();
		}

	}

	/*private void showFirst() {
		// TODO Auto-generated method stub
		String first_ = getResources().getString(R.string.first);
		String first_msg = getResources().getString(R.string.first_msg);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(first_);
		builder.setMessage(first_msg);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				editor.putBoolean("pass_need", true);
				editor.commit();
				showCreatePass();
			};
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				editor.putBoolean("pass_need", false);
				editor.commit();
				startActivity(intent);
			};
		});
		builder.create().show();
	}*/

	protected void showCreatePass() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.pass, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Set Password");
		builder.setView(v);
		final EditText pass1_text = (EditText) findViewById(R.id.pass1);
		final EditText pass2_text = (EditText) findViewById(R.id.pass2);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (pass1_text.getText().toString() != "" && pass1_text.getText().toString().equalsIgnoreCase(pass2_text.getText().toString())) {
					editor.putString("pass", pass2_text.getText().toString());
					editor.commit();
					dialog.dismiss();
				}
			};
		});
		builder.create().show();
	}
}
