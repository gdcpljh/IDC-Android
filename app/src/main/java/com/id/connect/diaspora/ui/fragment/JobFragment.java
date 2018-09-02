package com.id.connect.diaspora.ui.fragment;

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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class JobFragment extends Fragment {

    @BindView(R.id.rv_feed)
    RecyclerView rv_feed;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;
    private JobAdapter mAdapter;
    private ArrayList<JobModel> storyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job, container, false);
        ButterKnife.bind(this, view);
        initView();
        setUpRv();
        loadJob();
        return view;
    }

    private void initView() {
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadJob();
            }
        });
    }

    private void setUpRv() {
        storyList = new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_feed.setLayoutManager(mLayoutManager);
        mAdapter = new JobAdapter(getContext(), storyList);
        rv_feed.setAdapter(mAdapter);
    }

    private void loadJob() {
        storyList.clear();
        swipe_refresh.setRefreshing(true);
        LoginActivity.db
                .collection("jobs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String job_key = document.getId();
                                String company = document.getData().get("company_name").toString();
                                String desc = document.getData().get("description").toString();
                                String job_id = document.getData().get("job_id").toString();
                                String location = document.getData().get("location").toString();
                                String position = document.getData().get("position").toString();
                                String published = document.getData().get("published_by").toString();
                                JobModel story = new JobModel(job_key, company, desc, job_id, location, position, published);
                                storyList.add(story);
                                mAdapter.notifyDataSetChanged();
                                Log.d("diaspora", job_key);
                            }
                            swipe_refresh.setRefreshing(false);
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
