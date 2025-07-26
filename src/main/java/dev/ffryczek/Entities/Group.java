package dev.ffryczek.Entities;

import java.util.*;

public class Group {
    private String groupName;
    private final UUID groupID;
    private int userAmount;
    private boolean isSettled = false;

    //Constructor
    public Group(String groupName) {
        this.groupName = groupName;
        this.groupID = UUID.randomUUID();

    }
    //DAO constructor
    public Group(String groupName, UUID groupID){
        this.groupName  = groupName;
        this.groupID = groupID;
    }

    //Methods
    public String getName() {
        return groupName;
    }

    public UUID getGroupID() {
        return groupID;
    }
    

    public void setSettled(boolean isGroupSettled){
        isSettled = isGroupSettled;
    }


}


