package com.id.connect.diaspora.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.ui.activity.ChatActivity;
import com.id.connect.diaspora.ui.activity.ChatRoomActivity;
import com.id.connect.diaspora.utils.AvatarStream;
import com.id.connect.diaspora.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class ConversationsAdapter extends ArrayAdapter<IRainbowConversation> {
    private Context context;

    public ConversationsAdapter(Activity context, List<IRainbowConversation> conversations) {
        super(context, 0, conversations);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final IRainbowConversation conversation = getItem(position);
        Util.log("Adapter Conversation Run");
        IMMessage last_msg = conversation.getLastMessage();

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_conversations, parent, false);
        }

        final ListConversationsViewHolder viewHolder = new ListConversationsViewHolder(convertView);

        String contact_name = convertView.getResources().getString(R.string.loading_text);
        String last_msg_content = convertView.getResources().getString(R.string.loading_text);
        String last_msg_date = "";

        //GET PHOTO AND NAME
        if(conversation.isRoomType()){
            Room room = conversation.getRoom();

            if(room != null){
                contact_name=room.getName();

                //init photo
                Bitmap roomBmp = null;

                List<RoomParticipant> participants = room.getParticipants().getCopyOfDataList();
                ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();
                for(int i=0; i<participants.size(); i++){
                    try{
                        Bitmap bmp = participants.get(i).getContact().getPhoto();
                        if(bmp != null){
                            bmps.add(bmp);
                            if(bmps.size() >= 5) break;
                        }
                    }catch (Exception e){}
                }

                if(bmps.size() > 1){
                    roomBmp = Util.mergeMultiple(bmps.toArray(new Bitmap[bmps.size()]));
                    viewHolder.userPhoto.setImageBitmap(Util.getRoundedCornerBitmap(roomBmp, roomBmp.getWidth()));
                    viewHolder.userPhoto.setBackgroundResource(R.drawable.circle);
                }else{
                    String initName = "?";
                    try{
                        initName = contact_name.substring(0,1).toUpperCase();
                    }catch (Exception e){}
                    viewHolder.userPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(contact_name)));
                }
            }
        }else{
            IRainbowContact contact = conversation.getContact();

            if(contact != null){
                contact_name = contact.getFirstName()+" "+contact.getLastName();

                Bitmap bmp = contact.getPhoto();
                if(bmp != null){
                    viewHolder.userPhoto.setImageBitmap(Util.getRoundedCornerBitmap(bmp, bmp.getWidth() ));
                }else{
                    String initName = "?";
                    try{
                        initName = contact.getFirstName().substring(0,1).toUpperCase() + contact.getLastName().substring(0,1).toUpperCase();
                    }catch (Exception e){
                        Util.log(e.toString());
                    }
                    viewHolder.userPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(contact_name)));

                    new Thread(new AvatarStream(RainbowSdk.instance().contacts().getAvatarUrl(contact.getCorporateId()), new AvatarStream.Callback() {
                        @Override
                        public void onFinish(final Bitmap bitmap) {
                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewHolder.userPhoto.setImageBitmap(Util.getRoundedCornerBitmap(bitmap, 50));
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable t) {}
                    })).start();

                }
            }
        }
        //END GET PHOTO AND NAME

        if(last_msg.isEventType()){
            if(last_msg.isWebRtcEventType()){
                CallLogPacketExtension callog = last_msg.getCallLogEvent();
//                last_msg_content = callog.getState();

                if(!callog.getCallerJid().equals(last_msg.getContactJid())){
                    last_msg_content = callog.getState().equals("answered")
                            ? context.getString(R.string.answered_call)
                            : context.getString(R.string.missed_call);
                }else{
                    last_msg_content = callog.getState().equals("answered")
                            ? context.getString(R.string.answered_call)
                            : context.getString(R.string.missed_call);
                }
            }else{

            }
        }else{
            last_msg_content = last_msg.getMessageContent();
            if(last_msg_content != null){
                if (!last_msg_content.startsWith("WELCOMING_MESSAGE_ALE_HOTEL"))
                {
                    if(last_msg_content.length() > 30) last_msg_content = last_msg_content.substring(0, 30)+"..";
                }
                else
                {
                    last_msg_content = "";
                }

            }

            if(last_msg.isFileDescriptorAvailable()){
                last_msg_content = context.getString(R.string.file_received);
            }
        }

        if(!conversation.isRoomType()){
            int counter = conversation.getUnreadMsgNb();

            if(counter > 0){
                viewHolder.unread.setText(String.valueOf(counter));
                viewHolder.unread.setVisibility(View.VISIBLE);
            }else{
                viewHolder.unread.setText(String.valueOf(counter));
                viewHolder.unread.setVisibility(View.GONE);
            }
        }

        try{
            last_msg_date = Util.txtDate(last_msg.getMessageDate()).toString();
        }catch (Exception e){}

        viewHolder.tContactName.setText(contact_name);
        viewHolder.tLastMessage.setText(last_msg_content);
        viewHolder.tMessageDate.setText(last_msg_date);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(conversation.isRoomType()){
                    Util.tempRoom = conversation.getRoom();
                    Intent intent = new Intent(context, ChatRoomActivity.class);
                    context.startActivity(intent);
                }else{
                    Util.tempContact = (Contact) conversation.getContact();
                    Intent intent = new Intent(context, ChatActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        return convertView;
    }
}

class ListConversationsViewHolder{
    public TextView tContactName;
    public TextView tLastMessage;
    public TextView tMessageDate;
    public ImageView userPhoto;
    public TextView unread;
    public LinearLayout itemWrap;

    public ListConversationsViewHolder(View v){
        this.tContactName = (TextView) v.findViewById(R.id.contact_name);
        this.tLastMessage = (TextView) v.findViewById(R.id.last_message);
        this.tMessageDate = (TextView) v.findViewById(R.id.content_date);
        this.userPhoto = (ImageView) v.findViewById(R.id.user_photo);
        this.unread = (TextView) v.findViewById(R.id.unread);
    }
}