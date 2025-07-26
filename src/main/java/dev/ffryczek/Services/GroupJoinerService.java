package dev.ffryczek.Services;

import java.rmi.AccessException;
import java.util.UUID;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.GroupUserDAO;
import dev.ffryczek.DAOs.PendingRequestDAO;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Entities.PendingRequest;

public class GroupJoinerService {

    private final GroupUserDAO groupUserDAO;
    private final PendingRequestDAO pendingRequestDAO;

    private final UUID myGlobalUserID;

    //Constructor
    public GroupJoinerService(UUID myGlobalUserID, DataSourceConnectionHelper dataSourceHelper) {
        this.myGlobalUserID = myGlobalUserID;
        this.pendingRequestDAO = new PendingRequestDAO(dataSourceHelper);
        this.groupUserDAO = new GroupUserDAO(dataSourceHelper);
    }

    //Getter
    public UUID getMyGlobalUser() {
        return this.myGlobalUserID;
    }

    //Accept request
    public boolean assessRequest(boolean isAccepted, UUID pendingRequestID) throws AccessException {

        try {
            //Withdraw of request object
            PendingRequest request = pendingRequestDAO.withdrawRequest(pendingRequestID);

            if (!myGlobalUserID.equals(request.getInvitedGlobalUserID())) {
                throw new AccessException("Cant accept someone else's invitation.");

            }
            if (isAccepted) {
                //Withdraw groupID and create associated groupUser
                UUID groupID = request.getToGroupID();
                GroupUser groupUser = new GroupUser(myGlobalUserID, groupID);

                //Insert to DB
                this.groupUserDAO.insertGroupUser(groupUser, myGlobalUserID, groupID);
                this.pendingRequestDAO.deleteRequest(pendingRequestID);

                return true;
            }
            else {
                this.pendingRequestDAO.deleteRequest(pendingRequestID);
                return false;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}