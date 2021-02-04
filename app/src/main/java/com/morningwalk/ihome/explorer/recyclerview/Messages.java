package com.morningwalk.ihome.explorer.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
//import com.morningwalk.ihome.explorer.JSONParser;
//import com.morningwalk.ihome.explorer.NameValue;
import com.morningwalk.ihome.explorer.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.morningwalk.ihome.explorer.*;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class Messages {
    Context context;
    String lastMsg="";
    int count = -1;
    RequestQueue ghQ = null;

    public Messages(Context context) {
        this.context = context;
        ghQ = GHQ.getInstance(context).getRequestQueue();
    }
    public String getMessage() { return count>0 ? lastMsg: ""; }
    public void SendMessage(int uid, int opt, String msg) {
        StringRequest request = new StringRequest(Request.Method.POST, context.getString(R.string.base_url) + context.getString(R.string.message_url),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            count = -1;
//                            makeText(getApplicationContext(), response, LENGTH_LONG).show();
                            JSONObject result = new JSONObject(response);
                            String res = result.getString("success");
                            int success = Integer.parseInt(res);
                            if(success > 0) {
                                count = success;
                                lastMsg = result.getString("message");
                            } else {
//                              makeText(getApplicationContext(), "Error reading url data", LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
//                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                String msg = error.getMessage();
//                if(msg != null) makeText(context, "Message: " + msg, LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", uid + "");
                params.put("opt", opt + "");
                params.put("msg", msg);
                return params;
            }
        };
        ghQ.add(request);
    }
}
