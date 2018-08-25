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

import com.ale.infra.contact.IRainbowContact;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.Util;

import java.util.List;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class ContactSelectedAdapter extends ArrayAdapter<IRainbowContact> {
    private Context context;
    private List<IRainbowContact> data;

    public ContactSelectedAdapter(Activity context, List<IRainbowContact> contacts) {
        super(context, 0, contacts);
        data = contacts;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        IRainbowContact contact = (IRainbowContact) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_contact_select2, parent, false);
        }

        final ListContactsSelectedViewHolder viewHolder = new ListContactsSelectedViewHolder(convertView);

        String contact_name = contact.getFirstName()+" "+contact.getLastName();

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
        viewHolder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.remove(position);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
}

class ListContactsSelectedViewHolder{
    public TextView tContactName;
    public ImageView userPhoto;
    public ImageView removeBtn;

    public ListContactsSelectedViewHolder(View v){
        this.tContactName = (TextView) v.findViewById(R.id.participant_name);
        this.userPhoto = (ImageView) v.findViewById(R.id.user_photo);
        this.removeBtn = (ImageView) v.findViewById(R.id.remove_member);
    }
}