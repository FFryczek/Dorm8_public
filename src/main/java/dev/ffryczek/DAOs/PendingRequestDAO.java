package dev.ffryczek.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.UUID;

import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.PendingRequest;

public class PendingRequestDAO {

    private DataSourceConnectionHelper dataSourceHelper;

    //Constructor
    public PendingRequestDAO(DataSourceConnectionHelper dataSource) {
        this.dataSourceHelper = dataSource;
    }

    //CRUD:
    public void insertRequest(PendingRequest pendingRequest) throws IllegalArgumentException {
        String query = "INSERT INTO PendingRequest (id,toGroupID, fromID, invitedID) " +
                "VALUES(?, ?, ?, ?);";

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);
            ) {
            pStatement.setString(1, pendingRequest.getRequestID().toString());
            pStatement.setString(2, pendingRequest.getToGroupID().toString());
            pStatement.setString(3, pendingRequest.getFromID().toString());
            pStatement.setString(4, pendingRequest.getInvitedGlobalUserID().toString());

            int rowsInserted = pStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("\nRequest sucessfully added to database!\n");
            } else {
                throw new IllegalArgumentException("\nOperation failed.\n");
            }
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }
    
    public PendingRequest withdrawRequest(UUID requestID) throws IllegalArgumentException {
        String query = "SELECT " +
                "toGroupID, " +
                "fromID, " +
                "invitedID " +
                "FROM PendingRequest "+
                "WHERE id = ?;";

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);) {
            pStatement.setString(1, requestID.toString());

            ResultSet resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                UUID toGroupID = UUID.fromString(resultSet.getString("toGroupID"));
                UUID fromID = UUID.fromString(resultSet.getString("fromID"));
                UUID invitedID = UUID.fromString(resultSet.getString("invitedID"));

                return new PendingRequest(requestID, toGroupID, fromID, invitedID);

            } else {
                throw new IllegalArgumentException("\nID does not match any existing request.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }
    
    public TreeMap<UUID, PendingRequest> fetchAllRequests(GlobalUser globalUser) throws IllegalArgumentException {
        //Init found bool
        boolean found = false;
        //Init of output map
        TreeMap<UUID, PendingRequest> output = new TreeMap<>();
        //Withdrawe UUID from globalUser
        UUID globalUserID = globalUser.getUserID();
        String globalUser_id = globalUserID.toString();
        //Query
        String query = "SELECT " +
                "id, " +
                "toGroupID, " +
                "fromID " +
                "FROM PendingRequest " +
                "WHERE invitedID = ?;";

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);) {
            pStatement.setString(1, globalUser_id);

            ResultSet resultSet = pStatement.executeQuery();

            while (resultSet.next()) {
                found = true;
                UUID requestID = UUID.fromString(resultSet.getString("id"));
                UUID toGroupID = UUID.fromString(resultSet.getString("toGroupID"));
                UUID fromID = UUID.fromString(resultSet.getString("fromID"));

                PendingRequest request = new PendingRequest(requestID, toGroupID, fromID, globalUserID);
                output.put(requestID, request);
            }
            if (!found) {
                throw new IllegalArgumentException("\nUnable to find any requests for this user\n");
            } else {
                return output;
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }

    //Delete request by its ID
    public void deleteRequest(UUID requestID) throws IllegalStateException {
        //Extract string from UUID
        String request_id = requestID.toString();
        //Query
        String query = "DELETE FROM PendingRequest " +
                "WHERE id = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query 
            pStatement.setString(1, request_id);
            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nRequest sucessfully deleted from database!\n");
            } else {
                throw new IllegalStateException("\nOperation failed.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }

    public void cleanUp(){
        //Query
        String query = "DELETE " +
                "FROM PendingRequest; ";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nAll requests sucessfully deleted from database!\n");
            } else {
                System.out.println("\nDeleted 0 entries (pending requests) \n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }

}
