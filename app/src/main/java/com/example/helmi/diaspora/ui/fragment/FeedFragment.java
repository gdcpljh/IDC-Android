package com.example.helmi.diaspora.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.helmi.diaspora.R;
import com.example.helmi.diaspora.adapter.FeedAdapter;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FeedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.rv_feed);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        FeedAdapter mAdapter = new FeedAdapter(getContext());
        recyclerView.setAdapter(mAdapter);

        return view;
    }
}
