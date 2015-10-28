/**
 *
 */
package com.rayy.android.fake;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.rayy.android.fake.adapter.PagerAdapter;
import com.rayy.android.fake.fragment.CallFragment;
import com.rayy.android.fake.fragment.LogFragment;
import com.rayy.android.fake.fragment.SmsFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * @author RAY
 */
public class FakeViewPager extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
    private PagerAdapter mPagerAdapter;
    private static final String tag = "FakeViewPager";

    /**
     * @author mwho
     *         Maintains extrinsic info of a tab's construct
     */
    private class TabInfo {
        private String tag;
        private Class<?> clss;
        private Bundle args;
        private Fragment fragment;

        TabInfo(String tag, Class<?> clazz, Bundle args) {
            this.tag = tag;
            this.clss = clazz;
            this.args = args;
        }

    }

    /**
     * A simple factory that returns dummy views to the Tabhost
     *
     * @author mwho
     */
    class TabFactory implements TabContentFactory {

        private final Context mContext;

        /**
         * @param context
         */
        public TabFactory(Context context) {
            mContext = context;
        }

        /**
         * (non-Javadoc)
         *
         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
         */
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }

    }

    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        // Inflate the layout
        setContentView(R.layout.fake_viewpager);
        // Initialise the TabHost
        this.initialiseTabHost(savedInstanceState);
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
        // Intialise ViewPager
        this.intialiseViewPager();
    }

    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }

    /**
     * Initialise ViewPager
     */
    private void intialiseViewPager() {

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, SmsFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, CallFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, LogFragment.class.getName()));

        this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);

        this.mViewPager = (ViewPager) super.findViewById(R.id.tabviewpager);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mViewPager.setOnPageChangeListener(this);
    }

    /**
     * Initialise the Tab Host
     */
    private void initialiseTabHost(Bundle args) {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        TabInfo tabInfo = null;
        View tabView;

        String msg = getString(R.string.msg);
        String call = getString(R.string.call);
        String log = getString(R.string.log);

        tabView = LayoutInflater.from(this).inflate(R.layout.new_tab, null);
        ((TextView) tabView.findViewById(R.id.tab_text)).setText(msg);
        AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(msg).setIndicator(tabView), (tabInfo = new TabInfo(msg, SmsFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        tabView = LayoutInflater.from(this).inflate(R.layout.new_tab, null);
        ((TextView) tabView.findViewById(R.id.tab_text)).setText(call);
        AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(call).setIndicator(tabView), (tabInfo = new TabInfo(call, CallFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        tabView = LayoutInflater.from(this).inflate(R.layout.new_tab, null);
        ((TextView) tabView.findViewById(R.id.tab_text)).setText(log);
        AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(log).setIndicator(tabView), (tabInfo = new TabInfo(log, LogFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        // Default to first tab
        //this.onTabChanged(msg);
        mTabHost.setOnTabChangedListener(this);
    }

    /**
     * Add Tab content to the Tabhost
     *
     * @param activity
     * @param tabHost
     * @param tabSpec
     */
    private static void AddTab(FakeViewPager activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }

    public void openSetting(View v) {
        Log.v(tag, "openSetting");
        Intent intent = new Intent(this, Setting.class);
        startActivity(intent);
    }

    public void openHelp(View v) {
        Log.v(tag, "openHelp");
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    /**
     * (non-Javadoc)
     *
     * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
     */
    public void onTabChanged(String tag) {
        //TabInfo newTab = this.mapTabInfo.get(tag);
        int pos = this.mTabHost.getCurrentTab();
        this.mViewPager.setCurrentItem(pos);
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)
     */
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
     */
    public void onPageSelected(int position) {
        // TODO Auto-generated method stub
        this.mTabHost.setCurrentTab(position);
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
     */
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub

    }
}
