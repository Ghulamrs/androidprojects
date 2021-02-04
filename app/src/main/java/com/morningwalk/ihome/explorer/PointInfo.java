// PointInfo.java
// Version 2.1
// August 24, 2018.

// Version 3.6, template update<T>
// November 20, 2018

package com.morningwalk.ihome.explorer;

public class PointInfo<T> {
    private T lat;
    private T lng;
    private T alt;
    private T vel;

    public PointInfo(T lat, T lng, T alt, T vel) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.vel = vel;
    }

    public T getLat()
        {
            return lat;
        }
    public T getLng()
        {
            return lng;
        }
    public T getAlt()
        {
            return alt;
        }
    public T getVel()
        {
            return vel;
        }

    public void setLat(T _lat)
        {
            lat=_lat;
        }
    public void setLng(T _lng)
        {
            lng=_lng;
        }
    public void setAlt(T _alt)
        {
            alt=_alt;
        }
    public void setVel(T _vel)
        {
            vel=_vel;
        }
}
