package com.id.connect.diaspora.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.http.GetFileResponse;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.infra.xmpp.xep.IMamNotification;
import com.ale.listener.IRainbowContactsSearchListener;
import com.ale.listener.IRainbowGetConversationListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.adapter.InstantMessagesAdapter;
import com.id.connect.diaspora.utils.Util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private int NB_MESSAGES_TO_RETRIEVE = 50;
    private ImageView btnSend;
    private EditText inputChat;
    private TextView toolbarTitle;
    private ImageView toolbarPhoto;
    private ImageView btnAttachment;

    private IRainbowConversation conversation;
    private Contact contact;
    private InstantMessagesAdapter instantMessagesAdapter;

    private ImageView btnRecord;

    private ListView listView;

    private MediaRecorder mRecorder;

    private File mOutputFile;
    private boolean isRecordAllow = true;

    private int PICK_FILE_CODE = 1;
    private int PICK_BACKGROUND_CODE = 2;
    private Uri temp_uri;

    long mStartTime;
    private boolean isAttach = false;

    private ConstraintLayout previewLayout;
    private TextView prevFileName;
    private ImageView prevFileCancelBtn;
    private ImageView prevImage;
    private TextView prevFileSize;

    private ConstraintLayout bgPrevLayout;
    private Button bgCancelBtn;
    private Button bgSaveBtn;

    private LinearLayout main_layout;

    MediaPlayer btnRecordMP;
    String bgPath = "";
    Drawable currentBg;
    boolean isCustomBg = false;
    Menu m_menu;
    boolean isAllowReadcontact = false;

    private Boolean isBook = false;
    private String bookRoom;
    private String bookDate;
    private String bookDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        this.main_layout = (LinearLayout) findViewById(R.id.main_layout);

        this.toolbarTitle = (TextView) toolbar.findViewById(R.id.chat_title);
        this.toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContact();
            }
        });

        this.toolbarPhoto = (ImageView) toolbar.findViewById(R.id.chat_photo);
        this.toolbarPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContact();
            }
        });

        this.btnSend = (ImageView) findViewById(R.id.send_button);
        this.inputChat = (EditText) findViewById(R.id.chat_input);

        this.inputChat.addTextChangedListener(inputChatListener);

        this.btnSend.setOnClickListener(sendClickListener);
        this.listView = (ListView) findViewById(R.id.chat_list);

        this.btnRecord = (ImageView) findViewById(R.id.record_button);
        this.mRecorder = new MediaRecorder();

        this.btnAttachment = (ImageView) findViewById(R.id.attch_button);
        this.btnAttachment.setOnClickListener(btnAttachmentClickListener);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle != null){
            String contactJId = bundle.getString("CONTACT_JID");
            this.contact = (Contact) RainbowSdk.instance().contacts().getContactFromJabberId(contactJId);
        }else{
            this.contact = Util.tempContact;
        }

        this.contact.registerChangeListener(contactListener);
        updateContact();

        if(this.contact.getImJabberId() == null){
            this.conversation = new Conversation(this.contact);
            initConversation();
        }else{
            this.conversation = RainbowSdk.instance().conversations().getConversationFromContact(this.contact.getImJabberId(), getConversationListener);
        }

        this.btnRecord.setOnTouchListener(recordTouchListener);

        String [] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO", "android.permission.READ_CONTACTS"};

        int permsRequestCode = 200;
        ActivityCompat.requestPermissions(this,  perms, permsRequestCode);

        this.previewLayout = (ConstraintLayout) findViewById(R.id.layout_preview);
        this.prevFileName = (TextView) findViewById(R.id.prev_file_name);
        this.prevFileCancelBtn = (ImageView) findViewById(R.id.prev_file_cancel);
        this.prevImage = (ImageView) findViewById(R.id.prev_image);
        this.prevFileSize = (TextView) findViewById(R.id.prev_file_size);

        this.prevFileCancelBtn.setOnClickListener(cancelAttachListener);
        this.prevImage.setOnClickListener(prevImageclick);

        this.bgPrevLayout = (ConstraintLayout) findViewById(R.id.layout_bg_prev);

        this.bgCancelBtn = (Button) findViewById(R.id.btn_cancel_bg);
        this.bgSaveBtn = (Button) findViewById(R.id.btn_save_bg);
        this.bgCancelBtn.setOnClickListener(cancelBg);
        this.bgSaveBtn.setOnClickListener(saveBg);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.m_menu = menu;
        /*if(this.contact.getImJabberId().equals(Util.BOT_JABBER_ID) || this.contact.getImJabberId().equals(Util.BOT_INTERNAL_JABBER_ID))
        {
            getMenuInflater().inflate(R.menu.chat_bot_menu, menu);
        }
        else {*/
        getMenuInflater().inflate(R.menu.chat_menu, menu);
