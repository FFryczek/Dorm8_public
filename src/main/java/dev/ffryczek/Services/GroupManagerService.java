package dev.ffryczek.Services;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.ExpenseDAO;
import dev.ffryczek.DAOs.GroupDAO;
import dev.ffryczek.DAOs.GroupUserDAO;
import dev.ffryczek.Entities.Expense;
import dev.ffryczek.Entities.Group;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Entities.Transfer;
import dev.ffryczek.Utilities.FinanceHandler;

public class GroupManagerService {

    private final GroupDAO groupDAO;
    private final ExpenseDAO expenseDAO;
    private final GroupUserDAO groupUserDAO;

    private final UUID myGlobalUserID;
    
    private UUID myGroupUserID;
    private UUID myGroupID;

    //Constructor for entering group Interface
    public GroupManagerService(UUID myGlobalUserID, UUID myGroupID, UUID myGroupUserID,
            DataSourceConnectionHelper dataSourceHelper) {
        this.myGlobalUserID = myGlobalUserID;
        this.myGroupID = myGroupID;
        this.myGroupUserID = myGroupUserID;
        this.groupDAO = new GroupDAO(dataSourceHelper);
        this.expenseDAO = new ExpenseDAO(dataSourceHelper);
        this.groupUserDAO = new GroupUserDAO(dataSourceHelper);
    }
    //Constructor for entering create group Interface
    public GroupManagerService(UUID myGlobalUserID,
            DataSourceConnectionHelper dataSourceHelper) {
        this.myGlobalUserID = myGlobalUserID;
        this.myGroupID = null;
        this.myGroupUserID = null;
        this.groupDAO = new GroupDAO(dataSourceHelper);
        this.expenseDAO = new ExpenseDAO(dataSourceHelper);
        this.groupUserDAO = new GroupUserDAO(dataSourceHelper);
    }

    //Change managedGroup
    public void changeGroup(UUID myNewGroupID, UUID myNewGroupUserID) {
        this.myGroupID = myNewGroupID;
        this.myGroupUserID = myNewGroupUserID;
    }
    
    //Getters
    public UUID getGroupID() {
        return this.myGroupID;
    }

    public UUID getGroupUserID() {
        return this.myGroupUserID;
    }

    //METHODS

                                    
        //GROUP CRUD:

    public void insertGroup(Group group) {
        try {
            groupDAO.insertGroup(group);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteGroup(Group group) {
        UUID groupID = group.getGroupID();
        try {
            groupDAO.deleteGroup(groupID);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteGroup(UUID groupID) {
        try {
            groupDAO.deleteGroup(groupID);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Group withdrawGroup(Group group) {
        UUID groupID = group.getGroupID();
        try {
            return groupDAO.withdrawGroup(groupID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    public Group withdrawGroup(UUID groupID) {
        try {
            return groupDAO.withdrawGroup(groupID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

        //EXPENSE CRUD:
    
    public void insertExpense(Expense expense) {
        try {
            expenseDAO.insertExpense(expense);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public ArrayList<Expense> withdrawExpenseList(UUID groupID) {
            return new ArrayList<Expense>(expenseDAO.witdrawExpenses(groupID));
    }
    
    public ArrayList<UUID> withdrawExpenseListID(UUID groupID) {
        return new ArrayList<UUID>(expenseDAO.withdrawExpenseListIDs(groupID));
    }
    
    public ArrayList<UUID> withdrawExpenseDebtorIDs(UUID expenseID) {
        return new ArrayList<UUID>(expenseDAO.witdrawExpenseDebtorsIDs(expenseID));
    }
    
    public void modifyExpense(UUID expenseID, Expense newExpense) {
        try {
            expenseDAO.modifyExpense(expenseID, newExpense);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void deleteExpense(UUID expenseID) {
        try {
            expenseDAO.deleteExpense(expenseID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

        //GROUP_USERS

    public void insertGroupUser(GroupUser groupUser, UUID groupID) {
        try {
            groupUserDAO.insertGroupUser(groupUser, myGlobalUserID, groupID);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateGroupUserBalance(UUID groupUserID, BigDecimal newBalance) {
        try {
            groupUserDAO.updateBalance(groupUserID, newBalance);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateAllBalances(TreeMap<UUID, BigDecimal> newBalancesWithIDs) {
        for (Map.Entry<UUID, BigDecimal> entry : newBalancesWithIDs.entrySet()) {
            this.updateGroupUserBalance(entry.getKey(), entry.getValue());
        }
    }


    public TreeMap<UUID, GroupUser> withdrawGroupMembers() {
        try {
            return new TreeMap<UUID, GroupUser>(groupUserDAO.fetchGroupMembers(myGroupID));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //TOOLS:                (Inputs obtained from FrontEnd)
    //GroupCreator
    public UUID groupCreatorTool(String name) {
        //Create
        Group group = new Group(name);
        this.myGroupID = group.getGroupID();
        GroupUser me = new GroupUser(myGlobalUserID, myGroupID);

        //Insert to DB
        this.insertGroup(group);
        this.insertGroupUser(me, myGroupID);

        return group.getGroupID();
    }
    
    //ExpenseCreator
    public void expenseCreatorTool(BigDecimal amount, ArrayList<UUID> debtorIDs) {
        //Create
        Expense expense = new Expense(myGroupID, myGlobalUserID, amount, debtorIDs);

        //Insert
        this.insertExpense(expense);
    }
    
    //ExpenseModifier
    public void expenseModifierTool(UUID oldExpenseID, BigDecimal newAmount, ArrayList<UUID> newDebtorIDs) {
        //Create
        Expense modifiedExpense = new Expense(myGroupID, myGlobalUserID, newAmount, newDebtorIDs);

        //Update
        this.modifyExpense(oldExpenseID, modifiedExpense);
    }

    //CalculateBalances
    public TreeMap<UUID, BigDecimal> calculateBalancesTool() {
        //Init output
        TreeMap<UUID, BigDecimal> output = new TreeMap<>();
        
        //Fetch
        ArrayList<UUID> inGroupUsersIDs = new ArrayList<>(this.withdrawGroupMembers().keySet());
        ArrayList<Expense> expenseList = new ArrayList<>(this.withdrawExpenseList(myGroupID));

        //Call method
        output = new TreeMap<UUID, BigDecimal>(FinanceHandler.calculateBalances(inGroupUsersIDs, expenseList));
        
        //Update
        this.updateAllBalances(output);
        
        return output;
        
    }
    
    //CalculateTransfers
    public ArrayList<Transfer> calculateTransfersTool() {
        //Init output 
        ArrayList<Transfer> output = new ArrayList<>();

        //Fetch
        TreeMap<UUID,GroupUser> inGroupUsers = this.withdrawGroupMembers();

        //Call method
        output = new ArrayList<>(FinanceHandler.getTransfers(inGroupUsers));


        return FinanceHandler.sortTransfers(output);

    }


}
