package com.example.teamproject;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ReviewPageAdapter extends FragmentPagerAdapter {
    private String tabTitles[] = new String[] { "View", "Comments" };
    private Context context;

    public ReviewPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ViewDocFragment view_tab = new ViewDocFragment();
                return view_tab;
            case 1:
                CommentsFragment comments_tab = new CommentsFragment();
                return comments_tab;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
