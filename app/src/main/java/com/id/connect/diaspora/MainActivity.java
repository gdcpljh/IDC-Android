package com.id.connect.diaspora;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.id.connect.diaspora.databinding.ActivityCenterFabBinding;
import com.id.connect.diaspora.service.MessengerService;
import com.id.connect.diaspora.ui.activity.LoginActivity;
import com.id.connect.diaspora.ui.activity.RegisterSecondActivity;
import com.id.connect.diaspora.ui.fragment.ContactsFragment;
import com.id.connect.diaspora.ui.fragment.ConversationFragment;
import com.id.connect.diaspora.ui.fragment.HomeFragment;
import com.id.connect.diaspora.ui.fragment.ProfileFragment;
import com.id.connect.diaspora.utils.Util;
import com.jsibbold.zoomage.ZoomageView;
import com.rengwuxian.materialedittext.MaterialEditText;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityCenterFabBinding bind;
    private VpAdapter adapter;
    private List<Fragment> fragments;
    private ProgressDialog pDialog;
    private static int RESULT_LOAD_IMAGE = 1;

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
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Uploading..");

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
        ProfileFragment favorFragment = new ProfileFragment();
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
                if (previousPosition != position) {
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

    ImageView iv_story;
    boolean isImage = false;

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_to_story);
        MaterialEditText et_status = (MaterialEditText) dialog.findViewById(R.id.et_status);
        AppCompatButton btn_post = (AppCompatButton) dialog.findViewById(R.id.btn_post);
        iv_story = (ImageView) dialog.findViewById(R.id.iv_story);

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_status.getText().toString().isEmpty()) {
                    et_status.setError("Required!");
                    return;
                }

                //Post to DB
                postStatus(et_status.getText().toString());
                dialog.dismiss();
            }
        });

        iv_story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        dialog.show();
    }

    Uri selectedImage;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            iv_story.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            isImage = true;
        }
    }

    private void uploadStoryPhoto(String diaspora_key, String status) {

        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        StorageReference storyImage = LoginActivity.storageRef.child("story/" + ts + ".jpg");
        iv_story.setDrawingCacheEnabled(true);
        iv_story.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) iv_story.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storyImage.putBytes(data);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                pDialog.setMessage("Uploading " + progress + "%");
                pDialog.show();
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();
                postStory(diaspora_key, status, downloadUrl.toString());
            }
        });
    }

    private void postStatus(String status) {
        SharedPreferences sp = getSharedPreferences("userLogin", MODE_PRIVATE);
        String diaspora_key = sp.getString(Util.DIASPORAS_CODE, "");

        if (isImage) {
            uploadStoryPhoto(diaspora_key, status);
        } else {
            postStory(diaspora_key, status, "");
        }
    }

    private void postStory(String diaspora_key, String status, String img_url) {
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> user = new HashMap<>();
        user.put("diaspora_key", diaspora_key);
        user.put("status", status);
        user.put("img_url", img_url);
        user.put("created_at", currentTime.toString());

        LoginActivity.db
                .collection("story")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toasty.success(MainActivity.this, "Status has been posted!").show();
                        pDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocumentReference", "Error adding document", e);
                        pDialog.dismiss();
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
