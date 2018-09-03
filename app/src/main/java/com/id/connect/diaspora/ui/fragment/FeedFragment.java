package com.id.connect.diaspora.ui.fragment;

import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.id.connect.diaspora.MainActivity;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.adapter.FeedAdapter;
import com.id.connect.diaspora.model.StoryModel;
import com.id.connect.diaspora.service.MessengerService;
import com.id.connect.diaspora.ui.activity.LoginActivity;
import com.id.connect.diaspora.utils.Util;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedFragment extends Fragment {

    @BindView(R.id.rv_feed)
    RecyclerView rv_feed;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;
    private FeedAdapter mAdapter;
    private ArrayList<StoryModel> storyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, view);
        initView();
        setUpRv();
        loadStory();
        return view;
    }

    private void initView() {
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadStory();
            }
        });
    }

    private void setUpRv() {
        storyList = new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_feed.setLayoutManager(mLayoutManager);
        mAdapter = new FeedAdapter(getContext(), storyList);
        rv_feed.setAdapter(mAdapter);
    }

    StoryModel story;
    private void loadStory() {
        storyList.clear();
        swipe_refresh.setRefreshing(true);
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
                                storyList.add(story);
                                mAdapter.notifyDataSetChanged();
                                Log.d("diaspora", diaspora_key);
                            }
                            swipe_refresh.setRefreshing(false);
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
