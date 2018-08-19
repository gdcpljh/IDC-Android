package com.example.helmi.diaspora;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.example.helmi.diaspora.databinding.ActivityCenterFabBinding;
import com.example.helmi.diaspora.ui.fragment.HomeFragment;

public class MainPage extends AppCompatActivity {

    private static final String TAG = MainPage.class.getSimpleName();
    private ActivityCenterFabBinding bind;
    private VpAdapter adapter;

    private Context mContext = MainPage.this;
    //firebase


    // collections
    private List<Fragment> fragments;// used for ViewPager adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_with_view_pager);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_center_fab);

        Log.d(TAG, "onCreate: starting.");

        initData();
        initView();
        initEvent();
    }


    /**
     * create fragments
     */
    private void initData() {
        fragments = new ArrayList<>(2);

        // create music fragment and add it
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        homeFragment.setArguments(bundle);

        // create backup fragment and add it
        HomeFragment searchFragment = new HomeFragment();
        bundle = new Bundle();
        searchFragment.setArguments(bundle);

        // create friends fragment and add it

        // add to fragments for adapter
        fragments.add(homeFragment);
        fragments.add(searchFragment);
    }


    /**
     * change BottomNavigationViewEx style
     */
    private void initView() {
        bind.bnve.enableItemShiftingMode(false);
        bind.bnve.enableShiftingMode(false);
        bind.bnve.enableAnimation(false);
        bind.bnve.setTextVisibility(false);

        // set adapter
        adapter = new VpAdapter(getSupportFragmentManager(), fragments);
        bind.vp.setAdapter(adapter);
    }

    /**
     * set listeners
     */
    private void initEvent() {
        // set listener to change the current item of view pager when click bottom nav item
        bind.bnve.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            private int previousPosition = -1;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int position = 0;
                switch (item.getItemId()) {
                    case R.id.i_music:
                        position = 0;
                        break;
                    case R.id.i_favor:
                        position = 1;
                        break;
                    case R.id.i_empty: {
                        return false;
                    }
                }
                if(previousPosition != position) {
                    bind.vp.setCurrentItem(position, false);
                    previousPosition = position;
                    Log.i(TAG, "-----bnve-------- previous item:" + bind.bnve.getCurrentItem() + " current item:" + position + " ------------------");
                }

                return true;
            }
        });

        // set listener to change the current checked item of bottom nav when scroll view pager
        bind.vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "-----ViewPager-------- previous item:" + bind.bnve.getCurrentItem() + " current item:" + position + " ------------------");
                if (position >= 2)// 2 is center
                    position++;// if page is 2, need set bottom item to 3, and the same to 3 -> 4
                bind.bnve.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // center item click listener
        bind.fab.setOnClickListener(view -> {
            //Intent i = new Intent(MainPage.this,RegisterActivity.class);
            //startActivity(i);
        });
    }

    /**
     * view pager adapter
     */
    private static class VpAdapter extends FragmentPagerAdapter {
        private List<Fragment> data;

        public VpAdapter(FragmentManager fm, List<Fragment> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Fragment getItem(int position) {
            return data.get(position);
        }
    }
}