//        }
        initBackground();
        return true;
    }

    private void showContact(){
        Intent intentProfile = new Intent(getBaseContext(), ContactDetailActivity.class);
        intentProfile.putExtra("FROM_CHAT", true);
        startActivity(intentProfile);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(this.contact.getImJabberId().equals(Util.BOT_JABBER_ID) || this.contact.getImJabberId().equals(Util.BOT_INTERNAL_JABBER_ID)){
            switch (item.getItemId()) {
                case R.id.profile:
                    showContact();
                    break;
//                case R.id.call:
//                    RainbowSdk.instance().webRTC().makeCall((Contact) this.contact, false);
//                    Intent intentCall = new Intent(getBaseContext(), VoiceCallActivity.class);
//                    startActivity(intentCall);
//                    break;
//                case R.id.video_call:
//                    RainbowSdk.instance().webRTC().makeCall((Contact) this.contact, true);
//                    Intent intentVCall = new Intent(getBaseContext(), VoiceCallActivity.class);
//                    startActivity(intentVCall);
//                    break;
                case R.id.clear:
                    clearConversation();
                    break;
                case R.id.background:
                    browseBackground();
                    break;
                case R.id.background_reset:
                    resetBackground();
                    break;
//            case R.id.translate_off:
//                switchLang("off");
//                break;
//            case R.id.translate_bengali:
//                switchLang("bn");
//                break;
//            case R.id.translate_tamil:
//                switchLang("ta");
//                break;
//            case R.id.translate_china:
//                switchLang("zh");
//                break;
//            case R.id.translate_english:
//                switchLang("en");
//                break;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else {
            switch (item.getItemId()) {
                case R.id.profile:
                    showContact();
                    break;
                case R.id.call:
                    RainbowSdk.instance().webRTC().makeCall((Contact) this.contact, false);
                    Intent intentCall = new Intent(getBaseContext(), VoiceCallActivity.class);
                    startActivity(intentCall);
                    break;
                case R.id.video_call:
                    RainbowSdk.instance().webRTC().makeCall((Contact) this.contact, true);
                    Intent intentVCall = new Intent(getBaseContext(), VoiceCallActivity.class);
                    startActivity(intentVCall);
                    break;
                case R.id.clear:
                    clearConversation();
                    break;
                case R.id.background:
                    browseBackground();
                    break;
                case R.id.background_reset:
                    resetBackground();
                    break;
//            case R.id.translate_off:
//                switchLang("off");
//                break;
//            case R.id.translate_bengali:
//                switchLang("bn");
//                break;
//            case R.id.translate_tamil:
//                switchLang("ta");
//                break;
//            case R.id.translate_china:
//                switchLang("zh");
//                break;
//            case R.id.translate_english:
//                switchLang("en");
//                break;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    private void switchLang(String lang){
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("AUTO_TRANSLATE", lang);
        editor.commit();

        Util.AUTO_TRANSLATE = lang;
        updateIM();
    }

    @Override
    public void onResume(){
        super.onResume();
        updateSendButton();
    }

    private void clearConversation(){
        new android.app.AlertDialog.Builder(ChatActivity.this)
                .setTitle(getString(R.string.confirmation))
                .setMessage(getString(R.string.confirm_delete_conversation))
                .setIcon(R.drawable.ic_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        RainbowSdk.instance().conversations().deleteAllMessages(conversation, new IMamNotification() {
                            @Override
                            public void complete(ArrayItemList<IMMessage> arrayItemList, boolean b) {}

                            @Override
                            public void complete(String s, boolean b) {}

                            @Override
                            public void error(Exception e) {}

                            @Override
                            public void timeout() {}
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void browseBackground(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_BACKGROUND_CODE);
    }

    private void initConversation(){
        this.instantMessagesAdapter = new InstantMessagesAdapter(this, this.conversation.getMessages().getCopyOfDataList());
        listView.setAdapter(instantMessagesAdapter);
        listView.setSelection(instantMessagesAdapter.getCount() - 1);

        RainbowSdk.instance().im().getMessagesFromConversation(this.conversation, NB_MESSAGES_TO_RETRIEVE);
        this.conversation.getMessages().registerChangeListener(instantMessagesChangeListener);

        RainbowSdk.instance().im().markMessagesFromConversationAsRead(conversation);
        updateIM();
        this.currentBg = ActivityCompat.getDrawable(this, R.drawable.chat_bg);
    }

    private IRainbowGetConversationListener getConversationListener = new IRainbowGetConversationListener() {
        @Override
        public void onGetConversationSuccess(IRainbowConversation iRainbowConversation) {
            conversation = iRainbowConversation;
            initConversation();
        }

        @Override
        public void onGetConversationError() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    conversation = new Conversation(contact);
                    initConversation();
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        if(this.conversation != null){
            this.conversation.getMessages().unregisterChangeListener(instantMessagesChangeListener);
        }
        if(this.contact != null){
            this.contact.unregisterChangeListener(contactListener);
        }
        super.onDestroy();
    }

    private void updateIM(){
        instantMessagesAdapter.clear();
        boolean firstMessage = false;
        List<IMMessage> messagesList = conversation.getMessages().getCopyOfDataList();

        /*if(this.contact.getImJabberId().equals(Util.BOT_JABBER_ID) || this.contact.getImJabberId().equals(Util.BOT_INTERNAL_JABBER_ID)){
            if(messagesList.size() > 0){
                for(int i=0; i<messagesList.size(); i++) {
                    if (messagesList.get(i).getMessageContent().startsWith("WELCOMING_MESSAGE_ALE_HOTEL")) {
                        messagesList.remove(i);
                    }
                }
            }
        }*/

        instantMessagesAdapter.addAll(messagesList);
        instantMessagesAdapter.notifyDataSetChanged();
        listView.setSelection(instantMessagesAdapter.getCount() - 1);

        RainbowSdk.instance().im().markMessagesFromConversationAsRead(conversation);

        for(IMMessage message : messagesList){
            if(message.isFileDescriptorAvailable()){
                RainbowFileDescriptor fileDescriptor = message.getFileDescriptor();
                if(! RainbowSdk.instance().fileStorage().isDownloaded(fileDescriptor)){
                    RainbowSdk.instance().fileStorage().downloadFile(fileDescriptor, new IFileProxy.IDownloadFileListener() {
                        @Override
                        public void onDownloadSuccess(GetFileResponse getFileResponse) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    instantMessagesAdapter.notifyDataSetChanged();
                                }
                            });
                            Util.log("onDownloadSuccess");
                        }

                        @Override
                        public void onDownloadInProgress(GetFileResponse getFileResponse) {
                            Util.log("onDownloadInProgress");
                        }

                        @Override
                        public void onDownloadFailed(boolean b) {
                            Util.log("onDownloadFailed");
                        }
                    });
                }
            }
        }
    }

    private IItemListChangeListener instantMessagesChangeListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateIM();
                }
            });
        }
    };

    private void sendMessage(){
        final String msg = this.inputChat.getText().toString();

        if(isAttach){
            previewLayout.setVisibility(View.GONE);
            sendFileMessage(msg);
            isAttach = false;
            this.inputChat.setText("");
        }else{
            if(msg.length() != 0){
                this.inputChat.setText("");
                RainbowSdk.instance().im().sendMessageToConversation(this.conversation, msg);

            }
        }
    }

    private void updateContact(){
        String contact_name = this.contact.getFirstName()+" "+this.contact.getLastName();
        toolbarTitle.setText(contact_name);
        Bitmap bmp = this.contact.getPhoto();
        if(bmp != null){
            toolbarPhoto.setImageBitmap(Util.getRoundedCornerBitmap(bmp, bmp.getWidth()));
        }else{
            String initName = "?";
            try{
                initName = this.contact.getFirstName().substring(0,1).toUpperCase() + this.contact.getLastName().substring(0,1).toUpperCase();
            }catch (Exception e){}
            toolbarPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(contact_name)));
        }

        /*if(this.contact.getImJabberId().equals(Util.BOT_JABBER_ID)||this.contact.getImJabberId().equals(Util.BOT_INTERNAL_JABBER_ID)){
            this.btnAttachment.setVisibility(View.GONE);
            this.btnRecord.setVisibility(View.GONE);
        }*/
    }

    private Contact.ContactListener contactListener = new Contact.ContactListener() {
        @Override
        public void contactUpdated(Contact contact) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateContact();
                }
            });
        }
        @Override
        public void onPresenceChanged(Contact contact, RainbowPresence rainbowPresence) {}
        @Override
        public void onActionInProgress(boolean b) {}
    };

    View.OnClickListener sendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendMessage();
        }
    };

    private void callHotelService(String jid)
    {
        final Contact contactResult;
        RainbowSdk.instance().contacts().searchByJid(jid, new IRainbowContactsSearchListener() {
            @Override
            public void searchStarted() {}

            @Override
            public void searchFinished(List<IContact> list) {
                if(list.size() > 0){
                    try{
                        RainbowSdk.instance().contacts().getUserDataFromId(list.get(0).getCorporateId(), new IUserProxy.IGetUserDataListener() {
                            @Override
                            public void onSuccess(final Contact contact) {
                                try
                                {
                                    RainbowSdk.instance().webRTC().makeCall(contact, false);
                                    Intent intentCall = new Intent(getBaseContext(), VoiceCallActivity.class);
                                    startActivity(intentCall);
                                }
                                catch (Exception e)
                                {

                                }
                            }

                            @Override
                            public void onFailure(RainbowServiceException e) {}
                        });
                    }catch (Exception e){}
                }
            }

            @Override
            public void searchError(RainbowServiceException e) {}
        });

    }

    private void createConversation(String jid)
    {
        RainbowSdk.instance().contacts().searchByJid(jid, new IRainbowContactsSearchListener() {
            @Override
            public void searchStarted() {}

            @Override
            public void searchFinished(List<IContact> list) {
                if(list.size() > 0){
                    try{
                        RainbowSdk.instance().contacts().getUserDataFromId(list.get(0).getCorporateId(), new IUserProxy.IGetUserDataListener() {
                            @Override
                            public void onSuccess(final Contact contact) {
                                try{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            RainbowSdk.instance().conversations().getConversationFromContact(contact.getImJabberId(), new IRainbowGetConversationListener() {
                                                @Override
                                                public void onGetConversationSuccess(final IRainbowConversation iRainbowConversation) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            Conversation c = new Conversation(contact);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onGetConversationError() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Conversation c = new Conversation(contact);
                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    });
                                }catch (Exception ee){}
                            }

                            @Override
                            public void onFailure(RainbowServiceException e) {}
                        });
                    }catch (Exception e){}
                }
            }

            @Override
            public void searchError(RainbowServiceException e) {}
        });
    }

    private void chatAnotherMessage(String jid, final String msg)
    {
        RainbowSdk.instance().contacts().searchByJid(jid, new IRainbowContactsSearchListener() {
            @Override
            public void searchStarted() {}

            @Override
            public void searchFinished(List<IContact> list) {
                if(list.size() > 0){
                    try{
                        RainbowSdk.instance().contacts().getUserDataFromId(list.get(0).getCorporateId(), new IUserProxy.IGetUserDataListener() {
                            @Override
                            public void onSuccess(final Contact contact) {
                                try{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            RainbowSdk.instance().conversations().getConversationFromContact(contact.getImJabberId(), new IRainbowGetConversationListener() {
                                                @Override
                                                public void onGetConversationSuccess(final IRainbowConversation iRainbowConversation) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            Conversation c = new Conversation(contact);
                                                            RainbowSdk.instance().im().sendMessageToConversation(c, "WELCOMING_MESSAGE_ALE_HOTEL "+msg);

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onGetConversationError() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Conversation c = new Conversation(contact);
                                                            RainbowSdk.instance().im().sendMessageToConversation(c, "WELCOMING_MESSAGE_ALE_HOTEL "+msg);
                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    });
                                }catch (Exception ee){}
                            }

                            @Override
                            public void onFailure(RainbowServiceException e) {}
                        });
                    }catch (Exception e){}
                }
            }

            @Override
            public void searchError(RainbowServiceException e) {}
        });
    }

    View.OnTouchListener recordTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                playRecBtn(false);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startRecording();
                    }
                }, 500);
            } else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                stopRecording(true);
                playRecBtn(true);
            }
            return true;
        }
    };

    private void playRecBtn(boolean release){
        if(btnRecordMP != null){
            if (btnRecordMP.isPlaying()) {
                btnRecordMP.stop();
            }
            btnRecordMP.release();
            btnRecordMP = null;
        }

        try{
            String toneString = "android.resource://id.co.ale.rainbowDev/" + (release ? R.raw.record1 : R.raw.record2);
            Uri toneUri = Uri.parse(toneString);

            btnRecordMP = new MediaPlayer();
            btnRecordMP.setDataSource(getApplicationContext(), toneUri);
            btnRecordMP.prepare();
            btnRecordMP.start();
        }catch (Exception e){}
    }

    private void startRecording() {
        Util.log("start recording");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            mRecorder.setAudioEncodingBitRate(48000);
        } else {
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(64000);
        }
        mRecorder.setAudioSamplingRate(16000);
        mOutputFile = getOutputFile();
        mOutputFile.getParentFile().mkdirs();
        mRecorder.setOutputFile(mOutputFile.getAbsolutePath());

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartTime = SystemClock.elapsedRealtime();
        } catch (IOException e) {}
    }

    protected  void stopRecording(boolean saveFile) {
        final Handler handler = new Handler();
        final boolean isSave = saveFile;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                mStartTime = 0;
                if (!isSave && mOutputFile != null) {
                    mOutputFile.delete();
                }

                temp_uri = Uri.fromFile(mOutputFile);
                Util.log(temp_uri.toString());
                sendFileMessage("");
            }
        }, 500);
    }

    private File getOutputFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.ENGLISH);
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString()
                + "/Voice Recorder/RECORDING_"
                + dateFormat.format(new Date())
                + ".wav";
        Util.log("record path: "+filePath);
        return new File(filePath);
    }

    View.OnClickListener btnAttachmentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_FILE_CODE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            Util.log(data.getData().toString());

            if (requestCode == PICK_FILE_CODE) {
                try{
                    if (data == null) {
                        return;
                    }

                    temp_uri = data.getData();
                    this.isAttach = true;
                    showPreviewFile();
                }catch (Exception e){
                    Util.log(e.toString());
                }
            }else if(requestCode == PICK_BACKGROUND_CODE && data != null){
                try{
                    getSupportActionBar().hide();
                    bgPrevLayout.setVisibility(View.VISIBLE);
                    Uri selectedImage = data.getData();
                    bgPath = Util.getPath(this, selectedImage);

                    Drawable bg = Drawable.createFromPath(bgPath);
                    main_layout.setBackground(bg);
                }catch (Exception e){
                    Util.log(e.toString());
                }
            }
        }
    }

    private void resetBackground(){
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("bg_"+conversation.getId());
        editor.commit();
        main_layout.setBackground(getDrawable(R.drawable.chat_bg));
    }

    private View.OnClickListener saveBg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(conversation != null){
                SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("bg_"+conversation.getId(), bgPath);
                editor.commit();
                initBackground();

                bgPrevLayout.setVisibility(View.GONE);
                getSupportActionBar().show();
            }
        }
    };

    private View.OnClickListener cancelBg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bgPrevLayout.setVisibility(View.GONE);
            main_layout.setBackground(currentBg);
            getSupportActionBar().show();
        }
    };

    private void initBackground(){
        if(conversation != null){
            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            String savedBgPath = sp.getString("bg_"+conversation.getId(), null);

            if(savedBgPath != null){
                try{
                    Drawable bg = Drawable.createFromPath(savedBgPath);
                    main_layout.setBackground(bg);
                    currentBg = bg;
                    isCustomBg = true;
                }catch (Exception e){
                    Util.log(e.toString());
                }
            }

            try {
                MenuItem resetBgMenu = this.m_menu.findItem(R.id.background_reset);
                resetBgMenu.setVisible(isCustomBg);
            }catch (Exception e){}
        }
    }

    private void showPreviewFile(){
        String file_path = Util.getPath(getBaseContext(), temp_uri); // .temp_uri.toString().replace("file://", "" );
        File f = new File(file_path);
        this.prevFileName.setText(f.getName());
        this.prevFileSize.setText(Util.txtFileSize(f.length()));

        String extension = file_path.substring(file_path.lastIndexOf("."));

        if(Util.getMime(extension).equals("image")){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(Util.getPath(getBaseContext(), temp_uri), bmOptions);
            bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50,true);
            this.prevImage.setImageBitmap(bitmap);
        }else if (Util.getMime(extension).equals("pdf")){
            this.prevImage.setImageDrawable(getDrawable(R.drawable.ic_pdf));
        }else if(Util.getMime(extension).equals("audio")){
            this.prevImage.setImageDrawable(getDrawable(R.drawable.ic_audio_video));
        }else if(Util.getMime(extension).equals("doc")){
            this.prevImage.setImageDrawable(getDrawable(R.drawable.ic_doc));
        }else if(Util.getMime(extension).equals("video")){
            this.prevImage.setImageDrawable(getDrawable(R.drawable.ic_video_prev));
        }else if(Util.getMime(extension).equals("ppt")){
            this.prevImage.setImageDrawable(getDrawable(R.drawable.ic_ppt));
        }else if(Util.getMime(extension).equals("xls")){
            this.prevImage.setImageDrawable(getDrawable(R.drawable.ic_excel));
        }else{
            this.prevImage.setImageDrawable(getDrawable(R.drawable.ic_file));
        }

        this.previewLayout.setVisibility(View.VISIBLE);
        updateSendButton();
    }

    View.OnClickListener prevImageclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    View.OnClickListener cancelAttachListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            previewLayout.setVisibility(View.GONE);
            isAttach = false;
            updateSendButton();
        }
    };

    private void sendFileMessage(String msg){
        RainbowSdk.instance().fileStorage().uploadFileToConversation(conversation, temp_uri, msg, new IFileProxy.IUploadFileListener() {
            @Override
            public void onUploadSuccess(RainbowFileDescriptor fileDescriptor) {
                Util.log("upload success");
            }

            @Override
            public void onUploadInProgress(int percent) {
                Util.log("upload in progress "+String.valueOf(percent));
            }

            @Override
            public void onUploadFailed(RainbowServiceException exception) {
                Util.log("upload failed "+exception.toString());
            }
        });
        temp_uri = null;
    }

    private void updateSendButton(){
        /*try{
            if(contact.getImJabberId().equals(Util.BOT_JABBER_ID) || contact.getImJabberId().equals(Util.BOT_INTERNAL_JABBER_ID)){
                isRecordAllow = false;
            }
        }catch (Exception e){}*/

        if(isRecordAllow){
            if(isAttach || inputChat.getText().length() > 0){
                btnRecord.setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
            }else{
                btnSend.setVisibility(View.GONE);
                btnRecord.setVisibility(View.VISIBLE);
            }
        }else{
            btnRecord.setVisibility(View.GONE);
            btnSend.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean recordAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                isAllowReadcontact = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                if(writeAccepted && recordAccepted){
                    isRecordAllow = true;
                }else{
                    isRecordAllow = false;
                }
                updateSendButton();
                break;
        }
    }

    private TextWatcher inputChatListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            updateSendButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
}
