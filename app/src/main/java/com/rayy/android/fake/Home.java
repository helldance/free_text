package com.rayy.android.fake;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.rayy.android.fake.adapter.PagerAdapter;
import com.rayy.android.fake.fragment.CallFragment;
import com.rayy.android.fake.fragment.SmsFragment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

public class Home extends ActionBarActivity implements ActionBar.OnNavigationListener {
    /**
     * Called when the activity is first created.
     */

    ActionBar ab;
    ViewPager pgr;
    Fragment backupSmsFragment;
    private static final String TAG = "Home";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String currentDefaultSmsApp;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String theme_choice = pref.getString("theme", "Holo Dark");
		setTheme(theme_choice.equalsIgnoreCase("Theme Dark")? R.style.Theme_Sherlock: R.style.Theme_Sherlock_Light);*/
        super.onCreate(savedInstanceState);
        forceTabs();

        setContentView(R.layout.vp_main);

        pref = PreferenceManager.getDefaultSharedPreferences(Home.this);

        ab = getSupportActionBar();

        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.setDisplayShowTitleEnabled(true);

        pgr = (ViewPager) findViewById(R.id.pager);

        FragmentManager fm = getSupportFragmentManager();

        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ab.setSelectedNavigationItem(position);
            }

        };

        pgr.setOnPageChangeListener(pageChangeListener);

        List<Fragment> fragList = new Vector<Fragment>();
        fragList.add(Fragment.instantiate(this, SmsFragment.class.getName()));
        fragList.add(Fragment.instantiate(this, CallFragment.class.getName()));

        /** Creating an instance of FragmentPagerAdapter */
        PagerAdapter pgrAdapter = new PagerAdapter(fm, fragList);

        pgr.setAdapter(pgrAdapter);

        /** Defining tab listener */
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                pgr.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }
        };

        String tab_title_sms = getResources().getString(R.string.msg);
        String tab_title_call = getResources().getString(R.string.call);

        /** Creating Android Tab */
        ActionBar.Tab tab_sms = ab.newTab().setText(tab_title_sms).setTabListener(tabListener);
        ActionBar.Tab tab_call = ab.newTab().setText(tab_title_call).setTabListener(tabListener);

        ab.addTab(tab_sms);
        ab.addTab(tab_call);

        // set StrictMode policy
        StrictMode.setVmPolicy(new VmPolicy.Builder().setClassInstanceLimit(Home.class, 5).build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mn_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.setting:
                openSetting();
                return true;
            case R.id.info:
                openInfo();
                return true;
            case R.id.fake_list:
                //openFakeLog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openSetting() {
        Intent intent = new Intent(this, Setting.class);
        startActivity(intent);
    }

    public void openInfo() {
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onStop() {
        super.onStop();
        Log.i("", "stop");

        editor = pref.edit();
        editor.remove("exit").apply();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onBackPressed() {
        Log.i("", "onBack");

        pref.edit().putBoolean("exit", true).commit();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String newDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            currentDefaultSmsApp = pref.getString("default", "");

            Log.i("", newDefaultSmsApp + ", " + currentDefaultSmsApp);

            if (newDefaultSmsApp != null && !newDefaultSmsApp.equals(currentDefaultSmsApp)) {
                boolean revert_diag_show = pref.getBoolean("revert_diag_show", true);

                if (revert_diag_show) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);

                    //builder.setTitle(R.string.diag_set_default_sms);
                    //builder.setMessage(R.string.msg_revert_default_sms);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_revert_default_sms);
                    builder.setView(diag_view);

                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("", "Revert default Sms");

                            System.gc();

                            CheckBox cb = (CheckBox) diag_view.findViewById(R.id.cb_no_show);

                            if (cb.isChecked()) {
                                editor = pref.edit();
                                editor.putBoolean("revert_diag_show", false).apply();
                            }

                            startChangeSmsDiagActivity(currentDefaultSmsApp);
                        }
                    });

                    builder.create().show();
                } else {
                    startChangeSmsDiagActivity(currentDefaultSmsApp);
                }
            } else {
                Log.i(TAG, "Current app is not set the default Sms app");

                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onResume() {
        super.onResume();
        Log.i("", "resume, " + android.os.Build.VERSION.SDK_INT);

        if (pref.getBoolean("exit", false)) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            final String myPackageName = "com.rayy.android.fake";

            currentDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);

            if (currentDefaultSmsApp != null && !currentDefaultSmsApp.equals(myPackageName)) {
                // App is not default.
                // Show the "not currently set as the default SMS app" interface

                editor = pref.edit();

                editor.putString("default", currentDefaultSmsApp);
                editor.commit();

                boolean change_diag_show = pref.getBoolean("change_diag_show", true);

                if (change_diag_show) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);

                    LayoutInflater inflater = this.getLayoutInflater();
                    final View diag_view = inflater.inflate(R.layout.diag_sms_hint, null);

                    //builder.setTitle(R.string.diag_set_default_sms);
                    //builder.setMessage(R.string.msg_set_default_sms);
                    TextView tv = (TextView) diag_view.findViewById(R.id.tv_sms_msg);
                    tv.setText(R.string.msg_set_default_sms);
                    builder.setView(diag_view);

                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("", "Setting default Sms");

                            CheckBox cb = (CheckBox) diag_view.findViewById(R.id.cb_no_show);

                            if (cb.isChecked()) {
                                editor.putBoolean("change_diag_show", false).apply();
                            }

                            startChangeSmsDiagActivity(myPackageName);
                        }
                    });

                    builder.create().show();
                } else {
                    startChangeSmsDiagActivity(myPackageName);
                }
            } else {
                Log.i(TAG, "Current app is set the default Sms app");
            }
        }
    }

    public void forceTabs() {
        try {
            final ActionBar actionBar = getSupportActionBar();
            final Method setHasEmbeddedTabsMethod = actionBar.getClass()
                    .getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
            setHasEmbeddedTabsMethod.setAccessible(true);
            setHasEmbeddedTabsMethod.invoke(actionBar, true);
        }
        catch(final Exception e) {
            // Handle issues as needed: log, warn user, fallback etc
            // This error is safe to ignore, standard tabs will appear.
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startChangeSmsDiagActivity(String packageToChange) {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageToChange);

        startActivity(intent);
    }
}