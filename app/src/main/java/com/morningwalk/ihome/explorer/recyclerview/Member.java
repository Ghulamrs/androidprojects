package com.morningwalk.ihome.explorer.recyclerview;
// G. R. Akhtar, April 27, 2020

import android.os.Parcel;
import android.os.Parcelable;

public class Member implements Parcelable {
    private int id;
    private String name;
    private int status;

    public Member(int id, String name, int status) {
        assert(id > 0);
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        assert(id > 0);
        this.id = id;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

//  Following part is the requirement of recyclerview implementation
    protected Member(Parcel in) {
        int id = in.readInt();
        String name = in.readString();
        int status = in.readInt();
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(0);
        dest.writeString("username");
        dest.writeInt(-1);
    }
}
