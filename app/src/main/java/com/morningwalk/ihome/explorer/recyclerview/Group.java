package com.morningwalk.ihome.explorer.recyclerview;
// G. R. Akhtar, April 27, 2020
// updated April 30, 2020

import android.os.Parcelable;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class Group extends ExpandableGroup<Member> {
    private String admin;
    public Group(String title, String admin, List<Member> items) {
        super(title, items);
        this.admin = admin;
    }
    public String getAdmin() {
        return admin;
    }
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    public int getAdminId() {
        for(Member member:getItems()) {
            if(admin.equals(member.getName())) return member.getId();
        }
        return -1;
    }
}
