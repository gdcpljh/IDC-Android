package com.id.connect.diaspora.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.invitation.IInvitationListener;
import com.ale.infra.invitation.Invitation;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.Util;

import java.util.List;

/**
 * Created by Alcatel-Dev on 25/09/2017.
 */

public class InvitationSentAdapter extends ArrayAdapter<IRainbowContact> {
    private Context context;

    public InvitationSentAdapter(Activity context, List<IRainbowContact> contacts) {
        super(context, 0, contacts);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IRainbowContact contact = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_sent_invitations, parent, false);
        }

        final ListInvitationSentViewHolder viewHolder = new ListInvitationSentViewHolder(convertView);
        String contact_name = contact.getFirstName()+" "+contact.getLastName();

        Bitmap bmp = contact.getPhoto();
        if(bmp != null){
            viewHolder.userPhoto.setImageBitmap(Util.getRoundedCornerBitmap(contact.getPhoto(), 50));
        }else{
            String initName = "?";
            try{
                initName = contact.getFirstName().substring(0,1)+contact.getLastName().substring(0,1);
            }catch (Exception e){}
            viewHolder.userPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Color.parseColor("#F26722")));
        }

        viewHolder.tContactName.setText(contact_name);

        final String invitationId = contact.getInvitationId();
        Util.log("invitationId "+invitationId);
        final Invitation invitation = RainbowSdk.instance().contacts().getInvitationById(invitationId);
        try{
            viewHolder.tInvitationsDate.setText(Util.txtDate(invitation.getInvitingDate()));
        }catch (Exception e){
            Util.log(e.toString());
        }

        try{
            invitation.registerChangeListener(new IInvitationListener() {
                @Override
                public void invitationUpdated(final Invitation _invitation) {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                viewHolder.tInvitationsDate.setText(Util.txtDate(_invitation.getInvitingDate()));
                            }catch (Exception e){
                                Util.log(e.toString());
                            }
                        }
                    });
                }

                @Override
                public void onActionInProgress(boolean b) {}
            });

            viewHolder.cancelInvitations.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.log("CANCEL !!!!");
                        }
                    });

                    invitation.setSendingType(Invitation.SendingType.RECEIVED);
                }
            });
        }catch (Exception e){}

        return convertView;
    }
}

class ListInvitationSentViewHolder{
    public TextView tContactName;
    public TextView tInvitationsDate;
    public ImageView userPhoto;
    public ImageView cancelInvitations;

    public ListInvitationSentViewHolder(View v){
        this.tContactName = (TextView) v.findViewById(R.id.participant_name);
        this.tInvitationsDate= (TextView) v.findViewById(R.id.invitations_date);
        this.userPhoto = (ImageView) v.findViewById(R.id.user_photo);
        this.cancelInvitations = (ImageView) v.findViewById(R.id.cancel_invitations);
    }
}