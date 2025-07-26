package dev.ffryczek.DAOs;

import java.util.ArrayList;
import java.util.UUID;

//Custom return class for DebtorUpdate
    public class DebtorListUpdateResult {
        public ArrayList<UUID> toInsert;
        public ArrayList<UUID> toDelete;
    
        public DebtorListUpdateResult(ArrayList<UUID> toInsert, ArrayList<UUID> toDelete) {
            this.toInsert = toInsert;
            this.toDelete = toDelete;
        }
    }