package dev.ffryczek.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import dev.ffryczek.Entities.GlobalUser;



public class GlobalUserDAO {
    //Storing DataSourceConnectionHelper object
    private DataSourceConnectionHelper dataSourceHelper;


    //Constructor
    public GlobalUserDAO(DataSourceConnectionHelper dataSourceHelper) {
        this.dataSourceHelper = dataSourceHelper;
    }
    
    //CRUD:
    //Withdraw globalUser object by log in credential (log in operation) //TODO: Dodaj ispassword Correct jako osobna metode ktora returnuje true albo false(Po dodaniu hashowania)
    public GlobalUser withdrawGlobalUser(String login, String password) throws IllegalArgumentException {
        //Hashing password
        //String hashedPassword = ;
        //Query
        String query = "SELECT * FROM GlobalUser " +
                "WHERE login = ? AND password = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query (login and password)
            pStatement.setString(1, login);
            pStatement.setString(2, password);
            //Withdraw entry
            ResultSet resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("user_name");
                UUID id = UUID.fromString(resultSet.getString("id"));

                //Create object of GlobalUser
                GlobalUser globalUser = new GlobalUser(name, id);
                return globalUser;
            } else {
                throw new IllegalArgumentException("\nLog in parameters dont match any existing account.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }
    

    //Withdraw globalUserID by his login
    public UUID withdrawGlobalUser_onLogin(String usersLogin) throws IllegalArgumentException {
        //Query
        String query = "SELECT * FROM GlobalUser " +
                "WHERE login = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);
    
        ) {
            //Assign value to query
            pStatement.setString(1, usersLogin);
            //Withdraw entry
            ResultSet resultSet = pStatement.executeQuery();
    
            if (resultSet.next()) {
                UUID usersID = UUID.fromString(resultSet.getString("id"));
    
                return usersID;
            } else {
                throw new IllegalArgumentException("\nCredentials dont match any GlobalUser\n");
            }
    
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }

    //Insert new GlobalUser to DB upon registration (LOGIN AND PASSWORD ARE OBTAINED BY FRONTEND)
    public void insertGlobalUser(GlobalUser globalUser, String login, String password) throws IllegalArgumentException{
        //Hashing password
        //String hashedPassword = ;
        //Withdraw values from globaluser object
        String id = globalUser.getUserID().toString();
        String user_name = globalUser.getName();
        //Query
        String query = "INSERT INTO GlobalUser (id, user_name, login, password)" +
                "VALUES (?, ?, ?, ?)";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query (id, user_name, login, password)
            pStatement.setString(1, id);
            pStatement.setString(2, user_name);
            pStatement.setString(3, login);
            pStatement.setString(4, password);
            //Execture quary
            int rowsInserted = pStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("\nGlobalUser sucessfully added to database!\n");
            } else {
                throw new IllegalArgumentException("\nOperation failed.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }
    
    //Delete globalUser by using log in credentials
    public void deleteGlobalUser(String login, String password) throws IllegalArgumentException {
        //Hashing password
        //String hashedPassword = ;
        //Query

        ///TODO: Add also deleting all groupUsers in Database connected to this account!

        String query = "DELETE FROM GlobalUser " +
                "WHERE login = ? AND password = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query (login and password)
            pStatement.setString(1, login);
            pStatement.setString(2, password);

            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nGlobalUser sucessfully deleted from database!\n");
            } else {
                throw new IllegalArgumentException("\nOperation failed.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }

    //Cleanup Tool
    public void cleanUp(){
        //Query
        String query = "DELETE " +
                "FROM GlobalUser; ";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nAll globalUsers sucessfully deleted from database!\n");
            } else {
                System.out.println("\nDeleted 0 entries (global users)\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }
    
   
}
