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

import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.manager.room.RoomStatus;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.ui.activity.ChatRoomActivity;
import com.id.connect.diaspora.ui.activity.RoomDetailActivity;
import com.id.connect.diaspora.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class RoomsAdapter extends ArrayAdapter<Room> {
    private Context context;
    private ListRoomsViewHolder lastView = null;

    public RoomsAdapter(Activity context, List<Room> rooms) {
        super(context, 0, rooms);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Room room = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_rooms, parent, false);
        }

        final ListRoomsViewHolder viewHolder = new ListRoomsViewHolder(convertView);

        int pNumber = 0;
        for(RoomParticipant p : room.getParticipants().getCopyOfDataList()){
            if(p.getStatus().equals(RoomStatus.ACCEPTED)){
                pNumber++;
            }
        }
        String roomName = room.getName();
        String roomInfo = String.valueOf(pNumber)+" "+getContext().getString(R.string.members);

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
            viewHolder.roomPhoto.setImageBitmap(Util.getRoundedCornerBitmap(roomBmp, roomBmp.getWidth()));
        }else{
            String initName = "?";
            try{
                initName = roomName.substring(0,1).toUpperCase();
            }catch (Exception e){}
            viewHolder.roomPhoto.setImageDrawable(TextDrawable.builder().buildRound(initName, Util.generateColor(roomName)));
        }
        //-----------

        viewHolder.tRoomName.setText(roomName);
        viewHolder.tRoomInfo.setText(roomInfo);

        final String roomJid = room.getJid();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.tempRoom = room;
                Intent intent = new Intent(context, ChatRoomActivity.class);
                context.startActivity(intent);
            }
        });

        viewHolder.roomDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.tempRoom = room;
                Intent intent = new Intent(context, RoomDetailActivity.class);
                context.startActivity(intent);
            }
        });
//
//        convertView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                return false;
//
//                if( ! room.getOwner().getContactId().equals(RainbowSdk.instance().myProfile().getConnectedUser().getContactId()) ){
//                    viewHolder.roomLeave.setVisibility(View.VISIBLE);
//                    if(lastView != null){
//                        lastView.roomLeave.setVisibility(View.GONE);
//                    }
//                    lastView = viewHolder;
//
//                    return true;
//                }else return false;
//            }
//        });

        viewHolder.roomLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getContext().getString(R.string.confirmation))
                        .setMessage(getContext().getString(R.string.confirm_leave_group))
                        .setIcon(R.drawable.ic_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //leave group

                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        return convertView;
    }
}

class ListRoomsViewHolder{
    public TextView tRoomName;
    public TextView tRoomInfo;
    public ImageView roomPhoto;
    public ImageView roomDetail;
    public ImageView roomLeave;

    public ListRoomsViewHolder(View v){
        this.tRoomName = (TextView) v.findViewById(R.id.participant_name);
        this.tRoomInfo = (TextView) v.findViewById(R.id.room_info);
        this.roomPhoto = (ImageView) v.findViewById(R.id.room_photo);
        this.roomDetail = (ImageView) v.findViewById(R.id.room_detail);
        this.roomLeave = (ImageView) v.findViewById(R.id.room_leave);
    }
}