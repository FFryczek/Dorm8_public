package dev.ffryczek.Utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import dev.ffryczek.Entities.Expense;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Entities.Transfer;


public final class FinanceHandler{

    private FinanceHandler() {
        throw new UnsupportedOperationException("\nObject of this class cannot be instantiated.\n");
    }

    /// BALANCES

    public static TreeMap<UUID,BigDecimal> calculateBalances(ArrayList<UUID> inGroupUsers, ArrayList<Expense>expenseList) throws IllegalArgumentException{

        //Initialization of empty balances
        TreeMap<UUID, BigDecimal> storedBalances = new TreeMap<>();
        for (UUID userID : inGroupUsers){
            storedBalances.put(userID,BigDecimal.ZERO);
        }

        if (expenseList == null) {
            throw new IllegalArgumentException("\nCan't get expenses form uninitialized users map.\n");
        } else {

            for (Expense expense : expenseList) {

                //Withdraw from currentExpense
                BigDecimal amount = expense.getMoney();
                UUID payerID = expense.getPayingUserID();
                ArrayList<UUID> debtors = expense.getDebtorIDs();

                //Calculating shared amount
                int numberOfParticipants = inGroupUsers.size();
                BigDecimal shareAmount = amount.divide(BigDecimal.valueOf(numberOfParticipants),2, RoundingMode.HALF_UP);

                
                //Balance update for payer
                BigDecimal newPayerBalance = storedBalances.get(payerID).add(shareAmount);
                storedBalances.put(payerID, newPayerBalance);
                
                //Balance update for debtors
                for (UUID debtorID : debtors) {
                    BigDecimal newDebtorBalance = storedBalances.get(debtorID).subtract(shareAmount);
                    storedBalances.put(debtorID,newDebtorBalance);
                }
            }
            return storedBalances;
        }
    }


    /// TRANSFERS

