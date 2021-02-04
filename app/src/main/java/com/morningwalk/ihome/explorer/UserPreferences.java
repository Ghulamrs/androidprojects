// UserPreferences.java
// Version 1.1
// July 26, 2018.

package com.morningwalk.ihome.explorer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
// Zero Point(Islamabad Highway/Kashmir Highway Interchange - crossing), Islamabad, Pakistan
//double  latitude=33.6938, longitude=73.0652; // Zero point, Islamabad
//double  latitude=24.875503, longitude=67.041078; // Karachi (Mazar-e-Quaid)
//double  latitude = 30.40, longitude = 70.635; // Middle Point of Pakistan
//double  latitude=35.15703, longitude=76.3456; // Khaplu, Gilgit-Baltastan

public class UserPreferences {
    private SharedPreferences m_sp;
    private static UserPreferences object = null;
    public static final float DEFAULT_ZOOM = 5.0f;
    private String name;
    private int pid;

    private float lat  = 30.40f;  // Middle Point of Pakistan
    private float lng  = 70.635f; // Middle Point of Pakistan
    private float bearing = 0f;
    private float tilt =  0.0f;
    private float zoom = DEFAULT_ZOOM;
    CameraPosition cp;

    public static UserPreferences Shared(Context ctx) {
        if (object == null) object = new UserPreferences(ctx);
        return object;
    }

    private UserPreferences(Context ctx) {
        m_sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        pid  = m_sp.getInt("pid", 0);
        name = m_sp.getString("pname", "");
        lat  = getLatitude();
        lng  = getLongitude();
        bearing = getBearing();
        tilt = getTilt();
        zoom = getZoom();
    }

    public void Save(int Id, String Name) { //  called in LoginActivity
        SharedPreferences.Editor ed = m_sp.edit();
        ed.putString("pname", Name);
        ed.putInt("pid", Id);
        ed.putFloat ("lat", lat);
        ed.putFloat ("lng", lng);
        ed.putFloat ("bearing", bearing);
        ed.putFloat ("tilt", tilt);
        ed.putFloat ("zoom", zoom);
        ed.commit();
    }

    public boolean isName(String str) {
        return name.compareTo(str)==0;
    }
    public float getLatitude() { return m_sp.getFloat ("lat", lat); }
    public float getLongitude() { return m_sp.getFloat ("lng", lng); }
    public float getZoom() { return m_sp.getFloat ("zoom", DEFAULT_ZOOM); }
    public float getBearing() { return m_sp.getFloat ("bearing", 0.0f); }
    public float getTilt() { return m_sp.getFloat ("tilt", 40.0f); }
    public LatLng whereami() {
        return new LatLng((double)getLatitude (), (double)getLongitude ());
    }

    public boolean isPid(int pd) {
        if(pd==pid) return true;
        return false;
    }

    public int getPid() { return pid; }
    public String getName() { return name; }

    public boolean whoami() {
        this.pid = m_sp.getInt("pid", 0);
        if(this.pid > 0) {
            this.name = m_sp.getString("pname", "nill");
            return true;
        }

        return false;
    }

    public void SaveCameraOptions(LatLng lm, CameraPosition cp) {  // called in MapsActivity
        SharedPreferences.Editor ed = m_sp.edit();
        lat = (float)lm.latitude;
        lng = (float)lm.longitude;
        zoom = cp.zoom;
        tilt = cp.tilt;
        bearing = cp.bearing;
        ed.putFloat ("lat", lat);
        ed.putFloat ("lng", lng);
        ed.putFloat ("zoom", zoom);
        ed.putFloat ("bearing", bearing);
        ed.putFloat ("tilt", tilt);
        ed.commit();
    }
}
