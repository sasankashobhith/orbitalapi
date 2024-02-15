package com.bundee.ums.pojo;

import com.bundee.msfw.defs.*;

public class UserRequest {
    private UTF8String email;
    private int iduser;
    private UTF8String firstname;
    private UTF8String lastname;
    private int count;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    private String userIds;
    private String channelName;

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public UTF8String getEmail() {
        return email;
    }

    public void setEmailid(UTF8String emailID) {
        this.email = emailID;
    }

    public UTF8String getFirstName() {
        return firstname;
    }

    public void setFirstName(UTF8String first) {
        firstname = first;
    }

    public UTF8String getLastName() {
        return lastname;
    }

    public void setLastName(UTF8String lastName) {
        this.lastname = lastName;
    }

    public int getId() {
        return iduser;
    }

    public void setId(int Id) {
        this.iduser = Id;
    }



}
