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

import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.model.RoomModel;
import com.id.connect.diaspora.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class RoomSelectAdapter extends ArrayAdapter<RoomModel> {
    private Context context;

    public RoomSelectAdapter(Activity context, List<RoomModel> room) {
        super(context, 0, room);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RoomModel iRoom = (RoomModel) getItem(position);
        Room room = iRoom.getRoom();

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_room_select, parent, false);
        }

        final ListRoomsSelectViewHolder viewHolder = new ListRoomsSelectViewHolder(convertView);

        String roomName = room.getName();
        String roomInfo = room.getParticipants().getCount()+" "+getContext().getString(R.string.members);

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

            if(bmps.size() > 1){
                roomBmp = Util.mergeMultiple(bmps.toArray(new Bitmap[bmps.size()]));
            }
        }

        if(roomBmp != null){
            viewHolder.roomPhoto.setImageBitmap(Util.getRoundedCornerBitmap(roomBmp, roomBmp.getWidth()));
        }else{
            String initName = "?";
            try{
                initName = room.getName().substring(0,1).toUpperCase();
            }catch (Exception e){}
            viewHolder.roomPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(roomName)));
        }
        viewHolder.tRoomName.setText(roomName);
        viewHolder.tRoomInfo.setText(roomInfo);

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iRoom.switchChecked();
                viewHolder.checkBox.setChecked(iRoom.isChecked());
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iRoom.switchChecked();
                viewHolder.checkBox.setChecked(iRoom.isChecked());
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}

class ListRoomsSelectViewHolder{
    public TextView tRoomName;
    public TextView tRoomInfo;
    public ImageView roomPhoto;
    public CheckBox checkBox;

    public ListRoomsSelectViewHolder(View v){
        this.tRoomName = (TextView) v.findViewById(R.id.participant_name);
        this.roomPhoto = (ImageView) v.findViewById(R.id.room_photo);
        this.tRoomInfo = (TextView) v.findViewById(R.id.room_info);
        this.checkBox = (CheckBox) v.findViewById(R.id.checkBox);
    }
}