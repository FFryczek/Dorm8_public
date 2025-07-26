package dev.ffryczek.DAOsTested;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.GroupDAO;
import dev.ffryczek.Entities.Group;

public class GroupDAOTest {

    private GroupDAO testGroupDAO;
    private Group testGroup = new Group("TestGroup");

    @BeforeEach
    public void setUp() {

        //Creating DataSourceConnectionHelper (default conn params)
        DataSourceConnectionHelper dataSourceConnectionHelper = new DataSourceConnectionHelper();


        //Establishing connection with default params
        dataSourceConnectionHelper.connectCredentials();

        //Creating DAO object
        testGroupDAO = new GroupDAO(dataSourceConnectionHelper);
    }

    @AfterEach
    public void cleanUp() {
        testGroupDAO.deleteGroup(testGroup.getGroupID());
    }

    @Test
    public void insert_and_withdraw_new_group() {

        //Get ID of freshly creaded group
        UUID testGroupID = testGroup.getGroupID();
        //Inserting new group
        testGroupDAO.insertGroup(testGroup);

        //Withdraw of group by its ID
        Group fetchedGroup = testGroupDAO.withdrawGroup(testGroupID);

        //Asserting
        //1
        assertEquals(testGroup.getName(), fetchedGroup.getName());
        //2
        assertEquals(testGroupID, fetchedGroup.getGroupID());
    }



}
