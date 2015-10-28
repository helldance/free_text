/**
 * Copyright @2010 Rayy.
 * Pref.java
 */
package com.rayy.android.fake;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @author rayy
 * @date Apr 8, 2011
 */
public class Setting extends PreferenceActivity {
    SharedPreferences spref;
    Editor editor;
    View pref_view;
    boolean pass_on;
    PreferenceManager pm;
    Preference pref;
    CheckBoxPreference cbp;
    Dialog diag, diag1, diag2;
    EditText pass1_text, pass2_text, old_pass_text;
    String pass1, pass2, old_pass, stored_pass;
    View v1, v2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        bar.setTitle("Setting");
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pm = this.getPreferenceManager();
        pref = pm.findPreference("pass");
        spref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = spref.edit();
        // stored_pass = spref.getString("pass", "");
        // Log.v("Pass", stored_pass);
        //Fake.CONFIG_CHANGE = true;

        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                stored_pass = spref.getString("pass", "");
                Log.v("Pass", stored_pass);

                if (stored_pass.equalsIgnoreCase("PASS_NOT_SET") || stored_pass.equalsIgnoreCase(""))
                    showChangePass();
                else
                    showCheckPass();
                return true;
            }

        });

        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                // TODO Auto-generated method stub
                return false;
            }

        });
    }

    protected void showChangePass() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v1 = inflater.inflate(R.layout.pass, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.hint_pass1));
        builder.setView(v1);
        pass1_text = (EditText) v1.findViewById(R.id.pass1);
        pass2_text = (EditText) v1.findViewById(R.id.pass2);

        // pass1 = pass1_text.getText().toString();
        // pass2 = pass2_text.getText().toString();

        builder.setPositiveButton(getString(R.string.ok1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                pass1 = pass1_text.getText().toString();
                pass2 = pass2_text.getText().toString();
                if (pass2 != "" && pass1.equalsIgnoreCase(pass2)) {
                    editor.putString("pass", pass2);
                    editor.commit();

                    dialog.dismiss();
                } else {
                    ViewGroup vg = (ViewGroup) v1.getParent();
                    vg.removeView(v1);
                    Toast.makeText(Setting.this, getString(R.string.pass_not_match), Toast.LENGTH_SHORT).show();
                    builder.create().show();
                }
            }

            ;
        });
        builder.create().show();
        //diag1.show();
    }

    protected void showCheckPass() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v2 = inflater.inflate(R.layout.check_pass, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.hint_pass1));
        builder.setView(v2);
        old_pass_text = (EditText) v2.findViewById(R.id.check_pass);
        // old_pass = old_pass_text.getText().toString();

        builder.setPositiveButton(getString(R.string.ok1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                old_pass = old_pass_text.getText().toString();
                if (old_pass.equalsIgnoreCase(stored_pass)) {
                    showChangePass();
                    dialog.dismiss();
                } else {
                    ViewGroup vg = (ViewGroup) v2.getParent();
                    vg.removeView(v2);
                    builder.create().show();
                    Toast.makeText(Setting.this, getString(R.string.auth_fail), Toast.LENGTH_SHORT).show();
                }
            }

            ;
        });
        builder.create().show();
    }
}
