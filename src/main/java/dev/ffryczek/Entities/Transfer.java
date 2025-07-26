package dev.ffryczek.Entities;

import java.math.BigDecimal;

public class Transfer {
    private String nameFrom;
    private String nameTo;
    private BigDecimal amount;

    //Constructor
    public Transfer(String originUserName, String targetUserName, BigDecimal amountOfTransfer) {
        this.nameFrom = originUserName;
        this.nameTo = targetUserName;
        this.amount = amountOfTransfer;
    }

    //Methods
     @Override
    public String toString()
     {
        return ( nameFrom + " owes: " + amount + " to " + nameTo);
     }

     public BigDecimal getAmount() {
        return amount;
     }

     public String getFrom() {return nameFrom;}

     public String getTo() {return nameTo;}
}