    public static ArrayList<Transfer> getTransfers (TreeMap<UUID,GroupUser> users) throws IllegalArgumentException {
        //Get amount of users and their IDs
        TreeMap <UUID,BigDecimal> creditors = new TreeMap<>();
        TreeMap <UUID,BigDecimal> debtors = new TreeMap<>();
        ArrayList <Transfer> transferList = new ArrayList<>();

        //Splitting into creditors and debtors
        for (GroupUser tempUser : users.values()) {
            if (tempUser.getGroupUserBalance().compareTo(BigDecimal.ZERO) > 0) {
                creditors.put(tempUser.getGroupID(),tempUser.getGroupUserBalance());
            } else if (tempUser.getGroupUserBalance().compareTo(BigDecimal.ZERO) < 0) {
                debtors.put(tempUser.getGroupID(),tempUser.getGroupUserBalance());
            }
        }

        //Sorting from highest absolute value to lowest abs value of balance
        creditors = balanceSort(creditors);
        debtors = balanceSort(creditors);

        //Casting keysets to arrayList for further calculations
        ArrayList<UUID> creditorsIDArr = new ArrayList<>(creditors.keySet());
        ArrayList<UUID> debtorsIDArr = new ArrayList<>(debtors.keySet()); 


        //Calculate transfers
        int i = 0;
        int j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            //Fetch IDs
            UUID currentDebtorID = debtorsIDArr.get(i);
            UUID currentCreditorID = creditorsIDArr.get(j);

            //Create temporary groupUsers
            GroupUser currentCreditor = users.get(currentCreditorID);
            GroupUser currentDebtor = users.get(currentDebtorID);

            BigDecimal debtorBalance = currentDebtor.getGroupUserBalance();
            BigDecimal creditorBalance = currentCreditor.getGroupUserBalance();
            BigDecimal modBalance;

            /// Assigning modBalance
            if (debtorBalance.abs().compareTo(creditorBalance.abs()) > 0 ){
                modBalance = creditorBalance.abs();
                j++;
            } else if(debtorBalance.abs().compareTo(creditorBalance.abs()) < 0 ) {
                modBalance = debtorBalance.abs();
                i++;
            } else {
                modBalance = creditorBalance.abs();
                i++;
                j++;

            }
            //Call transfer

            transferList.add(new Transfer(currentDebtor.getName(),currentCreditor.getName(),modBalance));
            //Update balances
            currentCreditor.subtractBalance(modBalance);
            currentDebtor.addBalance(modBalance);
        }
        return transferList;

    }

    public static void printTransfers(ArrayList<Transfer> transferList){
        System.out.println("\n=== Transfers ===");
        for (Transfer transfer : transferList){
            System.out.println(transfer.toString());
        }
    }

    /// SORTING

    public static ArrayList<Transfer> sortTransfers(ArrayList<Transfer>transferList){
        if(transferList.isEmpty()){
            System.out.println("\nNothing to sort!\nb");
            return transferList;
        }else if(transferList.size()==1){
            return transferList;
        }
        int left = 0;
        int right = transferList.size();
        //Dividing
        /// SORTING BY CREATING A STACK OF DIVIDED Arrays
            int middle = (left+right)/2;
            ArrayList<Transfer>leftList = new ArrayList<>(transferList.subList(left,middle));
            ArrayList<Transfer>rightList = new ArrayList<>(transferList.subList(middle,right));
            rightList = sortTransfers(rightList);
            leftList = sortTransfers(leftList);
            return mergeTransfers(leftList,rightList);

    }

    public static ArrayList<Transfer> mergeTransfers(ArrayList<Transfer>leftList, ArrayList<Transfer>rightList){
        int i = 0; //left iterator
        int j = 0; //right iterator
        int sizeL = leftList.size();
        int sizeR = rightList.size();
        ArrayList<Transfer> mergedArray = new ArrayList<>();

        while(i<sizeL && j<sizeR) {
            Transfer currLeft = leftList.get(i);
            Transfer currRight = rightList.get(j);
            BigDecimal amountLeft = currLeft.getAmount();
            BigDecimal amountRight = currRight.getAmount();

            if (amountRight.compareTo(amountLeft) >= 0) {
                mergedArray.add(currLeft);
                i++;
            } else {
                mergedArray.add(currRight);
                j++;
            }
        }

            while(i<sizeL){         //Dumping all leftovers from leftList
                mergedArray.add(leftList.get(i));
                i++;
            }

            while(j<sizeR){         //Dumping all leftovers from rightList
                mergedArray.add(rightList.get(j));
                j++;
            }


        return mergedArray;
    }


    public static <K> TreeMap<K, BigDecimal> balanceSort(final Map<K, BigDecimal> map) {

        Comparator<K> customComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                BigDecimal abs1 = map.get(k1).abs();
                BigDecimal abs2 = map.get(k2).abs();
                int comp = abs1.compareTo(
                        abs2);
                if (comp == 0)
                    return 1;
                else
                    return comp;
            }
        };
        // SortedMap created using the comparator
        TreeMap<K, BigDecimal> sorted = new TreeMap<K, BigDecimal>(customComparator);

        sorted.putAll(map);

        return sorted;

    }

    public static splitOnBalanceReturn splitOnBalance (TreeMap<UUID,GroupUser> users) throws IllegalArgumentException    {
        //Initialize TreeMaps
        TreeMap <UUID,BigDecimal> creditors = new TreeMap<>();
        TreeMap <UUID,BigDecimal> debtors = new TreeMap<>();
        //Splitting into creditors and debtors
        if (users == null) {
            throw new IllegalArgumentException("\nCan't get expenses form uninitialized users mapt\n");
        }
        for (GroupUser tempUser : users.values()) {
            if (tempUser.getGroupUserBalance().compareTo(BigDecimal.ZERO) > 0) {
                creditors.put(tempUser.getGroupID(), tempUser.getGroupUserBalance());
            } else if (tempUser.getGroupUserBalance().compareTo(BigDecimal.ZERO) < 0) {
                debtors.put(tempUser.getGroupID(), tempUser.getGroupUserBalance());
            }
        }
        return new splitOnBalanceReturn(creditors,debtors);
    }
   

}



