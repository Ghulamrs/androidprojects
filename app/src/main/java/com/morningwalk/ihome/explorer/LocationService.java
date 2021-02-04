// LocationService.java
// Version 1.1
// July 21, 2018.
// updated: November 30, 2018

package com.morningwalk.ihome.explorer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class LocationService extends Service implements LocationListener, Runnable {
    final static int OutageCount = 5;
    private UserPreferences up;
    private LocationManager locationManager;
    private Thread muploadThread;
    volatile List<PointInfo> mList = new ArrayList<PointInfo>();
    WanderAzimuth waz = WanderAzimuth.getInstance();
    public RequestQueue ghQ = null;
    boolean isFirstLocation = true;
    boolean isOutage = true;
    Database db = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        db = new Database (this);
        ghQ = GHQ.getInstance (this).getRequestQueue ();
        up = UserPreferences.Shared (getBaseContext ());

        if (up.whoami ()) {
            locationManager = (LocationManager) getApplication ().getSystemService (Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, 3000, 35, this);

            muploadThread = new Thread (LocationService.this);
            muploadThread.start ();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy ();
        if (locationManager != null) {
            locationManager.removeUpdates (this);

            mList.clear ();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double v = location.getSpeed ();
        mList.add (new PointInfo<Double> (location.getLatitude (), location.getLongitude (), location.getAltitude (), v));
        if (OutageCount <= mList.size ()) {
            int n = db.getPointsCount ();
            for (int i = 0; i < mList.size (); i++) {
                PointInfo<Double> pi = mList.get (i);
                db.insertPoint (n + i, pi);
            }
            isOutage = true;
            mList.clear ();
        }

        waz.step1 ();
        if(v > 0.1 || isFirstLocation) {
            if(isFirstLocation) isFirstLocation = false;
            float decl = getGeomagneticField(location).getDeclination();
            waz.setFloat(decl);

            Message msg = Message.obtain();
            msg.what = waz.DOWNLOAD_MESSAGE + 1;
            msg.obj = location;
            waz.msgHandler.sendMessage (msg);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent it = new Intent (Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        it.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity (it);
    }

    public void SendList(List<PointInfo> list) {
        try {
            while (list.size () > 0) {
                PointInfo<Double> pt = list.get (0);
                send_data (pt);
                list.remove (0);
                if (list.size () > 0) Thread.sleep (20);
            }
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), e.getMessage (), Toast.LENGTH_SHORT).show();
        }
    }

    public void send_data(PointInfo<Double> pt) {
        StringRequest request = new StringRequest (Request.Method.POST, getString (R.string.base_url) + getString (R.string.despatch_url), new Response.Listener<String> () {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject (response);
                    String success = json.getString ("success");
                    int result = Integer.parseInt (success);
                    if (result == 0) { // error sending data
                        makeText (getApplicationContext (), "Error: sending data !!!", LENGTH_SHORT).show ();
                    }
                } catch (JSONException e) {}
            }
        }, new Response.ErrorListener () {
            @Override
            public void onErrorResponse(VolleyError error) {
//                makeText (getApplicationContext (), "Dispatch: " + error.getMessage (), LENGTH_SHORT).show ();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<> ();
                params.put ("pid", up.getPid () + "");
                params.put ("lat", pt.getLat () + "");
                params.put ("lng", pt.getLng () + "");
                params.put ("alt", pt.getAlt () + "");
                params.put ("spd", pt.getVel () + "");
                return params;
            }
        };
        ghQ.add (request);
    }

    private GeomagneticField getGeomagneticField(Location location) {
        GeomagneticField geomagneticField = new GeomagneticField ((float) location.getLatitude (), (float) location.getLongitude (), (float) location.getAltitude (), System.currentTimeMillis ());
        return geomagneticField;
    }

    public boolean Online() {
        try {
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService (Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo ();
            return (netInfo != null && netInfo.isConnected () && netInfo.isAvailable ());
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Online: "+e.getMessage (), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (Online ()) {
                    if (isOutage) {
                        SendList (db.getAllPoints ());
                        isOutage = false;
                    } else if (mList.size () > 0) {
                        SendList (mList);
                    }
                    waz.step2();
                }
                Thread.sleep (1000);
            } catch (Exception e) {
//                Toast.makeText(getApplicationContext(), e.getMessage (), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
