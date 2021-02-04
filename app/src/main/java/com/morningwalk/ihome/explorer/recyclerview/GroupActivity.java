package com.morningwalk.ihome.explorer.recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import com.morningwalk.ihome.explorer.R;
import com.morningwalk.ihome.explorer.UserPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.morningwalk.ihome.explorer.*;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class GroupActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    ArrayList<Group> groupList;
    RecyclerView groupView;
    SearchView searchView;
    GroupAdapter adapter;
    UserPreferences up;
    int admin, option;
    String newName = "";
    String query = "";
    RequestQueue ghQ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_group);
        groupView = findViewById(R.id.RecGroupView);
        groupView.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        ghQ = GHQ.getInstance(this).getRequestQueue();
        up = UserPreferences.Shared(getBaseContext());
        String options = getIntent().getStringExtra("option");
        option = Integer.parseInt(options);
        admin = (option == 14 ? 1 : 0);

        if (option >= 14) {
            fetch_group_data("dummy");
        } else {
            // Used to display New group screen for input name
            setContentView(R.layout.activity_group_creation);
        }
    }

    public void display(JSONObject job) {
        groupList = updateData(job);
        if (groupList.size() == 0) {
            Toast.makeText(getBaseContext(), "No group to perform admin!", LENGTH_LONG).show();
            finish();
        }
        display2(query);
    }

    public void display2(String query) {
        ArrayList<Group> grpList = query.isEmpty() ? groupList : filterData(query);
        adapter = new GroupAdapter(grpList, up, admin, this);
        groupView.setAdapter(adapter);
    }

    public void nothing_to_display(int resp) {
        if (this.option == 11) {
            if (resp == 0)
                makeText(getApplicationContext(), "Error: '" + newName + "' already exist!", LENGTH_LONG).show();
            else makeText(getApplicationContext(), "Error: group limit over!", LENGTH_LONG).show();
            finish();
        }
    }

    public void onSubmitButton(View view) {
        boolean cancel = false;
        View focusView = null;

        EditText mNewName = (EditText) findViewById(R.id.edittext);
        mNewName.setError(null);
        newName = mNewName.getText().toString();
        int length = newName.length();
        if (length < 3 || length > 16) {
            if (length > 16) mNewName.setError("Name cannot exceed 16 chars!");
            else mNewName.setError("Name must have at least 3 chars!");
            focusView = mNewName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
//        makeText(getApplicationContext(), "Submit button clicked", LENGTH_LONG).show();
        } else { // switch to list/search view
            setContentView(R.layout.activity_group);
            groupView = findViewById(R.id.RecGroupView);
            groupView.setLayoutManager(new LinearLayoutManager(this));
            fetch_group_data(newName);

            searchView = findViewById(R.id.search_view);
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
        }
    }

    public void onCancelButton(View view) {
//        makeText(getApplicationContext(), "Cancel button clicked", LENGTH_LONG).show();
        finish();
    }

    public ArrayList<Group> updateData(JSONObject job) {
        ArrayList<Group> grpList = new ArrayList<>();
        JSONArray group, groups;
        try {
            group = job.getJSONArray("group");
            groups = job.getJSONArray("groups");

            ArrayList<String> groupName = new ArrayList<>();
            for (int i = 0; i < group.length(); i++) {
                JSONObject jgroup = group.getJSONObject(i);
                groupName.add(jgroup.getString("name"));
            }

            for (int i = 0; i < groups.length(); i++) {
                JSONObject jgroup = groups.getJSONObject(i);
                String admins = jgroup.getString("admin");
                JSONArray members = jgroup.getJSONArray("members");

                ArrayList<Member> mlist = new ArrayList<>();
                for (int j = 0; j < members.length(); j++) {
                    JSONObject member = members.getJSONObject(j);
                    int id = Integer.parseInt(member.getString("id"));
                    int of = Integer.parseInt(member.getString("of"));
                    String name = member.getString("name"); // To find admin member from member's list
                    if (admins.equals(name)) mlist.add(new Member(id, name, 2));  // status admin=2
                    else if (of == 1 || option == 14) mlist.add(new Member(id, name, of));   // member=1
                    else if (id == up.getPid()) mlist.add(new Member(id, name, 0)); // Show only my request
                }
                if (groups.length() == group.length()) grpList.add(new Group(groupName.get(i), admins, mlist));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return grpList;
    }

    public ArrayList<Group> filterData(String query) {
        this.query = query; // remember last query string
        query = query.toLowerCase();
        ArrayList<Group> grpList = new ArrayList<>();
        for (Group group : groupList) {
            if (group.getTitle().toLowerCase().startsWith(query)) {
                grpList.add(group);
            }
        }

        return grpList;
    }

    @Override
    public boolean onClose() {
        display2("");
        adapter.notifyDataSetChanged();
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!query.isEmpty()) {
            display2(query);
            adapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (!query.isEmpty()) {
            display2(query);
            adapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public void fetch_group_data(String groupName) {
        StringRequest request = new StringRequest(Request.Method.POST,
                getString(R.string.base_url) + getString(R.string.group_url),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
//                            makeText(getApplicationContext(), response, LENGTH_LONG).show();
                            JSONObject json = new JSONObject(response);
                            String res = json.getString("result");
                            int resp = Integer.parseInt(res);
                            if (resp > 0) display(json);
                            else nothing_to_display(resp);
                        } catch (JSONException e) {
//                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                String msg = error.getMessage();
//                makeText(getApplicationContext(), "Groups: " + msg, LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", up.getPid() + "");
                params.put("name", groupName);
                params.put("option", option + "");
                return params;
            }
        };
        ghQ.add(request);
    }
}
