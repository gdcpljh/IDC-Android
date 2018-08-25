package com.id.connect.diaspora.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.IRainbowContact;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.model.ContactModel;
import com.id.connect.diaspora.utils.Util;

import java.util.List;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class ContactSelectAdapter extends ArrayAdapter<ContactModel> {
    private Context context;

    public ContactSelectAdapter(Activity context, List<ContactModel> contacts) {
        super(context, 0, contacts);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ContactModel iContact = (ContactModel) getItem(position);
        IRainbowContact contact = iContact.getContact();

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_contact_select, parent, false);
        }

        final ListContactsSelectViewHolder viewHolder = new ListContactsSelectViewHolder(convertView);

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

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iContact.switchChecked();
                viewHolder.checkBox.setChecked(iContact.isChecked());
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iContact.switchChecked();
                viewHolder.checkBox.setChecked(iContact.isChecked());
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}

class ListContactsSelectViewHolder{
    public TextView tContactName;
    public ImageView userPhoto;
    public CheckBox checkBox;

    public ListContactsSelectViewHolder(View v){
        this.tContactName = (TextView) v.findViewById(R.id.participant_name);
        this.userPhoto = (ImageView) v.findViewById(R.id.user_photo);
        this.checkBox = (CheckBox) v.findViewById(R.id.checkBox);
    }
}