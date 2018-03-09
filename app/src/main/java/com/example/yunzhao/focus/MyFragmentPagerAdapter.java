package com.example.yunzhao.focus;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yunzhao on 2018/3/9.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private String[] tableTitle = new String[] {"今日待办", "收件箱"};
    private Context mContext;
    private List<Fragment> mFragmentTab;
    private TodoFragment mTodoFragment;
    private InboxFragment mInboxFragment;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        initFragmentTab();
    }


    @Override
    public Fragment getItem(int position) {
        return mFragmentTab.get(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tableTitle[position];
    }

    private void initFragmentTab() {
        mTodoFragment = new TodoFragment();
        mInboxFragment = new InboxFragment();
        mFragmentTab = new ArrayList<Fragment>();
        mFragmentTab.add(mTodoFragment);
        mFragmentTab.add(mInboxFragment);
    }

}
