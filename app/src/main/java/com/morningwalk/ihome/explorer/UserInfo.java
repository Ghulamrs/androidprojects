// MemberInfo.java
// Version 1.1
// July 21, 2018.

package com.morningwalk.ihome.explorer;

public class UserInfo {
    int id;
    String name;
    double lat;
    double lng;

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setID(int _id) {
        id = _id;
    }

    public void setName(String _name) {
        name = _name;
    }

    public void setLat(double _lat) {
        lat = _lat;
    }

    public void setLng(double _lng) {
        lng = _lng;
    }
}
