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
import com.ale.infra.contact.IRainbowContact;
import com.ale.listener.IRainbowContactManagementListener;
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

public class ContactsAdapter extends ArrayAdapter<IRainbowContact> {
    private Context context;
    private ListContactsViewHolder lastView = null;

    public ContactsAdapter(Activity context, List<IRainbowContact> contacts) {
        super(context, 0, contacts);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final IRainbowContact contact = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_contacts, parent, false);
        }

        final ListContactsViewHolder viewHolder = new ListContactsViewHolder(convertView);

        String contact_name = contact.getFirstName()+" "+contact.getLastName();
        String contact_status = Util.presenceStatus(contact.getPresence().getPresence());

        Bitmap bmp = contact.getPhoto();
        if(bmp != null){
            viewHolder.userPhoto.setImageBitmap(Util.getRoundedCornerBitmap(bmp, bmp.getWidth()));
        }else{
            String initName = "?";
            try{
                initName = contact.getFirstName().substring(0,1).toUpperCase() + contact.getLastName().substring(0,1).toUpperCase();
            }catch (Exception e){}
            viewHolder.userPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(contact_name)));
        }

        viewHolder.tContactName.setText(contact_name);
        viewHolder.tUserStatus.setText(contact_status);

        final String contactJid = contact.getImJabberId();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.tempContact = (Contact) contact;
                Intent intent = new Intent(context, ChatActivity.class);
                context.startActivity(intent);
            }
        });

        if( ! contact.getImJabberId().equals(Util.BOT_JABBER_ID)){
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    viewHolder.contactDelete.setVisibility(View.VISIBLE);
                    if(lastView != null){
                        lastView.contactDelete.setVisibility(View.GONE);
                    }
                    lastView = viewHolder;
                    return true;
                }
            });
        }

        viewHolder.contactDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.tempContact = (Contact) contact;
                Intent intent = new Intent(context, ContactDetailActivity.class);
                context.startActivity(intent);
            }
        });

        final String contactMail = contact.getMainEmailAddress();
        viewHolder.contactDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getContext().getString(R.string.confirmation))
                        .setMessage(getContext().getString(R.string.confirm_delete_user))
                        .setIcon(R.drawable.ic_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //delete user
                                RainbowSdk.instance().contacts().removeContactFromRoster(contactJid, new IRainbowContactManagementListener() {

                                    @Override
                                    public void onContactRemoveSuccess() {
                                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                lastView.contactDelete.setVisibility(View.GONE);
                                                lastView = null;
                                            }
                                        });
                                    }

                                    @Override
                                    public void onContactRemovedError(Exception e) {}
                                });
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        return convertView;
    }
}

class ListContactsViewHolder{
    public TextView tContactName;
    public TextView tUserStatus;
    public ImageView userPhoto;
    public ImageView contactDetail;
    public ImageView contactDelete;

    public ListContactsViewHolder(View v){
        this.tContactName = (TextView) v.findViewById(R.id.participant_name);
        this.tUserStatus= (TextView) v.findViewById(R.id.user_status);
        this.userPhoto = (ImageView) v.findViewById(R.id.user_photo);
        this.contactDetail= (ImageView) v.findViewById(R.id.contact_detail);
        this.contactDelete = (ImageView) v.findViewById(R.id.contact_delete);
    }
}