package com.id.connect.diaspora.ui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.adapter.FeedAdapter;
import com.id.connect.diaspora.adapter.JobAdapter;
import com.id.connect.diaspora.model.JobModel;
import com.id.connect.diaspora.model.StoryModel;
import com.id.connect.diaspora.ui.activity.LoginActivity;
import com.id.connect.diaspora.utils.Util;
import com.pixplicity.fontview.FontAppCompatTextView;
import com.pixplicity.fontview.FontTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static org.webrtc.ContextUtils.getApplicationContext;

public class ProfileFragment extends Fragment {

    @BindView(R.id.tv_nama)
    TextView tv_nama;
    @BindView(R.id.tv_speciality)
    TextView tv_speciality;
    @BindView(R.id.tv_status)
    TextView tv_status;
    @BindView(R.id.tv_university)
    TextView tv_university;
    @BindView(R.id.tv_hometown)
    TextView tv_hometown;
    @BindView(R.id.newTask)
    FontAppCompatTextView newTask;
    @BindView(R.id.profile_photo)
    CircleImageView profile_photo;
    @BindView(R.id.img_feed)
    ImageView img_feed;
    @BindView(R.id.rv_post)
    RecyclerView rv_post;
    private FeedAdapter mAdapter;
    private ArrayList<StoryModel> storyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        setUpRv();
        loadProfile();
        loadJob();
        return view;
    }

    private void setUpRv() {
        newTask.setText("Profile");
        img_feed.setImageResource(R.drawable.ico_task);

        storyList = new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_post.setLayoutManager(mLayoutManager);
        mAdapter = new FeedAdapter(getContext(), storyList);
        rv_post.setAdapter(mAdapter);
    }

    StoryModel story;
    private void loadJob() {
        SharedPreferences sp = getContext().getSharedPreferences("userLogin", MODE_PRIVATE);
        String diaspora_key1 = sp.getString(Util.DIASPORAS_CODE, "");

        storyList.clear();
        LoginActivity.db
                .collection("story")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String story_key = document.getId();
                                String diaspora_key = document.getData().get("diaspora_key").toString();
                                String created_at = document.getData().get("created_at").toString();
                                String status = document.getData().get("status").toString();
                                try {
                                    String image = document.getData().get("img_url").toString();
                                    story = new StoryModel(story_key, diaspora_key, status, created_at, image);
                                } catch (Exception ex) {
                                    story = new StoryModel(story_key, diaspora_key, status, created_at, "");
                                }

                                if(diaspora_key.equals(diaspora_key1)) {
                                    storyList.add(story);
                                    mAdapter.notifyDataSetChanged();
                                    Log.d("diaspora", diaspora_key);
                                }
                            }
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void loadProfile() {
        SharedPreferences sp = getContext().getSharedPreferences("userLogin", MODE_PRIVATE);
        String diaspora_key1 = sp.getString(Util.DIASPORAS_CODE, "");

        LoginActivity.db
                .collection("diasporas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getId().equals(diaspora_key1)) {
                                    tv_nama.setText(document.getData().get("full_name").toString());
                                    tv_speciality.setText(document.getData().get("major").toString());
                                    tv_status.setText(document.getData().get("status").toString());
                                    tv_university.setText(document.getData().get("university").toString());
                                    tv_hometown.setText(document.getData().get("hometown").toString());

                                    try {
                                        Picasso.get().load(document.getData().get("profile_pic").toString()).into(profile_photo);
                                    } catch (Exception ex) {
                                        profile_photo.setImageDrawable(getContext().getDrawable(R.drawable.diaspora_logo));
                                    }
                                }
                            }
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}
