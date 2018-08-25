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

import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.manager.room.RoomStatus;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alcatel-Dev on 25/09/2017.
 */

public class InvitationRoomRcvAdapter extends ArrayAdapter<Room> {
    private Context context;

    public InvitationRoomRcvAdapter(Activity context, List<Room> rooms) {
        super(context, 0, rooms);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Room room = (Room) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_room_rcv_invitations, parent, false);
        }

        final ListInvitationRoomRcvViewHolder viewHolder = new ListInvitationRoomRcvViewHolder(convertView);


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

        viewHolder.acceptInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RainbowSdk.instance().bubbles().acceptInvitation(room, new IRoomProxy.IChangeUserRoomDataListener() {
                    @Override
                    public void onChangeUserRoomDataSuccess(RoomParticipant roomParticipant) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onChangeUserRoomDataFailed() {}
                });
            }
        });

        viewHolder.declineInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RainbowSdk.instance().bubbles().rejectInvitation(room, new IRoomProxy.IChangeUserRoomDataListener() {
                    @Override
                    public void onChangeUserRoomDataSuccess(RoomParticipant roomParticipant) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onChangeUserRoomDataFailed() {}
                });

            }
        });

        return convertView;
    }
}

class ListInvitationRoomRcvViewHolder{
    public TextView tRoomName;
    public TextView tRoomInfo;
    public ImageView roomPhoto;
    public ImageView acceptInvitations;
    public ImageView declineInvitations;

    public ListInvitationRoomRcvViewHolder(View v){
        this.tRoomName = (TextView) v.findViewById(R.id.room_name);
        this.tRoomInfo = (TextView) v.findViewById(R.id.room_info);
        this.roomPhoto = (ImageView) v.findViewById(R.id.room_photo);
        this.acceptInvitations= (ImageView) v.findViewById(R.id.accept_invitations);
        this.declineInvitations = (ImageView) v.findViewById(R.id.decline_invitations);
    }
}