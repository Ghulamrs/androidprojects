// WanderAzimuth.java
// Version 3.65
// November 23, 2018.

// Shares azimuth between LocationService and MapsAcivity classes

package com.morningwalk.ihome.explorer;

import android.os.Handler;
import android.os.Message;

class WanderAzimuth {
    private static WanderAzimuth instance;

    // Global variable
    Handler msgHandler;
    private int count = 0;
    private float data = 0.0f;
    public static final int DOWNLOAD_MESSAGE = 1;

    // Restrict the constructor from being instantiated
    private WanderAzimuth(){}
    public void step1() { if(count==0) count=1; }
    public void step2() { if(count==1) count=2; }
    public void step3() { if(count==2) count=3; }
    public void step4() { if(count==3) count=4; }
    public boolean instep() {  return count==3; }
    public void setFloat(float d) {this.data=d; }
    public float getFloat() { return this.data; }

    public static synchronized WanderAzimuth getInstance() {
        if(instance==null) {
            instance = new WanderAzimuth();
        }
        return instance;
    }
}
