package com.morningwalk.ihome.explorer.recyclerview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.morningwalk.ihome.explorer.GHQ;
//import com.morningwalk.ihome.explorer.NameValue;
import com.morningwalk.ihome.explorer.R;
import com.morningwalk.ihome.explorer.UserPreferences;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.listeners.OnGroupClickListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class GroupAdapter extends ExpandableRecyclerViewAdapter<GroupView, MemberView> implements BubbleMessage {
    UserPreferences up;
    Context  context;
    Messages xMessage;
    int admin; // response of service_request for communication message service

    RequestQueue ghQ = null;
    int response = -1;
    Member m_member;
    Group m_group;

    public GroupAdapter(List<? extends ExpandableGroup> groups, UserPreferences up, int admin, Context context) {
        super(groups);
        ghQ = GHQ.getInstance(context).getRequestQueue();
        xMessage = new Messages(context);
        this.context = context;
        this.admin = admin;
        this.up = up;
    }

    @Override
    public GroupView onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_group, parent, false);
        return new GroupView(view);
    }

    @Override
    public MemberView onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_member, parent, false);
        return new MemberView(view, this, up, admin);
    }

    @Override
    public void onBindChildViewHolder(MemberView holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final Member member = (Member)group.getItems().get(childIndex);
        holder.bind(member, (Group)group);
    }

    @Override
    public void onBindGroupViewHolder(GroupView holder, int flatPosition, ExpandableGroup group) {
        holder.bind((Group)group);
    }

    @Override
    public void onItemClick(Member member, Group group) {
        response = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(admin==0) {
            builder.setMessage("Do you want to join " + group.getTitle() + " group?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_group = group; m_member = member; response = 0;
                    service_request("glogin.php", up.getPid() + "", group.getTitle(), "12", "");
                }
            }).setCancelable(true).create().show();
        }
        else {
            builder.setMessage("Hello " + group.getAdmin() + ": Do you want to add " + member.getName() + " to " + group.getTitle() + " group?");
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_group = group; m_member = member; response = 1; // grant membership
                    service_request("alogin.php", up.getPid() + "", group.getTitle(), "" + member.getId(), "");
                }
            }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_group = group; m_member = member; response = 2; // decline membership
                    service_request("alogin.php", up.getPid() + "", group.getTitle(),  "-" + member.getId(), "");
                }
            }).setCancelable(true).create().show();
        }
    }

    @Override
    public void onItemLongClick(Member member, Group group) {
        response = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(admin==0) builder.setMessage("Do you want to leave " + group.getTitle() + " group?");
        else if(group.getItems().size() == 1) builder.setMessage("Are you sure? you want to delete " + group.getTitle() + " group?");
        else builder.setMessage("Do you want to remove " + member.getName() + " from " + group.getTitle() + " group?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_group = group; m_member = member;
                response = admin + 3; // generates 3 and 4
                if(admin==0) service_request("glogin.php", up.getPid() + "", group.getTitle(), "13", "");
                else service_request("alogin.php", up.getPid() + "", group.getTitle(),  "-" + member.getId(), "");
            }
        }).setCancelable(true).create().show();
    }

    public void service_request(String... args) {
        StringRequest request = new StringRequest(Request.Method.POST, context.getString(R.string.base_url) + args[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
//                            makeText(getApplicationContext(), response, LENGTH_LONG).show();
                            JSONObject json = new JSONObject(response);
                            onPostExecute(json);
                        } catch (JSONException e) {
//                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                String msg = error.getMessage();
//                makeText(context, "Functions: " + msg, LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", args[1]);
                params.put("name", args[2]);
                params.put("option", args[3]);
                return params;
            }
        };
        ghQ.add(request);
    }

    protected void onPostExecute(JSONObject result) {
        try {
 //           if (result != null) {
                String res = result.getString("result");
                int success = Integer.parseInt(res);
                if (success >= 0) {
                    if (response >= 0) {
                        String message;
                        List<Member> items = m_group.getItems();
                        switch (response) {
                            case 0:
                                items.add(new Member(up.getPid(), up.getName(), 0)); // status 0 mean request
                                notifyDataSetChanged();

                                message = "Hello " + m_group.getAdmin() + ": " + up.getName() + " has requested for membership of " + m_group.getTitle() + " group!";
                                xMessage.SendMessage(m_group.getAdminId(), 2, message);
                                break;
                            case 1:
                                int index = items.indexOf(m_member); // which request to serve
                                items.get(index).setStatus(1);    // change request status to member status
                                notifyDataSetChanged();

                                makeText(context, m_member.getName() + " added to " + m_group.getTitle() + " group and is being informed!!!", LENGTH_LONG).show();
                                message = "Hello " + m_member.getName() + ": " + up.getName() + " has acknowledged you as member of " + m_group.getTitle() + " group!";
                                xMessage.SendMessage(m_member.getId(), 2, message);
                                break;
                            case 2:
                                items.remove(m_member);
                                notifyDataSetChanged();

                                message = "Sorry " + m_member.getName() + ": Your request to join " + m_group.getTitle() + " group is declined!";
                                xMessage.SendMessage(m_member.getId(), 2, message);
                                break;
                            case 3:
                                m_group.getItems().remove(m_member);
                                notifyDataSetChanged();

                                message = "Hello " + m_group.getAdmin() + ": " + up.getName() + " has left the " + m_group.getTitle() + " group!";
                                xMessage.SendMessage(m_group.getAdminId(), 2, message);
                                break;
                            case 4:
                                m_group.getItems().remove(m_member);
                                if (m_group.getItems().size() == 0) getGroups().remove(m_group);
                                notifyDataSetChanged(); // No one to inform about group removal!!!

                                if (m_member.getId() != up.getPid()) {
                                    message = "Sorry " + m_member.getName() + ": Your are no more present in " + m_group.getTitle() + " group!";
                                    xMessage.SendMessage(m_member.getId(), 2, message);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    if(success==-1) makeText(context, "Error: group not found!", LENGTH_LONG).show();
                    else if(success < -1) makeText(context, "Error: limit exceeded!", LENGTH_LONG).show();
                }
//            } else {
  //              makeText(context, "Error reading url data", LENGTH_LONG).show();
    //        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
