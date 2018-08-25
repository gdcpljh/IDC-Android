package com.id.connect.diaspora.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.manager.room.RoomStatus;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.listener.IRainbowContactsSearchListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.adapter.ContactSearch2Adapter;
import com.id.connect.diaspora.adapter.ContactSelectAdapter;
import com.id.connect.diaspora.adapter.ParticipantsAdapter;
import com.id.connect.diaspora.model.ContactModel;
import com.id.connect.diaspora.utils.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class RoomDetailActivity extends AppCompatActivity {
    private TextView roomName;
    private TextView roomInfo;
    private TextView roomSubject;
    private TextView roomParticipantInfo;
    private TextView invitationInfo;
    private ListView listView;
    private ListView listViewPending;
    private LinearLayout layoutAdd;
    private ImageView addConfirm;

    private ParticipantsAdapter participantsAdapter;
    private ParticipantsAdapter pendingAdapter;
    private ContactSelectAdapter contactSelectAdapter;
    private ListView listContact;
    private ListView listSearch;
    private EditText inputSearch;
    private ImageView deleteBtn;

    private boolean fromChat;
    private ContactSearch2Adapter contactSearch2Adapter;
    private List<IContact> iContactList;
    private Room room;

    private boolean isOwner = false;
    private MenuItem menuParticipantAdd;
    private MenuItem menuGroupEdit;
    private ProgressBar loading;

    private EditText editGroupName;
    private EditText editGroupSubject;
    private Button editGroupSaveBtn;

    LinearLayout layoutEditGroup;

    private String m_keyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        this.layoutEditGroup = (LinearLayout) findViewById(R.id.layout_edit_group);
        this.loading = (ProgressBar) findViewById(R.id.loading);
        this.roomName = (TextView) findViewById(R.id.participant_name);
        this.roomInfo = (TextView) findViewById(R.id.room_info);
        this.roomSubject = (TextView) findViewById(R.id.room_subject);
        this.roomParticipantInfo = (TextView) findViewById(R.id.room_participant_info);
        this.invitationInfo = (TextView) findViewById(R.id.pending_info);

        this.listView = (ListView) findViewById(R.id.contacts_list);
        this.listViewPending = (ListView) findViewById(R.id.pending_list);

        this.layoutAdd = (LinearLayout) findViewById(R.id.layout_add_participants);
        this.addConfirm = (ImageView) findViewById(R.id.add_confirm);

        this.deleteBtn = (ImageView) findViewById(R.id.delete_bubble);

        this.room = Util.tempRoom;
        this.participantsAdapter = new ParticipantsAdapter(this, new ArrayList<RoomParticipant>());
        this.pendingAdapter = new ParticipantsAdapter(this, new ArrayList<RoomParticipant>());

        this.listView.setAdapter(this.participantsAdapter);
        this.listViewPending.setAdapter(this.pendingAdapter);

        this.editGroupName = (EditText) findViewById(R.id.edit_group_name);
        this.editGroupSubject = (EditText) findViewById(R.id.edit_group_subject);
        this.editGroupSaveBtn = (Button) findViewById(R.id.edit_group_save);

        this.editGroupSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomName = editGroupName.getText().toString();
                String roomSubject = editGroupSubject.getText().toString();

                RainbowSdk.instance().bubbles().changeBubbleData(room, roomName, roomSubject, true, new IRoomProxy.IChangeRoomDataListener() {
                    @Override
                    public void onChangeRoomDataSuccess(Room room) {}

                    @Override
                    public void onChangeRoomDataFailed(String s) {}
                });

                layoutEditGroup.setVisibility(View.GONE);
            }
        });

        updateRoom();
        this.room.registerChangeListener(roomListener);

        listContact = (ListView) findViewById(R.id.list_contact);
        listSearch = (ListView) findViewById(R.id.list_contact_search);
        inputSearch = (EditText) findViewById(R.id.input_search);

        iContactList = new ArrayList<IContact>();

        inputSearch.addTextChangedListener(searchListener);

        this.deleteBtn.setOnClickListener(deleteBtnListener);
        this.addConfirm.setOnClickListener(addConfirmListener);

        this.room.getParticipants().registerChangeListener(new IItemListChangeListener() {
            @Override
            public void dataChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateRoom();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(layoutAdd.getVisibility() == View.VISIBLE) {
            layoutAdd.setVisibility(View.GONE);
            getSupportActionBar().show();
        }else if(layoutEditGroup.getVisibility() == View.VISIBLE){
            layoutEditGroup.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    private void updateContactSelect(){
        List<IRainbowContact> contacts = nonBotContact();
        List<ContactModel> contactModels = new ArrayList<ContactModel>(contacts.size());
        for(IRainbowContact c : contacts){
            boolean isAdded = false;
            for(RoomParticipant _p : room.getParticipants().getCopyOfDataList()){
                if(_p.getContact().getContactId().equals(c.getContactId())){
                    isAdded = true;
                    break;
                }
            }
            if(!isAdded) contactModels.add(new ContactModel(c, false));
        }
        contactSelectAdapter = new ContactSelectAdapter(this, contactModels);
        listContact.setAdapter(contactSelectAdapter);
    }

    private List<IRainbowContact> nonBotContact(){
        List<IRainbowContact> contacts = RainbowSdk.instance().contacts().getRainbowContacts().getCopyOfDataList();

        Iterator iterator = contacts.iterator();
        while (iterator.hasNext()) {
            IRainbowContact _c = (IRainbowContact) iterator.next();
            if(_c.isBot()){
                iterator.remove();
            }
        }
        return contacts;
    }

    View.OnClickListener deleteBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        new AlertDialog.Builder(RoomDetailActivity.this)
                                .setTitle(getString(R.string.confirmation))
                                .setMessage(getString(R.string.confirm_delete_bubble))
                                .setIcon(R.drawable.ic_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        RainbowSdk.instance().bubbles().deleteBubble(room, new IRoomProxy.IDeleteRoomListener() {
                                            @Override
                                            public void onRoomDeletedSuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        finish();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onRoomDeletedFailed() {
                                                Toasty.error(getBaseContext(), "Failed");
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }catch (Exception e){
                        Util.log(e.toString());
                    }
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.room_menu, menu);
        menuParticipantAdd = menu.findItem(R.id.participant_add);
        menuGroupEdit = menu.findItem(R.id.room_edit);
        menuParticipantAdd.setVisible(isOwner);
        menuGroupEdit.setVisible(isOwner);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.room_chat:
                if(fromChat){
                    finish();
                }else{
                    Intent intent = new Intent(getBaseContext(), ChatRoomActivity.class);
                    startActivity(intent);
                }
                return true;
            case R.id.participant_add:
                updateContactSelect();
                this.layoutAdd.setVisibility(View.VISIBLE);
                getSupportActionBar().hide();
                return true;
            case R.id.room_edit:
                layoutEditGroup.setVisibility(View.VISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener addConfirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            layoutAdd.setVisibility(View.GONE);
            getSupportActionBar().show();

            List<IRainbowContact> contactToInvite = new ArrayList<IRainbowContact>();
            for(int i=0; i<contactSelectAdapter.getCount(); i++){
                ContactModel cm = contactSelectAdapter.getItem(i);
                if(cm.isChecked()){
                    contactToInvite.add(cm.getContact());
                }
            }
            if(contactToInvite.size() > 0){
                RainbowSdk.instance().bubbles().addParticipantsToBubble(room, contactToInvite, new IRoomProxy.IAddParticipantsListener() {
                    @Override
                    public void onAddParticipantsSuccess() {

                    }

                    @Override
                    public void onMaxParticipantsReached() {

                    }

                    @Override
                    public void onAddParticipantFailed(Contact contact) {

                    }
                });
                updateRoom();
            }
        }
    };

    private void updateRoom(){
        this.roomName.setText(this.room.getName());
        this.editGroupName.setText(this.room.getName());

        this.roomSubject.setText(this.room.getTopic());
        this.editGroupSubject.setText(this.room.getTopic());

        Contact owner = room.getOwner();
        try{
            this.roomInfo.setText(getString(R.string.created_on)+" "+Util.txtDate(room.getCreationDate())+" "+getString(R.string.created_by)+" "+owner.getFirstName()+" "+owner.getLastName());
        }catch (Exception e){
            Util.log(e.toString());
        }

        try{
            isOwner = room.isUserOwner();
            menuParticipantAdd.setVisible(isOwner);
            menuGroupEdit.setVisible(isOwner);
        }catch (Exception e){}

        deleteBtn.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        this.participantsAdapter.setAdmin(isOwner);
        this.pendingAdapter.setAdmin(isOwner);

        this.participantsAdapter.clear();
        this.pendingAdapter.clear();
        for(RoomParticipant p : this.room.getParticipants().getCopyOfDataList()){
            if(p.getStatus().equals(RoomStatus.ACCEPTED)){
                this.participantsAdapter.add(p);
            }else if(p.getStatus().equals(RoomStatus.INVITED)){
                this.pendingAdapter.add(p);
            }
        }

        this.invitationInfo.setVisibility(pendingAdapter.getCount() > 0 ? View.VISIBLE : View.GONE);

        this.participantsAdapter.notifyDataSetChanged();
        this.pendingAdapter.notifyDataSetChanged();
        this.roomParticipantInfo.setText(String.valueOf(this.participantsAdapter.getCount())+" "+getString(R.string.members));
    }

    Room.RoomListener roomListener = new Room.RoomListener() {
        @Override
        public void roomUpdated(Room room) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateRoom();
                }
            });
        }

        @Override
        public void conferenceUpdated(Room room) {

        }
    };

    private TextWatcher searchListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            m_keyword = charSequence.toString();

            if(m_keyword.length() > 2){
                listContact.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);

                updateSearch();
            }else{
                listSearch.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                listContact.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };


    private void updateSearch(){
        final String temp_key = m_keyword;

        RainbowSdk.instance().contacts().searchByName(m_keyword, new IRainbowContactsSearchListener() {
            @Override
            public void searchStarted() {}

            @Override
            public void searchFinished(List<IContact> list) {
                if(temp_key.equals(m_keyword)){
                    iContactList.clear();
                    for(int i=0; i<15 && i< list.size(); i++){
                        IContact iContact = list.get(i);

                        if( ! iContact.getCorporateId().equals(RainbowSdk.instance().myProfile().getConnectedUser().getCorporateId())){
                            iContactList.add(iContact);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            contactSearch2Adapter = new ContactSearch2Adapter(RoomDetailActivity.this, iContactList);
                            listSearch.setAdapter(contactSearch2Adapter);
                            loading.setVisibility(View.GONE);
                            listSearch.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void searchError(RainbowServiceException e) {

            }
        });
    }
}
