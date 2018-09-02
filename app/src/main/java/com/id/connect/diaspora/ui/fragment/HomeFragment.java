package com.id.connect.diaspora.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.CustomViewPager;
import com.pixplicity.fontview.FontAppCompatTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    @BindView(R.id.tab_viewpager)
    CustomViewPager customViewPager;

    @BindView(R.id.newTask)
    FontAppCompatTextView newTask;

    @BindView(R.id.line_1)
    View line1;

    @BindView(R.id.onProgressTask)
    FontAppCompatTextView onProgressTask;

    @BindView(R.id.line_2)
    View line2;

    @BindView(R.id.img_feed)
    ImageView imgFeed;
    @BindView(R.id.img_jobs)
    ImageView imgJobs;

    Context mContext;
    private View mView;
    private Unbinder mUnbinder;
    private int mParamPage;
    private static final String PAGE = "page";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_home, container, false);
            mUnbinder = ButterKnife.bind(this, mView);
            setupViewPager(customViewPager, mParamPage);
        }

        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParamPage = getArguments().getInt(PAGE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void setupViewPager(CustomViewPager viewPager, int page) {
        //mTaskListPresenter = new TaskListPresenter(this);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        sectionsPagerAdapter.addFragment(new FeedFragment(), getResources().getString(R.string.tab_label_feed));
        sectionsPagerAdapter.addFragment(new JobFragment(), getResources().getString(R.string.tab_label_jobs));

        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateTabTextView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(page);
    }

    void updateTabTextView(int position) {
        if (position == 0) {
            line1.setVisibility(View.VISIBLE);
            line2.setVisibility(View.GONE);
            imgFeed.setColorFilter(getResources().getColor(R.color.linkColor));
            imgJobs.setColorFilter(getResources().getColor(R.color.grey_20));
            newTask.setTextColor(this.getResources().getColor(R.color.black));
            onProgressTask.setTextColor(this.getResources().getColor(R.color.grey_20));

        } else if (position == 1) {
            line1.setVisibility(View.GONE);
            line2.setVisibility(View.VISIBLE);
            imgJobs.setColorFilter(getResources().getColor(R.color.linkColor));
            imgFeed.setColorFilter(getResources().getColor(R.color.grey_20));
            newTask.setTextColor(this.getResources().getColor(R.color.grey_20));
            onProgressTask.setTextColor(this.getResources().getColor(R.color.black));
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
