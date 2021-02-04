// GHQ.java
// Added for communication issues with idzeropoint.com
// Not it worked whereas previously the communication stuckup occured
// 1. Login.java: Registeration purposes
// 2. LocationService.java: For sending data to Website
// 3. MapsAcitvity.java: To fetch shared location data to my device
// 4. GroupAcitivity.java/GroupAdapter.java: Group info fetching
// 5. Messages.java: To check and send messages to the Website

package com.morningwalk.ihome.explorer;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class GHQ {
    private static GHQ mInstance;
    private RequestQueue mRequestQueue;

    private GHQ(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    public static synchronized GHQ getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new GHQ(context);
        }

        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
