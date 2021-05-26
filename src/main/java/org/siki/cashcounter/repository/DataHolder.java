package org.siki.cashcounter.repository;

import lombok.Getter;
import org.siki.cashcounter.model.MonthlyBalance;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DataHolder {
    private final List<MonthlyBalance> monthlyBalances = new ArrayList<>();

    public void addMonthlyBalance(MonthlyBalance monthlyBalance) {
        monthlyBalances.add(monthlyBalance);
    }
}
