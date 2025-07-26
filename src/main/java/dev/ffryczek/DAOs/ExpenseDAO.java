package dev.ffryczek.DAOs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.ffryczek.Entities.Expense;

public class ExpenseDAO {

    private DataSourceConnectionHelper dataSourceHelper;

    //Constructor
    public ExpenseDAO(DataSourceConnectionHelper dataSourceHelper) {
        this.dataSourceHelper = dataSourceHelper;
    }

    //TOOLS:
    //UUID ArrayList to String ArrayList converter
    public ArrayList<String> UUID_ArrList_2_String_ArrList(ArrayList<UUID> inputList) {
        ArrayList<String> outputList = new ArrayList<>();
        for (UUID debtor : inputList) {
            outputList.add(debtor.toString());
        }
        return outputList;
    }
    
    //Tool for comparing two debtor arrayLists
    public DebtorListUpdateResult DebtorListUpdate (UUID expenseID, Expense newExpense){
        ArrayList<UUID> oldDebtors = this.witdrawExpenseDebtorsIDs(expenseID);
        ArrayList<UUID> newDebtors = newExpense.getDebtorIDs();

        //Creating sets (need to ignore order)
        Set<UUID> oldDebtorsSet = new HashSet<>(oldDebtors);
        Set<UUID> newDebtorsSet = new HashSet<>(newDebtors);

        //Creating output sets
        Set<UUID> toInsert = new HashSet<>(newDebtorsSet);
        Set<UUID> toDelete = new HashSet<>(oldDebtorsSet);

        //Assigning results of comparison
        toInsert.removeAll(oldDebtorsSet);
        toDelete.removeAll(newDebtorsSet);

        //Custom return
        return new DebtorListUpdateResult (new ArrayList<>(toInsert), new ArrayList<>(toDelete));
    }
    
    //Tool for fetching one expense from resultSet (IF WANT TO USE KEEP NAMING CONVENTION!!!!)
    public Expense expenseFromRS(ResultSet resultSet) throws SQLException {
        UUID expenseID = UUID.fromString(resultSet.getString("id"));
                UUID payingUserID = UUID.fromString(resultSet.getString("E_payer_id"));
                UUID group_id = UUID.fromString(resultSet.getString("E_group_id"));
                BigDecimal amount = resultSet.getBigDecimal("amount");
                String[] debtorIDs = resultSet.getString("debtor_ids").split(",");
                Timestamp sqlTimestamp = resultSet.getTimestamp("timestamp");
                LocalDateTime timestamp = sqlTimestamp.toLocalDateTime();
                ArrayList<UUID> debtorUsers = new ArrayList<>();


                for (String debtorID : debtorIDs) {
                    //Convert to UUID
                    UUID debtor_ID = UUID.fromString(debtorID);
                    debtorUsers.add(debtor_ID);
                }

                //Creating new expense object
                return new Expense(expenseID, group_id, payingUserID, amount, debtorUsers, timestamp);
    }

    //ExpenseList from resultSet (KEEP COLUMN LABEL NAMING CONVENTION!!!!!)
    public ArrayList<Expense> expenseListFromRS(ResultSet resultSet) throws SQLException {
        //Init of the list
        ArrayList<Expense> outputList = new ArrayList<>();
        while (resultSet.next()) {
            //Add expense to output
            outputList.add(expenseFromRS(resultSet));
        }
        return outputList;
    }
    //-----------------------------------------

