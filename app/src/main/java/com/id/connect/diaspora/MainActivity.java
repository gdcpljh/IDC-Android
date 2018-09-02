package com.id.connect.diaspora;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.id.connect.diaspora.databinding.ActivityCenterFabBinding;
import com.id.connect.diaspora.ui.activity.LoginActivity;
import com.id.connect.diaspora.ui.activity.RegisterSecondActivity;
import com.id.connect.diaspora.ui.fragment.ContactsFragment;
import com.id.connect.diaspora.ui.fragment.ConversationFragment;
import com.id.connect.diaspora.ui.fragment.HomeFragment;
import com.id.connect.diaspora.utils.Util;
import com.jsibbold.zoomage.ZoomageView;
import com.rengwuxian.materialedittext.MaterialEditText;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityCenterFabBinding bind;
    private VpAdapter adapter;
    private List<Fragment> fragments;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_center_fab);
        initData();
        initView();
        initEvent();
    }

    /**
     * create fragments
     */
    private void initData() {
        fragments = new ArrayList<>(4);

        // create music fragment and add it
        HomeFragment musicFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.beranda));
        musicFragment.setArguments(bundle);

        // create backup fragment and add it
        ContactsFragment backupFragment = new ContactsFragment();
        bundle = new Bundle();
        bundle.putString("title", getString(R.string.contact));
        backupFragment.setArguments(bundle);

        // create friends fragment and add it
        HomeFragment favorFragment = new HomeFragment();
        bundle = new Bundle();
        bundle.putString("title", getString(R.string.chat));
        favorFragment.setArguments(bundle);

        // create friends fragment and add it
        ConversationFragment visibilityFragment = new ConversationFragment();
        bundle = new Bundle();
        bundle.putString("title", getString(R.string.profile));
        visibilityFragment.setArguments(bundle);

        // add to fragments for adapter
        fragments.add(musicFragment);
        fragments.add(backupFragment);
        fragments.add(favorFragment);
        fragments.add(visibilityFragment);
    }


    /**
     * change BottomNavigationViewEx style
     */
    private void initView() {
        bind.bnve.enableItemShiftingMode(false);
        bind.bnve.enableShiftingMode(false);
        bind.bnve.enableAnimation(false);

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
                    case R.id.i_contact:
                        position = 1;
                        break;
                    case R.id.i_favor:
                        position = 2;
                        break;
                    case R.id.i_chat:
                        position = 3;
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
                if (position == 2)// 2 is center
                    position++;// if page is 2, need set bottom item to 3, and the same to 3 -> 4
                bind.bnve.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // center item click listener
        bind.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_to_story);
        MaterialEditText et_status = (MaterialEditText) dialog.findViewById(R.id.et_status);
        AppCompatButton btn_post = (AppCompatButton) dialog.findViewById(R.id.btn_post);

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_status.getText().toString().isEmpty()) {
                    et_status.setError("Required!");
                    return;
                }

                //Post to DB
                postStatus(et_status.getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void postStatus(String status) {
        SharedPreferences sp = getSharedPreferences("userLogin", MODE_PRIVATE);
        String diaspora_key = sp.getString(Util.DIASPORAS_CODE, "");
        Date currentTime = Calendar.getInstance().getTime();

        Map<String, Object> user = new HashMap<>();
        user.put("diaspora_key", diaspora_key);
        user.put("status", status);
        user.put("created_at", currentTime.toString());

        db.collection("story")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toasty.success(MainActivity.this, "Status has been posted!").show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocumentReference", "Error adding document", e);
                    }
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
