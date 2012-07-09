
package com.example.animcalc;

import java.math.BigDecimal;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends FragmentActivity {
    // 電卓画面
    private CalcFragment mCalcFragment;
    // 履歴画面
    private HistoryFragment mHistoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // レイアウトからTabHostを取得
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        // セットアップ！
        tabHost.setup();
        // レイアウトからViewPagerを取得
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        // TabsAdapterを作って
        TabsAdapter adapter = new TabsAdapter(this, tabHost, viewPager);
        // 電卓画面：CalcFragmentを作って
        mCalcFragment = (CalcFragment) Fragment.instantiate(this,
                CalcFragment.class.getName(), null);
        // 履歴画面：HistoryFragmentを作って
        mHistoryFragment = (HistoryFragment) Fragment.instantiate(this,
                HistoryFragment.class.getName(), null);
        // TabsAdapterに追加！
        adapter.addTab("電卓", mCalcFragment);
        adapter.addTab("履歴", mHistoryFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                // 設定画面に移動!
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return false;
    }

    private class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener,
            ViewPager.OnPageChangeListener {
        private Context mContext;
        private TabHost mTabHost;
        private ViewPager mViewPager;
        ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

        // TabContentFactoryというタブのコンテンツを作るクラスが必要になるが
        // 今回は何も表示しないのでダミーのコンテンツを作るクラスを定義する
        private class Dummy implements TabHost.TabContentFactory {
            private final Context mContext;

            public Dummy(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                // サイズ0のViewを返す
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity,
                TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            // タブが選ばれて変化した時のリスナーをセット
            mTabHost.setOnTabChangedListener(this);
            // ViewPagerに表示するAdapterをセット
            mViewPager.setAdapter(this);
            // ViewPagerでページが変化した時にリスナーをセット
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(String title, Fragment fragment) {
            // TabSpecを作って
            TabSpec tabSpec = mTabHost.newTabSpec(title);
            // 表示タイトルをセット
            tabSpec.setIndicator(title);
            // コンテンツをセット（ダミー）
            tabSpec.setContent(new Dummy(mContext));
            // タブを追加
            mTabHost.addTab(tabSpec);
            // フラグメントをリストに追加
            mFragments.add(fragment);
        }

        @Override
        public int getCount() {
            // Adapterのデータの数を返すメソッドです。
            // 今回はフラグメントのリストの数を返します。
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            // 指定された位置（何番目のタブか）に存在する
            // フラグメントを返すメソッドです。
            return mFragments.get(position);
        }

        @Override
        public void onTabChanged(String tabId) {
            // タブが選ばれて変化した場合は何番目のタブか取得して
            int position = mTabHost.getCurrentTab();
            // ViewPagerの現在位置をセットする
            mViewPager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position,
                float positionOffset,
                int positionOffsetPixels) {
            // 今回は何もしない
        }

        @Override
        public void onPageSelected(int position) {
            // ViewPagerがスワイプされた時に呼び出されます。
            // タブの現在位置をセットする
            mTabHost.setCurrentTab(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // 今回は何もしない
        }
    }

    public void addHistory(String history) {
        // HistoryFragmentクラスのaddHistory()メソッドを使って
        // 履歴の文字列を伝える。
        mHistoryFragment.addHistory(history);
    }

    public void setCalcValue(BigDecimal value) {
        // CalcFragmentクラスのsetCalcValue()メソッドを使って
        // 値をセットする
        mCalcFragment.setCalcValue(value);
        // TabHostを取得して
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        // タブを0番目にセットして電卓画面に移動する
        tabHost.setCurrentTab(0);
    }
}