    //CRUD:
    //Insert expense
    public void insertExpense(Expense expense) throws IllegalArgumentException{
        //Null check
        this.isExpenseValid(expense);

        Connection conn = null;
        //Withdraw data from expense
        String expenseID = expense.getExpenseID().toString();
        String payerID = expense.getPayingUserID().toString();
        String groupID = expense.getGroupID().toString();
        ArrayList<String> debtorIDs = this.UUID_ArrList_2_String_ArrList(expense.getDebtorIDs());
        String timestamp = expense.getSqltime().toString();


        //Create query
        String query1 = "INSERT INTO Expense (id, Group_id, Payer_id, amount, timestamp) " +
                "VALUES (?, ?, ?, ?, ?);";
        String query2 = "INSERT INTO ExpenseDebtors (Expense_id, Debtor_id) " +
                "VALUES (?, ?);";

        try {
            conn = this.dataSourceHelper.openConnection();
        } catch (SQLException sqlException) {
            System.out.println("\nSomething went wrong when opening connection\n");
            sqlException.printStackTrace();
        }
        if (conn != null) {
            try (
                    PreparedStatement pStatement = conn.prepareStatement(query1);

            ) {
                conn.setAutoCommit(false);
                //Assign values to query
                pStatement.setString(1, expenseID);
                pStatement.setString(2, groupID);
                pStatement.setString(3, payerID);
                pStatement.setBigDecimal(4, expense.getMoney());
                pStatement.setString(5, timestamp);

                //Withdraw entry
                int InsertedRows = pStatement.executeUpdate();

                if (InsertedRows < 1) {
                    throw new IllegalArgumentException("\nSomething went wrong when inserting expense!\n");
                }

                try (
                        PreparedStatement pStatement2 = conn.prepareStatement(query2)) {

                    for (String debtorID : debtorIDs) {

                        //Assign values to query
                        pStatement2.setString(1, expenseID);
                        pStatement2.setString(2, debtorID);

                        //Withdraw entry
                        int InsertedRows2 = pStatement2.executeUpdate();
                        
                        if (InsertedRows2 < 1) {
                            throw new IllegalArgumentException("\nSomething went wrong when inserting expense debtors!\n");
                        }
                    }
                }
                conn.commit();

            } catch (SQLException e) {
                System.out.println("\nSomething went wrong when opening connection(insert Expense)\n");
                e.printStackTrace();
                try{
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    System.out.println("\nSomething went wrong when opening connection(rollback exception)\n");
                    rollbackException.printStackTrace();
                }
            } finally {
                try{
                    conn.close();
                } catch (SQLException closeException) {
                    System.out.println("\nSomething went wrong when opening connection(close exception)\n");
                    closeException.printStackTrace();
                }
            }
        }
    }
    //Withdraw expense on its ID
    public Expense withdrawExpense(UUID expenseID) {
        //Query 
        String query = "SELECT " +
                "Expense.id AS id, " +
                "Expense.Group_id AS E_group_id, " +
                "Expense.Payer_id AS E_payer_id, " +
                "Expense.amount AS amount, " +
                "Expense.timestamp AS timestamp, " +
                "GROUP_CONCAT(ExpenseDebtors.Debtor_id) AS debtor_ids " +
                "FROM Expense " +
                "JOIN ExpenseDebtors " +
                "ON Expense.id = ExpenseDebtors.Expense_id " +
                "WHERE Expense.id = ? ;";

        //Get String from UUID
        String expense_id = expenseID.toString();

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);
        ) 
        {
            pStatement.setString(1, expense_id);
            ResultSet resultSet = pStatement.executeQuery();

            //Using method for witdrawing expense (KEEPING CORRECT NAMING CONVENTION!)
            if (resultSet.next()) {
                return expenseFromRS(resultSet);
            } else {
                System.out.println("Expense not found for ID: " + expense_id);
                return null;
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
            return null;
        }
    }


    //Withdraw expenseList for group
    public ArrayList<Expense> witdrawExpenses(UUID groupID) {
        //Query 
        String query = "SELECT " +
                "Expense.id AS id, " +
                "Expense.Group_id AS E_group_id, " +
                "Expense.Payer_id AS E_payer_id, " +
                "Expense.amount AS amount, " +
                "Expense.timestamp AS timestamp, " +
                "GROUP_CONCAT(ExpenseDebtors.Debtor_id) AS debtor_ids " +
                "FROM Expense " +
                "JOIN ExpenseDebtors " +
                "ON Expense.id = ExpenseDebtors.Expense_id " +
                "WHERE Expense.Group_id = ? " +
                "GROUP BY Expense.id;";

        //Get String from UUID
        String group_id = groupID.toString();

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);
        ) 
        {
            pStatement.setString(1, group_id);
            ResultSet resultSet = pStatement.executeQuery();

            //Using method for witdrawing expenseList (KEEPING CORRECT NAMING CONVENTION!)
            return expenseListFromRS(resultSet);

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
            return null;
        }
    }
    
    //Witdraw expenseList <UUID>
    public ArrayList<UUID> withdrawExpenseListIDs(UUID groupID) {
        //Init of output array
        ArrayList<UUID> outputList = new ArrayList<>();
        //Query
        String query = "SELECT " +
                "Expense.id AS id " +
                "FROM Expense " +
                "WHERE Group_id = ?;";
        //Get string from UUID
        String group_id = groupID.toString();

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);) {
            pStatement.setString(1, group_id);
            ResultSet resultSet = pStatement.executeQuery();

            while (resultSet.next()) {
                UUID expenseId = UUID.fromString(resultSet.getString("id"));
                outputList.add(expenseId);
            }
            return outputList;

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
            return null;
        }

    }

    //Witdraw expenseDebtorsIDs <UUID>
    public ArrayList<UUID> witdrawExpenseDebtorsIDs(UUID expenseID) {
        //Init of output array
        ArrayList<UUID> outputList = new ArrayList<>();
        //Query
        String query = "SELECT " +
                "Debtor_id " +
                "FROM ExpenseDebtors " +
                "WHERE Expense_id = ?";
        //Get string from UUID
        String expense_id = expenseID.toString();

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);
        )
        {
            pStatement.setString(1, expense_id);
            ResultSet resultSet = pStatement.executeQuery();

            while (resultSet.next()) {
                UUID debtorID = UUID.fromString(resultSet.getString("Debtor_id"));
                outputList.add(debtorID);
            }
            return outputList;

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
            return null;
        }

    }

    //Modify expense (SONSIDET PUTING THIS IN SERVICE like initial fetcher for groupUsers and groups)
    public void modifyExpense(UUID expenseId, Expense newExpense) throws IllegalArgumentException{
        //Validate new expense
        this.isExpenseValid(newExpense);
        this.modifyExpenseAttributes(expenseId, newExpense);
        this.modifyExpenseDebtors(expenseId, newExpense);
    }
   
    //Modify expense Attributes
    public void modifyExpenseAttributes(UUID expenseID, Expense newExpense) {
        //Query
        String query = "UPDATE Expense " +
                "SET Expense.Group_id = ?, " +
                "Expense.Payer_id = ?, " +
                "Expense.amount = ? " +
                "WHERE id = ?;";

        //Get string from UUID
        String expense_id = expenseID.toString();

        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);) {
            //Set values
            pStatement.setString(1, newExpense.getGroupID().toString());
            pStatement.setString(2, newExpense.getPayingUserID().toString());
            pStatement.setBigDecimal(3, newExpense.getMoney());
            pStatement.setString(4, expense_id);
            int AffectedRows = pStatement.executeUpdate();

            if (AffectedRows > 1) {
                throw new IllegalArgumentException("\nSomethin went wrong during update!\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
        }

    }
    
    //Modify expense DebtorList
    public void modifyExpenseDebtors(UUID expenseID, Expense newExpense) {
        Connection conn = null;
        //DeleteQuery
        String deleteQuery = "DELETE  " +
                "FROM ExpenseDebtors " +
                "WHERE Expense_id = ? " +
                "AND Debtor_id = ?";

        String insertQuery = "INSERT " +
                "INTO ExpenseDebtors " +
                "(Expense_id, Debtor_id) " +
                "VALUES " +
                "(?,?);";

        //Get string from UUID
        String expense_id = expenseID.toString();

        //UUID 2 STRING (Assigning arrays for insert and delete quaries)
        ArrayList<String> toInsert = this
                .UUID_ArrList_2_String_ArrList(this.DebtorListUpdate(expenseID, newExpense).toInsert);
        ArrayList<String> toDelete = this
                .UUID_ArrList_2_String_ArrList(this.DebtorListUpdate(expenseID, newExpense).toDelete);

        //Connection outside try block due to autocommit off
        try {
            conn = dataSourceHelper.openConnection();
        } catch (SQLException openConn) {
            System.out.println("\nSomething went wrong when opening connection(initial opening)");
            openConn.printStackTrace();
        }

        try (
                PreparedStatement pStatement1 = conn.prepareStatement(deleteQuery);
                PreparedStatement pStatement2 = conn.prepareStatement(insertQuery);) {
            conn.setAutoCommit(false);
            //Init feedback variables
            //int insertedRows = 0;
            //int deletedRows = 0;
            //Set values for pStatement1 and execute query for every deleteID
            for (String deleteID : toDelete) {
                pStatement1.setString(1, expense_id);
                pStatement1.setString(2, deleteID);
                pStatement1.executeUpdate();
                //deletedRows++;
            }

            //Set values for pStatement2 and execute query for every insertID
            for (String insertID : toInsert) {
                pStatement2.setString(1, expense_id);
                pStatement2.setString(2, insertID);
                pStatement2.executeUpdate();
                //insertedRows++;
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("\nSomething went wrong when opening connection(rollback exception)\n");
                rollbackException.printStackTrace();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException closeException) {
                System.out.println("\nSomething went wrong when opening connection(close exception)\n");
                closeException.printStackTrace();
            }
        }

    }
    
    //Delete Expense
    public void deleteExpense(UUID expenseID) throws IllegalStateException {
        Connection conn = null;
        //Extract string from UUID
        String expense_id = expenseID.toString();
        //Query
        String query1 = "DELETE FROM ExpenseDebtors " +
                "WHERE ExpenseDebtors.Expense_id = ?;";
        String query2 = "DELETE FROM Expense " +
                "WHERE Expense.id = ?;";

        //Connection outside try block due to autocommit off
        try {
            conn = dataSourceHelper.openConnection();
        } catch (SQLException openConn) {
            System.out.println("\nSomething went wrong when opening connection(initial opening)");
            openConn.printStackTrace();
        }

        try (
                PreparedStatement pStatement1 = conn.prepareStatement(query1);
                PreparedStatement pStatement2 = conn.prepareStatement(query2);) {
            //Autocommit false
            conn.setAutoCommit(false);
            //Assign value to query 
            pStatement1.setString(1, expense_id);
            int rowsUpdated1 = pStatement1.executeUpdate();

            pStatement2.setString(1, expense_id);
            int rowsUpdated2 = pStatement2.executeUpdate();

            if (rowsUpdated1 > 0) {
                System.out.println("\nExpense Debtor list sucessfully deleted from database!\n");
            } else {
                throw new IllegalStateException("\nOperation failed.\n");
            }

            if (rowsUpdated2 > 0) {
                System.out.println("\nExpense list sucessfully deleted from database!\n");
            } else {
                throw new IllegalStateException("\nOperation failed.\n");
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("\nSomething went wrong when opening connection(rollback exception)\n");
                rollbackException.printStackTrace();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException closeException) {
                System.out.println("\nSomething went wrong when opening connection(close exception)\n");
                closeException.printStackTrace();
            }
        }
    }
    
    //CleanUp
    public void cleanUp(){
        Connection conn = null;
        
        //Query
        String query1 = "DELETE " +
                "FROM ExpenseDebtors; "; 
        String query2 = "DELETE " +
                "FROM Expense; "; 

        //Connection outside try block due to autocommit off
        try {
            conn = dataSourceHelper.openConnection();
        } catch (SQLException openConn) {
            System.out.println("\nSomething went wrong when opening connection(initial opening)");
            openConn.printStackTrace();
        }

        try (
                PreparedStatement pStatement1 = conn.prepareStatement(query1);
                PreparedStatement pStatement2 = conn.prepareStatement(query2);
        ) {
            //Autocommit false
            conn.setAutoCommit(false);

            int rowsUpdated1 = pStatement1.executeUpdate();
            int rowsUpdated2 = pStatement2.executeUpdate();

            if (rowsUpdated1 > 0) {
                System.out.println("\nAll Expense Debtors sucessfully deleted from database!\n");
            } else {
                System.out.println("\n0 entries deleted (expense debtors)\n");
            }

            if (rowsUpdated2 > 0) {
                System.out.println("\nAll Expenses sucessfully deleted from database!\n");
            } else {
                System.out.println("\n0 entries deleted (expenses)\n");
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection(close exception)\n");
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("\nSomething went wrong when opening connection(rollback exception)\n");
                rollbackException.printStackTrace();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException closeException) {
                System.out.println("\nSomething went wrong when opening connection(close exception)\n");
                closeException.printStackTrace();
            }
        }
    }
    

    //Tool for validation
    public boolean isExpenseValid(Expense expenseToValidate) throws IllegalArgumentException{
        if(expenseToValidate == null ){
            throw new IllegalArgumentException("Expense is not valid please check [invalid Expense]");
        }
        if(expenseToValidate.getDebtorIDs() == null ){
            throw new IllegalArgumentException("Expense is not valid please check [invalid debtorIDs]");
        }
        if(expenseToValidate.getExpenseID() == null ){
            throw new IllegalArgumentException("Expense is not valid please check [invalid ExpenseID]");
        }
        if(expenseToValidate.getGroupID() == null ){
            throw new IllegalArgumentException("Expense is not valid please check [invalid groupID]");
        }
        if(expenseToValidate.getMoney() == null ){
            throw new IllegalArgumentException("Expense is not valid please check [invalid amount]");
        }
        if (expenseToValidate.getPayingUserID() == null) {
            throw new IllegalArgumentException("Expense is not valid please check [invalid payingUserID]");
        }
        else {
            return true;
        }
        
    }

    
}
