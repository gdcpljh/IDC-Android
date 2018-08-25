package com.id.connect.diaspora.model;

import com.ale.infra.contact.IRainbowContact;

/**
 * Created by Alcatel-Dev on 26/09/2017.
 */

public class ContactModel {
    private IRainbowContact m_contact;
    private boolean m_checked = false;

    public ContactModel(IRainbowContact contact, boolean checked){
        this.m_contact = contact;
        this.m_checked = checked;
    }

    public IRainbowContact getContact(){
        return this.m_contact;
    }

    public boolean isChecked(){
        return this.m_checked;
    }

    public void switchChecked(){
        this.m_checked = !this.m_checked;
    }

    public void setContact(IRainbowContact contact){
        this.m_contact = contact;
    }

    public void setChecked(boolean checked){
        this.m_checked = checked;
    }
}
