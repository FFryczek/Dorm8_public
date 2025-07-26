package dev.ffryczek.Entities;

import java.util.*;


public class GlobalUser {
    protected final String userName;
    private final UUID userID;

    //Constructor
    public GlobalUser(String userName) {
        this.userName = userName;
        userID = UUID.randomUUID();
    }
    //DAO Constructor
    public GlobalUser(String userName, UUID userID){
        this.userName = userName;
        this.userID = userID;
    }


    ///Methods
    ///
    public UUID getUserID() {
        return this.userID;
    }

    public String getName() {
        return this.userName;
    }



}

