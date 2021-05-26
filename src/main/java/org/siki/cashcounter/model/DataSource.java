package org.siki.cashcounter.model;

import lombok.Data;

import java.util.List;
@Data
public class DataSource {
    private List<MonthlyBalance> monthlyBalances;
}
