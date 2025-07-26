package dev.ffryczek.Entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Expense {

    private final UUID groupID;
    private final UUID expenseID;
    private UUID payingUser;
    private BigDecimal amount;
    private ArrayList<UUID> debtorUsers;
    private LocalDateTime timestamp;

    /// Custom expense constructor (CUSTOM DEBTORS)
    public Expense(UUID groupID, UUID payerID, BigDecimal amount, ArrayList<UUID> debtorIDs) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount has to be greater than zero!");
        }
        if (debtorIDs == null || debtorIDs.isEmpty()) {
            throw new IllegalArgumentException("Amount of debtors has to be greater than zero!");
        }
        this.debtorUsers = new ArrayList<UUID>(debtorIDs);
        this.groupID = groupID;
        this.payingUser = payerID;
        this.amount = amount;
        this.expenseID = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }
    
    public Expense(UUID expenseID, UUID groupID, UUID payerID, BigDecimal amount, ArrayList <UUID> debtorIDs, LocalDateTime timestamp){
        if (amount == null || amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount has to be greater than zero!");
        }
        if (debtorIDs == null || debtorIDs.isEmpty()){
            throw new IllegalArgumentException("Amount of debtors has to be greater than zero!");
        }
        this.debtorUsers = new ArrayList<UUID> (debtorIDs);
        this.groupID = groupID;
        this.payingUser = payerID;
        this.amount = amount;
        this.expenseID = expenseID;
        this.timestamp = timestamp;
    }


    public void modifyExpense(UUID newPayerID,BigDecimal amountNEW, ArrayList <UUID> debtorIDsNEW){
        if (amountNEW == null || amountNEW.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount has to be greater than zero!");
        }
        if (debtorIDsNEW == null || debtorIDsNEW.isEmpty()){
            throw new IllegalArgumentException("Amount of debtors has to be greater than zero!");
        }
        this.payingUser = newPayerID;
        this.amount = amountNEW;
        this.debtorUsers = new ArrayList<UUID>(debtorIDsNEW);

    }


    //Getters
    public BigDecimal getMoney(){
        return this.amount;
    }

    public UUID getPayingUserID(){
        return this.payingUser;
    }

    public ArrayList<UUID> getDebtorIDs() {
        return new ArrayList<UUID>(this.debtorUsers);
    }

    public UUID getExpenseID(){
        return this.expenseID;
    }

    public UUID getGroupID() {
        return this.groupID;
    }
    
    public Timestamp getSqltime() {
        return Timestamp.valueOf(timestamp);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }


}
