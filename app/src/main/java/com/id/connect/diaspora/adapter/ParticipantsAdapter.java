package com.id.connect.diaspora.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.Contact;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.ui.activity.ChatActivity;
import com.id.connect.diaspora.ui.activity.ContactDetailActivity;
import com.id.connect.diaspora.utils.Util;

import java.util.List;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class ParticipantsAdapter extends ArrayAdapter<RoomParticipant> {
    private Context context;
    private boolean isAdmin = false;
    private ListParticipantsViewHolder lastView = null;

    public ParticipantsAdapter(Activity context, List<RoomParticipant> participants) {
        super(context, 0, participants);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RoomParticipant participant = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_participants, parent, false);
        }

        final ListParticipantsViewHolder viewHolder = new ListParticipantsViewHolder(convertView);

        final Contact contact = participant.getContact();

        String participant_name = contact.getFirstName()+" "+contact.getLastName();
        String partipant_status = Util.presenceStatus(contact.getPresence().getPresence());

        Bitmap bmp = contact.getPhoto();
        if(bmp != null){
            viewHolder.participantPhoto.setImageBitmap(Util.getRoundedCornerBitmap(bmp, bmp.getWidth()));
        }else{
            String initName = "?";
            try{
                initName = contact.getFirstName().substring(0,1)+contact.getLastName().substring(0,1);
            }catch (Exception e){}
            viewHolder.participantPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(participant_name)));
        }

        viewHolder.tParticipantName.setText(participant_name);
        viewHolder.tParticipantStatus.setText(partipant_status);

        final String contactJid = contact.getImJabberId();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(contact.getContactId() != RainbowSdk.instance().myProfile().getConnectedUser().getContactId()){
                    Util.tempContact = contact;
                    Intent intent = new Intent(context, ChatActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        if(contact.getContactId() == RainbowSdk.instance().myProfile().getConnectedUser().getContactId()){
            viewHolder.participantDetail.setVisibility(View.GONE);
        }
        viewHolder.participantDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(contact.getContactId() != RainbowSdk.instance().myProfile().getConnectedUser().getContactId()) {
                    Util.tempContact = (Contact) contact;
                    Intent intent = new Intent(context, ContactDetailActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        if(isAdmin && ! contact.getCorporateId().equals(RainbowSdk.instance().myProfile().getConnectedUser().getCorporateId())){
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    viewHolder.participantDelete.setVisibility(View.VISIBLE);
                    if(lastView != null){
                        lastView.participantDelete.setVisibility(View.GONE);
                    }
                    lastView = viewHolder;
                    return true;
                }
            });

            viewHolder.participantDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(getContext().getString(R.string.confirmation))
                            .setMessage(getContext().getString(R.string.confirm_delete_user))
                            .setIcon(R.drawable.ic_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    RainbowSdk.instance().bubbles().deleteParticipantFromBubble(Util.tempRoom, contact, new IRoomProxy.IDeleteParticipantListener() {
                                        @Override
                                        public void onDeleteParticipantSuccess(String s, String s1) {
                                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    notifyDataSetChanged();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onDeleteParticipantFailure() {}
                                    });
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });
        }

        return convertView;
    }

    public void setAdmin(boolean _isAdmin){
        this.isAdmin = _isAdmin;
    }
}

class ListParticipantsViewHolder{
    public TextView tParticipantName;
    public TextView tParticipantStatus;
    public ImageView participantPhoto;
    public ImageView participantDetail;
    public ImageView participantDelete;

    public ListParticipantsViewHolder(View v){
        this.tParticipantName = (TextView) v.findViewById(R.id.participant_name);
        this.tParticipantStatus= (TextView) v.findViewById(R.id.participant_status);
        this.participantPhoto = (ImageView) v.findViewById(R.id.participant_photo);
        this.participantDetail = (ImageView) v.findViewById(R.id.participant_detail);
        this.participantDelete = (ImageView) v.findViewById(R.id.participant_delete);
    }
}