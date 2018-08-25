package com.id.connect.diaspora.model;

import com.ale.infra.manager.room.Room;

/**
 * Created by Alcatel-Dev on 26/09/2017.
 */

public class RoomModel {
    private Room m_room;
    private boolean m_checked = false;

    public RoomModel(Room room, boolean checked){
        this.m_room = room;
        this.m_checked = checked;
    }

    public Room getRoom(){
        return this.m_room;
    }

    public boolean isChecked(){
        return this.m_checked;
    }

    public void switchChecked(){
        this.m_checked = !this.m_checked;
    }

    public void setBubble(Room room){
        this.m_room = room;
    }

    public void setChecked(boolean checked){
        this.m_checked = checked;
    }
}
