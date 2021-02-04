package com.morningwalk.ihome.explorer.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.morningwalk.ihome.explorer.R;
import com.morningwalk.ihome.explorer.UserPreferences;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MemberView extends ChildViewHolder {
    ImageView image, action;
    TextView textView;
    BubbleMessage bubbleMessage;
    UserPreferences up;
    boolean delete, insert;
    int admin;

     public MemberView(View itemView, BubbleMessage bubbleMessage, UserPreferences up, int admin) {
        super(itemView);
        this.up  = up;
        this.admin = admin;
        this.bubbleMessage = bubbleMessage;
        textView = (TextView)itemView.findViewById(R.id.text_member);
        image  = (ImageView) itemView.findViewById(R.id.image_member);
        action = (ImageView) itemView.findViewById(R.id.action_member);
    }

    public void setView(Member member, Group group) {
        int status = member.getStatus();
        textView.setText(member.getName());

        if(status > 1) image.setImageResource( R.drawable.admin);
        else if(status == 1) image.setImageResource(R.drawable.member);
        else image.setImageResource(R.drawable.request);

        delete = insert = false;
        if(admin == 1) {
            if (status < 1) {
                action.setImageResource(R.drawable.image_insert); insert = true;
            }
            else if (status == 1 || group.getItems().size() == 1) {
                action.setImageResource(R.drawable.image_delete); delete = true;
            }
            else action.setImageResource(R.drawable.image_disable);
        }
        else {
            action.setImageResource(R.drawable.image_disable);
            if (status == 1) { // Disable all actions except one can delete himself
                if (member.getId() == up.getPid()) {
                    action.setImageResource(R.drawable.image_delete); delete = true;
                }
            }
            else if (!checkme(group)) {
                action.setImageResource(R.drawable.image_insert); insert = true;
            }
        }
    }

    public void bind(Member member, Group group) {
        setView(member, group);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // used for request and register roles
                if(insert) bubbleMessage.onItemClick(member, group);
            }
        });
        action.setOnLongClickListener(new View.OnLongClickListener() { // used for delete
            @Override
            public boolean onLongClick(View v) {
                if(delete) bubbleMessage.onItemLongClick(member, group);
                return false;
            }
        });
    }

    boolean checkme(Group group) {
        List<Member> items = group.getItems();
        for(Member me:items) {
            if(me.getId()==up.getPid()) return true;
        }
        return false;
    }
}
