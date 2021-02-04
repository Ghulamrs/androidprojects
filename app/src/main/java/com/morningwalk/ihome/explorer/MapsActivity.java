// MapsActivity.java
// Version 1.1
// July 21, 2018.

package com.morningwalk.ihome.explorer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import com.morningwalk.ihome.explorer.recyclerview.GroupActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.Toast.*;
import static android.widget.Toast.makeText;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Runnable, GoogleMap.OnMarkerClickListener  {

    private GoogleMap mMap;
    private Thread mdownloadThread;

    LatLng  lm;// = new LatLng (33.6938, 73.0652); // Zero point, Islamabad
    CameraPosition cameraPosition;

    ArrayList<UserInfo> memberList = new ArrayList<UserInfo>();
    ArrayList<UserInfo> nameList = new ArrayList<UserInfo>();
    public UserPreferences up = UserPreferences.Shared(getBaseContext());
    public RequestQueue ghQ = null;
    public int index = 1;

    public Hashtable<Integer,Marker> markers;
    boolean bMarkersInitialized = false;

    int  tid = 0;
    float zoom = UserPreferences.DEFAULT_ZOOM;
    WanderAzimuth waz = WanderAzimuth.getInstance();
    com.morningwalk.ihome.explorer.recyclerview.Messages xMessage;
    RelativeLayout relativeLayout = null; // used in groupButtonCreate
    Button groupButton = null; // used in groupButtonCreate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ghQ = GHQ.getInstance(this).getRequestQueue();
        groupButtonCreate(); // added on May 9, 2020
        up = UserPreferences.Shared(getBaseContext());
        up.whoami();
        tid = up.getPid ();
        lm = up.whereami ();
        zoom = up.getZoom ();
        waz.setFloat (up.getBearing ());
        cameraPosition = new CameraPosition.Builder().target(lm).zoom(zoom).bearing(up.getBearing ()).tilt(up.getTilt ()).build();

        if (!runtime_permissions()) { // you already have these permissions
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            enable_service();

            mdownloadThread = new Thread(MapsActivity.this);
            mdownloadThread.start();
        }

        waz.msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == waz.DOWNLOAD_MESSAGE) {
                    if(!bMarkersInitialized) InitializeMarkers();

                    for (int i = 0; i < memberList.size (); i++) {
                        UserInfo mi = memberList.get (i);
                        LatLng ll = new LatLng (mi.getLat (), mi.getLng ());
                        MarkerAnimation.animateMarkerToHC(markers.get (mi.id), ll, new LatLngInterpolator.Spherical());
                        if (mi.id == tid) lm = ll;
                    }
                }
                else if(msg.what == waz.DOWNLOAD_MESSAGE+1) {
                    Location location = (Location)msg.obj;
                    lm = new LatLng(location.getLatitude(), location.getLongitude());
                    float bearing = location.getBearing() + waz.getFloat(); // add declin.

                    CameraPosition cp = mMap.getCameraPosition ();
                    cameraPosition = new CameraPosition.Builder ().target (lm).bearing (bearing).zoom(cp.zoom).tilt (cp.tilt).build ();
                    mMap.animateCamera (CameraUpdateFactory.newCameraPosition (cameraPosition));
                    makeText (getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
                }

                if(waz.instep()) {
                    makeText (getApplicationContext(), getString (R.string.title_activity_maps), Toast.LENGTH_LONG).show();
                    waz.step4 (); // done with initialization
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        up.SaveCameraOptions (lm, mMap.getCameraPosition()); // also save zoom
    }

    boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_service ();

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ().findFragmentById (R.id.map);
                mapFragment.getMapAsync (this);
            }
        }
    }

    void enable_service() {
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        startService(i);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType (GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled (true);
        mMap.setOnMarkerClickListener (this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.addMarker (new MarkerOptions ().position (lm).title (up.getName()).icon (
                BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN)));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                makeText(getApplicationContext(), getString(R.string.title_activity_maps)+"\n"+getString(R.string.base_url), LENGTH_SHORT).show();
            }
        });
        mMap.setOnMapLongClickListener (new GoogleMap.OnMapLongClickListener () {
            @Override
            public void onMapLongClick(LatLng latLng) {
                tid = up.getPid ();
                float bearing = waz.getFloat(); // my bearing
                CameraPosition cp = mMap.getCameraPosition ();
                cameraPosition = new CameraPosition.Builder ().target (lm).bearing (bearing).zoom(cp.zoom).tilt (cp.tilt).build ();
                mMap.animateCamera (CameraUpdateFactory.newCameraPosition (cameraPosition));
                makeText(getApplicationContext(), "Tracking OFF", LENGTH_SHORT).show();
            }
        });
    }

    public void InitializeMarkers() {
        try {
            Marker marker;
            mMap.clear (); // remove previous marker (if any)
            markers = new Hashtable<Integer,Marker>();
            for (int i = 0; i < memberList.size (); i++) {
                UserInfo mi = memberList.get (i);
                LatLng ll = new LatLng (mi.getLat (), mi.getLng ());

                if(mi.id != up.getPid ())  marker = mMap.addMarker (new MarkerOptions ().position (ll).title (mi.getName ())); // default
                else marker = mMap.addMarker (new MarkerOptions ().position (ll).title (mi.getName ()).zIndex(1.0f).icon ( // It'm mine
                                BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN)));
                markers.put (mi.id, marker);
            }

            bMarkersInitialized = true;
            nameList.addAll (memberList);
        }
        catch (Exception e) {
//            makeText(getApplicationContext(), "Online: "+e.getMessage (), LENGTH_LONG).show();
        }
    }

    public boolean Online() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return (netInfo != null && netInfo.isConnected() && netInfo.isAvailable ());
        }
        catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Online: "+e.getMessage (), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1500);
            while(true) {
                if(Online ()) {
                    receive_data();
                    waz.step3 ();
                }
                Thread.sleep(3500);
            }
        }
        catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Online: "+e.getMessage (), Toast.LENGTH_SHORT).show();
        }
    }

    void receive_data() {
        StringRequest request = new StringRequest(Request.Method.POST,getString (R.string.base_url) + getString (R.string.download_url), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jArr = new JSONArray(response);
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jObj = jArr.getJSONObject(i);

                        UserInfo mi = new UserInfo();
                        mi.setID(jObj.getInt("id"));
                        if (index > 0) mi.setName(jObj.getString("name"));
                        mi.setLat(jObj.getDouble("lat"));
                        mi.setLng(jObj.getDouble("lng"));
                        memberList.add(mi);
                    }

                    Message msg = Message.obtain();
                    msg.what = waz.DOWNLOAD_MESSAGE;
                    waz.msgHandler.sendMessage(msg);
                    if (index > 0) index = 0;
                }
                catch(JSONException e) {}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//             makeText(getApplicationContext(), "error.getMessage(), LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("pid", up.getPid () + "_" + index);
                return params;
            }
        };
        memberList.clear ();
        ghQ.add(request);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String name = marker.getTitle();

        Iterator<UserInfo> iter = nameList.iterator();
        while ( iter.hasNext() == true ) {
            UserInfo info = (UserInfo)iter.next();
            if (info.getName ().equals(name)) {
                lm = marker.getPosition ();
                tid = info.getID ();
                break;
            }
        }

        CameraPosition cp = mMap.getCameraPosition ();
        cameraPosition = new CameraPosition.Builder ().target (lm).bearing (cp.bearing).zoom(cp.zoom).tilt (cp.tilt).build ();
        mMap.animateCamera (CameraUpdateFactory.newCameraPosition (cameraPosition));
        makeText(getApplicationContext(), "Tracking " + name, LENGTH_SHORT).show();

        return false;
    }

    public void onNewOptions(View view) {
        if(Online()) {
            Intent i = new Intent(getApplicationContext(), GroupActivity.class);
            i.putExtra("option", "11");
            startActivity(i);
        }
    }

    public void onMemOptions(View view) {
        if(Online()) {
            Intent i = new Intent(getApplicationContext(), GroupActivity.class);
            i.putExtra("option", "15");
            startActivity(i);
        }
    }

    public void onAdmOptions(View view) {
        if(Online()) {
            Intent i = new Intent(getApplicationContext(), GroupActivity.class);
            i.putExtra("option", "14");
            startActivity(i);
        }
    }

    public void onGroupOptions(View view) {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.activity_maps_bottomsheet, null);
        mBottomSheetDialog.setContentView(sheetView);
        String msgText = xMessage.getMessage();

        if(msgText.length() < 3) msgText =  "Hello - "+up.getName()+"\nSelect one of the following options";
        ((TextView)sheetView.findViewById(R.id.fragment_bottom_sheet_message)).setText(msgText);
        mBottomSheetDialog.show();
        if(Online()) xMessage.SendMessage(up.getPid(), 0, "");
    }

    void groupButtonCreate() {
        relativeLayout = (RelativeLayout)findViewById(R.id.map_parent_layout);
        groupButton = new Button(MapsActivity.this);
        groupButton.setText("Groups");
        RelativeLayout.LayoutParams buttonParam = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.id.map);
        buttonParam.addRule(RelativeLayout.CENTER_HORIZONTAL, R.id.map);
        groupButton.setLayoutParams(buttonParam);

        groupButton.bringToFront();
        relativeLayout.addView(groupButton);
        groupButton.setClickable(true);
        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Online()) onGroupOptions(v);
            }
        });

        xMessage = new com.morningwalk.ihome.explorer.recyclerview.Messages(getBaseContext());
        if(Online()) xMessage.SendMessage(up.getPid(), 0, "");
    }
 }
