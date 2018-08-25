package com.id.connect.diaspora.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.call.ITelephonyListener;
import com.ale.infra.manager.call.WebRTCCall;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.listener.IRainbowImListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.ui.activity.ChatActivity;
import com.id.connect.diaspora.ui.activity.VoiceCallActivity;
import com.id.connect.diaspora.utils.Util;

import java.util.List;

public class RainbowService extends Service {
    private Context context;
    public RainbowService() {

    }

    @Override
    public void onCreate(){
        super.onCreate();

        this.context = getApplicationContext();
        RainbowSdk.instance().im().registerListener(imListener);
        RainbowSdk.instance().webRTC().registerTelephonyListener(telephonyListener);
    }

    @Override
    public void onDestroy(){
        Util.log("DESTROY SERVICE");
        try{
            RainbowSdk.instance().im().unregisterListener(imListener);
            RainbowSdk.instance().webRTC().unregisterTelephonyListener(telephonyListener);
        }catch (Exception e){}
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Util.log("BIND SERVICE");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    IRainbowImListener imListener = new IRainbowImListener() {
        @Override
        public void onImReceived(String s, IMMessage imMessage) {
            String nContent = "";
            String nTitle = "";
            Intent intent;
            PendingIntent pendingIntent;
            IRainbowContact contact;
            boolean isNotify = true;

            if(imMessage.isWebRtcEventType()){
                contact = RainbowSdk.instance().contacts().getContactFromJabberId(imMessage.getContactJid());

                CallLogPacketExtension callog = imMessage.getCallLogEvent();
                if(!callog.isAnswered()){
                    nTitle = getResources().getString(R.string.notif_missed_call_title);
                    nContent = contact.getFirstName()+" "+contact.getLastName();

                    intent = new Intent(context, ChatActivity.class);
//                intent.putExtra("CONTACT_JID", imMessage.getContactJid());
                    pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                }else{
                    isNotify = false;
                    pendingIntent = null;
                }
            }else{
                nTitle = RainbowSdk.instance().contacts().getContactFromJabberId(imMessage.getContactJid()).getFirstName();

                if(imMessage.isFileDescriptorAvailable()){
                    RainbowFileDescriptor fd = imMessage.getFileDescriptor();
                    if(fd.isAudioVideoFileType()){
                        nContent = Util.getMime(fd.getExtension()).equals("video") ? "Pesan video diterima" : "Pesan suara diterima";
                    }else if(fd.isImageType()){
                        nContent = "Gambar diterima";
                    }else{
                        nContent = "File diterima";
                    }
                    nContent = getResources().getString(R.string.file_received);
                }else{
                    nContent = imMessage.getMessageContent();
                }

                Util.tempContact = (Contact) RainbowSdk.instance().contacts().getContactFromJabberId(imMessage.getContactJid());
                intent = new Intent(context, ChatActivity.class);
                intent.putExtra("CONTACT_JID", imMessage.getContactJid());
                pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                if(nContent.length() == 0) isNotify = false;
            }

            if (imMessage.getContactJid().equals(Util.BOT_RECEPTIONIST_JABBER_ID) || imMessage.getContactJid().equals(Util.BOT_ROOM_SERVICE_JABBER_ID) || imMessage.getContactJid().equals(Util.BOT_HOUSE_KEEPING_JABBER_ID) || imMessage.getContactJid().equals(Util.BOT_RESTAURANT_JABBER_ID) || imMessage.getContactJid().equals(Util.BOT_EMERGENCY_JABBER_ID))
            {
                Contact contactTemp = (Contact)RainbowSdk.instance().contacts().getContactFromJabberId(imMessage.getContactJid());
                if (contactTemp != null)
                {
                    if (!RainbowSdk.instance().contacts().getRainbowContacts().getCopyOfDataList().contains(contactTemp))
                    {
                        isNotify = false;
                    }
                }
            }

            if(isNotify){
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(nTitle)
                        .setContentText(nContent)
                        .setAutoCancel(true)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setPriority(Notification.PRIORITY_HIGH);
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(Util.hash(s), mBuilder.build());
            }

        }

        @Override
        public void onImSent(String s, IMMessage imMessage) {

        }

        @Override
        public void isTypingState(IRainbowContact iRainbowContact, boolean b, String s) {

        }

        @Override
        public void onMessagesListUpdated(int i, String s, List<IMMessage> list) {

        }

        @Override
        public void onMoreMessagesListUpdated(int i, String s, List<IMMessage> list) {

        }
    };

    ITelephonyListener telephonyListener = new ITelephonyListener() {
        @Override
        public void onCallAdded(WebRTCCall webRTCCall) {
            Util.log("call added");
            Intent intentCall = new Intent(context, VoiceCallActivity.class);
            intentCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentCall);
        }

        @Override
        public void onCallModified(WebRTCCall webRTCCall) {
            Util.log("call modified");
        }

        @Override
        public void onCallRemoved(WebRTCCall webRTCCall) {
            Util.log("call removed");
        }
    };
}
