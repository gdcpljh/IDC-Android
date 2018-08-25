package com.id.connect.diaspora.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.IContact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.listener.IRainbowSentInvitationListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.AvatarStream;
import com.id.connect.diaspora.utils.Util;

import java.util.List;

/**
 * Created by Alcatel-Dev on 20/09/2017.
 */

public class ContactSearchAdapter extends ArrayAdapter {
    private Context context;

    public ContactSearchAdapter(Activity context, List<IContact> contacts) {
        super(context, 0, contacts);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final IContact iContact = (IContact) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_contact_search, parent, false);
        }
        
        final ListContactSearchViewHolder viewHolder = new ListContactSearchViewHolder(convertView);
        final String contact_name = iContact.getFirstName()+" "+iContact.getLastName();

        Bitmap bmp = iContact.getPhoto();
        if(bmp != null){
            viewHolder.userPhoto.setImageBitmap(Util.getRoundedCornerBitmap(bmp, 50));
        }else{
            String initName = "?";
            try{
                initName = iContact.getFirstName().substring(0,1).toUpperCase() + iContact.getLastName().substring(0,1).toUpperCase();
            }catch (Exception e){}
            viewHolder.userPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(contact_name)));

            new Thread(new AvatarStream(RainbowSdk.instance().contacts().getAvatarUrl(iContact.getCorporateId()), new AvatarStream.Callback() {
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

        viewHolder.tContactName.setText(contact_name);

        //CHECK If INVITED
        boolean addBtnVisible = true;
        for(IRainbowContact contactInvited : RainbowSdk.instance().contacts().getPendingSentInvitations()){
            if(contactInvited.getCorporateId().equals( iContact.getCorporateId())){
                addBtnVisible = false;
                viewHolder.pendingStatus.setVisibility(View.VISIBLE);
                break;
            }
        }
        //CHECK IF IN CONTACTS
        for(IRainbowContact contactFriend : RainbowSdk.instance().contacts().getRainbowContacts().getCopyOfDataList()){
            if(contactFriend.getCorporateId().equals(iContact.getCorporateId())){
                addBtnVisible = false;
                break;
            }
        }
        if(addBtnVisible) viewHolder.contactAdd.setVisibility(View.VISIBLE);

        final String corporateId = iContact.getCorporateId();
        final String mailAddress = iContact.getMainEmailAddress();
        viewHolder.contactAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.contactAdd.setVisibility(View.GONE);
                    }
                });
                RainbowSdk.instance().contacts().addRainbowContactToRoster(corporateId, new IRainbowSentInvitationListener() {
                    @Override
                    public void onInvitationSentSuccess() {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onInvitationSentError(RainbowServiceException e) {

                    }

                    @Override
                    public void onInvitationError() {

                    }
                });
            }
        });

        return convertView;
    }
}

class ListContactSearchViewHolder{
    public TextView tContactName;
    public ImageView userPhoto;
    public ImageView contactAdd;
    public TextView pendingStatus;

    public ListContactSearchViewHolder(View v){
        this.tContactName = (TextView) v.findViewById(R.id.participant_name);
        this.userPhoto = (ImageView) v.findViewById(R.id.user_photo);
        this.contactAdd = (ImageView) v.findViewById(R.id.contact_add);
        this.pendingStatus = (TextView) v.findViewById(R.id.pending_label);
    }
}