package dev.ffryczek.Utilities;

import java.math.BigDecimal;
import java.util.TreeMap;
import java.util.UUID;

public final class splitOnBalanceReturn {
    public final TreeMap<UUID, BigDecimal> creditors;
    public final TreeMap<UUID, BigDecimal> debtors;

    public splitOnBalanceReturn(TreeMap<UUID, BigDecimal> creditors, TreeMap<UUID, BigDecimal> debtors) {
        this.creditors = creditors;
        this.debtors = debtors;
    }
}
