package com.morningwalk.ihome.explorer.recyclerview;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.morningwalk.ihome.explorer.R;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class GroupView extends GroupViewHolder {
    TextView groupName;
    ImageView arrowSymbol;
    public GroupView(View itemView) {
        super(itemView);
        arrowSymbol = (ImageView)itemView.findViewById(R.id.arrow_symbol);
        groupName = (TextView)itemView.findViewById(R.id.list_group);
    }

    public void bind(Group group1) {
        groupName.setText(group1.getTitle());
    }

    @Override
    public void expand() {
        RotateAnimation ra = new RotateAnimation(0, 90,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(300);
        ra.setFillAfter(true);
        arrowSymbol.setAnimation(ra);
    }

    @Override
    public void collapse() {
        RotateAnimation ra = new RotateAnimation(90, 0,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(300);
        ra.setFillAfter(true);
        arrowSymbol.setAnimation(ra);
    }
}
