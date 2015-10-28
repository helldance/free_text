/**
 * Copyright @2010 Rayy.
 * Help.java
 */
package com.rayy.android.fake;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * @author rayy
 * @date Apr 10, 2011
 */
public class Help extends ActionBarActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.info);

		TextView tv_help = (TextView) findViewById(R.id.help_text);
		tv_help.setTextSize(15f);
		tv_help.setText(Html.fromHtml(getString(R.string.help)));

		TextView tv_note = (TextView) findViewById(R.id.note_text);
		tv_note.setTextSize(15f);
		tv_note.setText(Html.fromHtml(getString(R.string.note)));

		TextView tv_about = (TextView) findViewById(R.id.help_abt_text);
		tv_about.setTextSize(15f);
		tv_about.setMovementMethod(LinkMovementMethod.getInstance());
		tv_about.setText(Html.fromHtml(getString(R.string.about)));
	}
}
