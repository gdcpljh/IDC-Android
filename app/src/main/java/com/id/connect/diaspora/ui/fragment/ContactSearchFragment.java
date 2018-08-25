package com.id.connect.diaspora.ui.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale.infra.contact.IContact;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.listener.IRainbowContactsSearchListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.adapter.ContactSearchAdapter;
import com.id.connect.diaspora.utils.AvatarStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alcatel-Dev on 20/09/2017.
 */

public class ContactSearchFragment extends Fragment {
    private String m_keyword;
    private ContactSearchViewHolder viewHolder;
    private ContactSearchAdapter contactSearchAdapter;
    private List<IContact> iContactList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_search, container, false);
        viewHolder = new ContactSearchViewHolder(v);
        iContactList = new ArrayList<IContact>();
//        this.updateSearch();
        return v;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void setKeyword(String keyword){
        this.m_keyword = keyword;
        updateSearch();
    }

    public void showResult(){
        viewHolder.loading.setVisibility(View.GONE);
        viewHolder.resultLayout.setVisibility(View.VISIBLE);
    }

    public void hideResult(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewHolder.resultLayout.setVisibility(View.GONE);
                viewHolder.loading.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateSearchList(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactSearchAdapter = new ContactSearchAdapter(getActivity(), iContactList);
                viewHolder.listView.setAdapter(contactSearchAdapter);
                showResult();
            }
        });
    }

    private void updateSearch(){
        if(this.m_keyword.length() == 0){
            hideResult();
            return;
        }

        final String temp_key = this.m_keyword;
        iContactList.clear();

        RainbowSdk.instance().contacts().searchByName(this.m_keyword, new IRainbowContactsSearchListener() {
            @Override
            public void searchStarted() {}

            @Override
            public void searchFinished(List<IContact> list) {
                if(temp_key.equals(m_keyword)){
                    iContactList.clear();

                    for(int i=0; i< list.size(); i++){
                        final IContact iContact = list.get(i);

                        if(iContact.getCompanyName().equals(RainbowSdk.instance().myProfile().getConnectedUser().getCompanyName())){
                            if( ! iContact.getCorporateId().equals(RainbowSdk.instance().myProfile().getConnectedUser().getCorporateId())){
                                //init photo
                                if(iContact.getPhoto() == null){
                                    new Thread(new AvatarStream(RainbowSdk.instance().contacts().getAvatarUrl(iContact.getCorporateId()), new AvatarStream.Callback() {
                                        @Override
                                        public void onFinish(Bitmap bitmap) {
                                            iContact.setPhoto(bitmap);
                                        }

                                        @Override
                                        public void onError(Throwable t) {

                                        }
                                    })).start();
                                }
                                //

                                iContactList.add(iContact);
                            }
                        }
                    }
                    updateSearchList();
                }
            }

            @Override
            public void searchError(RainbowServiceException e) {}
        });
    }
}

class ContactSearchViewHolder{
    public ListView listView;
    public TextView searchResult;
    public ProgressBar loading;
    public LinearLayout resultLayout;

    public ContactSearchViewHolder(View v){
        this.listView = (ListView) v.findViewById(R.id.list_contact_search);
        this.searchResult = (TextView) v.findViewById(R.id.search_result);
        this.loading = (ProgressBar) v.findViewById(R.id.loading);
        this.resultLayout = (LinearLayout) v.findViewById(R.id.layout_result);
    }
}