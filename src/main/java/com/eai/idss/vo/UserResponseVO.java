package com.eai.idss.vo;

import com.eai.idss.model.User;

import java.util.List;

public class UserResponseVO {
    private List<User> userList;
    private long totalRecords;

    public List<User> getUserList() {
        return userList;
    }
    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
    public long getTotalRecords() {
        return totalRecords;
    }
    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

}
