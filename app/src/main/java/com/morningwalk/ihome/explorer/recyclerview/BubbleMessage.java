package com.morningwalk.ihome.explorer.recyclerview;
// It is an interface class that send messages from MemberView to its upper class GroupAdapter
// It is implemented in GroupAdapter class using implements in the top class declaration line
// Its is instantiated in MemberView as member object and is invoked in bind() method of it

public interface BubbleMessage {
    void onItemClick(Member member, Group group);
    void onItemLongClick(Member member, Group group);
}
