package edu.rosehulman.android.directory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

/**
 * Allows tabs to be managed easier by action bars
 *
 * @param <T> The fragment to use for this tab
 */
public class TabListener<T extends Fragment> implements ActionBar.TabListener {
	private final FragmentActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    private final Bundle mArgs;
    private Fragment mFragment;

    public TabListener(FragmentActivity activity, String tag, Class<T> clz) {
        this(activity, tag, clz, null);
    }

    public TabListener(FragmentActivity activity, String tag, Class<T> clz, Bundle args) {
    	mActivity = activity;
        mTag = tag;
        mClass = clz;
        mArgs = args;

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
        if (mFragment != null && !mFragment.isDetached()) {
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            ft.detach(mFragment);
            ft.commit();
        }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	if (mFragment == null) {
            mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
            ft.add(android.R.id.content, mFragment, mTag);
        } else {
            ft.attach(mFragment);
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        if (mFragment != null) {
            ft.detach(mFragment);
        }
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
}