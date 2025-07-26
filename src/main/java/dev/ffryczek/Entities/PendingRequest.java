package dev.ffryczek.Entities;

import java.util.UUID;

public class PendingRequest {

    private final UUID requestID;
    private final UUID toGroupID;
    private final UUID fromID;
    private final UUID invitedID;

    //Constructor
    public PendingRequest(UUID toGroupID, UUID fromGlobalUserID, UUID invitedGlobalUserID) {
        this.toGroupID = toGroupID;
        this.fromID = fromGlobalUserID;
        this.invitedID = invitedGlobalUserID;
        this.requestID = UUID.randomUUID();
    }

    //DAO constructor
    public PendingRequest(UUID requestID, UUID toGroupID, UUID fromGlobalUserID, UUID invitedGlobalUserID) {
        this.toGroupID = toGroupID;
        this.fromID = fromGlobalUserID;
        this.invitedID = invitedGlobalUserID;
        this.requestID = requestID;
    }

    //Methods

    public UUID getRequestID() {
        return this.requestID;
    }
    public UUID getToGroupID() {
        return this.toGroupID;
    }

    public UUID getFromID() {
        return this.fromID;
    }

    public UUID getInvitedGlobalUserID() {
        return this.invitedID;
    }


}
