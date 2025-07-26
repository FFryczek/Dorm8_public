package dev.ffryczek.Entities;
import java.math.BigDecimal;
import java.util.UUID;

public class GroupUser{
    private final UUID myGlobalUserID;
    private BigDecimal groupUserBalance;
    private UUID groupUserID;
    private final UUID myGroupID;
    private String name;

    //Constructor
    public GroupUser(UUID globalUserID, UUID groupID) {
        this.myGlobalUserID = globalUserID;
        this.myGroupID = groupID;
        groupUserBalance = BigDecimal.ZERO;
        groupUserID = UUID.randomUUID();
    }
    
    //DAO constructor
    public GroupUser(UUID globalUserID, UUID groupID, UUID groupUserID, BigDecimal balance) {
        this.myGlobalUserID = globalUserID;
        this.myGroupID = groupID;
        this.groupUserBalance = balance;
        this.groupUserID = groupUserID;
    }




    public UUID getGroupUserID() {
        return this.groupUserID;
    }

    public UUID getGroupID() {
        return myGroupID;
    }
    
    public BigDecimal getGroupUserBalance() {
        return this.groupUserBalance;
    }

    public String getName() {
        return name;
    }


    //ADD and SUBTRACT balance
    public void addBalance(BigDecimal value) {
        this.groupUserBalance = this.groupUserBalance.add(value);
    }

    public void subtractBalance(BigDecimal value) {
        this.groupUserBalance = this.groupUserBalance.subtract(value);
    }

    public void zeroBalance(){
        this.groupUserBalance = BigDecimal.valueOf(0);
    }

}

