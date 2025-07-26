package dev.ffryczek.Services;

import java.util.UUID;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.PendingRequestDAO;
import dev.ffryczek.Entities.PendingRequest;

public class RequestService {

    private final PendingRequestDAO pendingRequestDAO;

    private final UUID myGlobalUserID;

    //Constructor
    public RequestService(UUID myGlobalUserID, DataSourceConnectionHelper dataSourceHelper) {
        this.myGlobalUserID = myGlobalUserID;
        this.pendingRequestDAO = new PendingRequestDAO(dataSourceHelper);
    }

    //Getter
    public UUID getMyGlobalUser() {
        return this.myGlobalUserID;
    }

        //Request CRUD
        
        public void insertPendingRequest(PendingRequest pendingRequest) {
        try {
            pendingRequestDAO.insertRequest(pendingRequest);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public PendingRequest withdrawPendingRequest(UUID requestID) {
        try {
            return pendingRequestDAO.withdrawRequest(requestID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void deletePendingRequest(UUID requestID) {
        try {
            pendingRequestDAO.deleteRequest(requestID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    //TOOL
    //RequestCreator menu
    public void sendRequest(UUID groupID, UUID invitorID, UUID invitedID) {
        PendingRequest newRequest = new PendingRequest(groupID, invitorID,invitedID);
        insertPendingRequest(newRequest);
    }

}
