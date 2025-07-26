package dev.ffryczek.DAOsTested;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.ExpenseDAO;
import dev.ffryczek.DAOs.GlobalUserDAO;
import dev.ffryczek.DAOs.GroupDAO;
import dev.ffryczek.DAOs.GroupUserDAO;
import dev.ffryczek.Entities.Expense;
import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.Group;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Utilities.DBCleaner;
import dev.ffryczek.Utilities.FinanceHandler;

public class ExpenseDAOTest {

    //DAOs
    private GroupUserDAO testGroupUserDAO;
    private GroupDAO testGroupDAO;
    private GlobalUserDAO testGlobalUserDAO;
    private ExpenseDAO testExpenseDAO;

    //Credentials
    private String testLogin = "testlogin_123";
    private String userName = "testGlobalUser";

    private String friendLogin = "friendlogin_123";
    private String friendName = "testGlobalFriend";

    private String testPassword = "testpassword_123";
    

    //Objects

    private GlobalUser testGlobalUser = new GlobalUser(userName);
    private GlobalUser testGlobalFriend = new GlobalUser(friendName);

    private Group testGroup = new Group("testGroup");

    private GroupUser testGroupUser = new GroupUser(testGlobalUser.getUserID(), testGroup.getGroupID());
    private GroupUser testGroupFriend = new GroupUser(testGlobalFriend.getUserID(), testGroup.getGroupID());

    //Storing IDs for convenience
    private UUID groupUserID = testGroupUser.getGroupUserID();
    private UUID globalUserID = testGlobalUser.getUserID();

    private UUID groupID = testGroup.getGroupID();

    private UUID globalFriendID = testGlobalFriend.getUserID();
    private UUID groupFriendID = testGroupFriend.getGroupUserID();

    //Create amounts for expenses
    BigDecimal amount1 = BigDecimal.valueOf(100.00).setScale(2,RoundingMode.HALF_UP);
    BigDecimal amount2 = BigDecimal.valueOf(500.00).setScale(2,RoundingMode.HALF_UP);
    
    //Create arrays for debtor IDs
    private ArrayList<UUID> debtorIDs_forExpense1 = new ArrayList<>();
    private ArrayList<UUID> debtorIDs_forExpense2 = new ArrayList<>();

    private Expense expense1;
    private Expense expense2;


    @BeforeEach
    public void setUp() {

        //Creating DataSourceConnectionHelper (default conn params)
        DataSourceConnectionHelper dataSourceConnectionHelper = new DataSourceConnectionHelper();

        //Establishing connection with default params
        dataSourceConnectionHelper.connectCredentials();

        //Creating DAO objects
        testGroupDAO = new GroupDAO(dataSourceConnectionHelper);
        testGroupUserDAO = new GroupUserDAO(dataSourceConnectionHelper);
        testGlobalUserDAO = new GlobalUserDAO(dataSourceConnectionHelper);
        testExpenseDAO = new ExpenseDAO(dataSourceConnectionHelper);

        //Insert both new global user and group to DB
        testGlobalUserDAO.insertGlobalUser(testGlobalUser, testLogin, testPassword);
        testGlobalUserDAO.insertGlobalUser(testGlobalFriend, friendLogin, testPassword);
        testGroupDAO.insertGroup(testGroup);

        //Insert grupUsers to db
        testGroupUserDAO.insertGroupUser(testGroupUser, globalUserID, groupID);
        testGroupUserDAO.insertGroupUser(testGroupFriend, globalFriendID, groupID);

    }

    @AfterEach
    public void cleanUp() {
        DBCleaner.ereaseAll();
        
    }
    
    @Test
    public void insert_and_withdraw_expense_test() {
        //Add debtor to debtor list
        debtorIDs_forExpense1.add(groupFriendID);
        debtorIDs_forExpense2.add(groupUserID);

        //Create two expenses
        expense1 = new Expense(groupID, groupUserID, amount1, debtorIDs_forExpense1);
        expense2 = new Expense(groupID, groupFriendID, amount2, debtorIDs_forExpense2);

        //Insert two expenses
        testExpenseDAO.insertExpense(expense1);
        testExpenseDAO.insertExpense(expense2);

        //Witdraw expenses
        testExpenseDAO.withdrawExpenseListIDs(groupID);

        //Create expenseId list
        HashSet<UUID> expenseIDList = new HashSet<>();
        expenseIDList.add(expense2.getExpenseID());
        expenseIDList.add(expense1.getExpenseID());

        //Assertion
        assertEquals(expenseIDList, new HashSet<UUID>(testExpenseDAO.withdrawExpenseListIDs(groupID)));
    }
    
    @Test
    public void modify_expense_test() {
        //Add debtor to debtor list
        debtorIDs_forExpense1.add(groupFriendID);

        //Create expense
        expense1 = new Expense(groupID, groupUserID, amount1, debtorIDs_forExpense1);

        //Create modified expense.  
        Expense modifiedExpense = new Expense(expense1.getExpenseID(), groupID, groupUserID, amount2, debtorIDs_forExpense1, expense1.getTimestamp());
        
        //Insert first expense
        testExpenseDAO.insertExpense(expense1);

        //Modify this expense
        testExpenseDAO.modifyExpense(expense1.getExpenseID(), modifiedExpense);

        System.out.println(expense1.getExpenseID());
        System.out.println("---------");
        System.out.println(modifiedExpense.getExpenseID());

        //Fetch expense
        Expense fetchedExpense = testExpenseDAO.withdrawExpense(expense1.getExpenseID());

        //Assertions
        assertEquals(amount2, fetchedExpense.getMoney());
    }
    
    @Test //TODO: MOVE THIS TEST TO SERVICE CLASS TEST (GROUPMANAGERSERVICE!)
    public void calculate_balances_for_group() {
        //Add debtor to debtor list
        debtorIDs_forExpense1.add(groupFriendID);
        debtorIDs_forExpense2.add(groupUserID);

        //Create two expenses
        Expense expense1 = new Expense(groupID, groupUserID, amount1, debtorIDs_forExpense1);
        Expense expense2 = new Expense(groupID, groupFriendID, amount2, debtorIDs_forExpense2);

        //Insert two expenses
        testExpenseDAO.insertExpense(expense1);
        testExpenseDAO.insertExpense(expense2);

        //Get in group users
        ArrayList<UUID> inGroupMembers = new ArrayList<>(testGroupUserDAO.fetchGroupMembers(groupID).keySet());

        //Get expense list
        ArrayList<Expense> expenseList = testExpenseDAO.witdrawExpenses(groupID);

        //Call calculateBalances
        FinanceHandler.calculateBalances(inGroupMembers, expenseList);

        //Fetch groupUsers from db
        GroupUser newGroupUser1 = testGroupUserDAO.withdrawGroupUser(globalUserID, groupID);
        GroupUser newGroupUser2 = testGroupUserDAO.withdrawGroupUser(globalFriendID, groupID);

        //Asssertions
        assertEquals(BigDecimal.valueOf(-100), newGroupUser1.getGroupUserBalance());
        assertEquals( BigDecimal.valueOf(100), newGroupUser2.getGroupUserBalance());



    }

}
