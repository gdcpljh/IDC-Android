package com.id.connect.diaspora.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.adapter.ContactsAdapter;
import com.id.connect.diaspora.utils.Util;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class ContactsFragment extends Fragment {
    private ListView listView;
    private ContactsAdapter contactsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        ContactsFragmentViewHolder viewHolder = new ContactsFragmentViewHolder(v);
        this.listView = (ListView) viewHolder.listView;

        //----------------
        this.contactsAdapter = new ContactsAdapter(getActivity(), nonBotContact());
        listView.setAdapter(contactsAdapter);
        listView.setLongClickable(true);

        RainbowSdk.instance().contacts().getRainbowContacts().registerChangeListener(contactsChangeListener);
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        RainbowSdk.instance().contacts().getRainbowContacts().unregisterChangeListener(contactsChangeListener);
        super.onDestroyView();
    }

    public IItemListChangeListener contactsChangeListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    contactsAdapter.clear();
                    contactsAdapter.addAll(nonBotContact());
                    contactsAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private List<IRainbowContact> nonBotContact(){
        List<IRainbowContact> contacts = RainbowSdk.instance().contacts().getRainbowContacts().getCopyOfDataList();

        Iterator iterator = contacts.iterator();
        while (iterator.hasNext()) {
            IRainbowContact _c = (IRainbowContact) iterator.next();
            if(_c.isBot()){
                iterator.remove();
            }
        }

        for(int i=0; i<contacts.size(); i++){
            if(contacts.get(i).getImJabberId().equals(Util.BOT_JABBER_ID)){
                IRainbowContact tempContact = contacts.get(i);
                for(int j=i; j>0; j--){
                    contacts.set(j, contacts.get(j-1));
                }
                contacts.set(0, tempContact);
                break;
            }
        }
        return contacts;
    }
}

class ContactsFragmentViewHolder{
    public ListView listView;
    public ContactsFragmentViewHolder(View v){
        this.listView = v.findViewById(R.id.contacts_list);
    }
}