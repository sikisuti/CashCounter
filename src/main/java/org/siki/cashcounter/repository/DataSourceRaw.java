package org.siki.cashcounter.repository;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DataSourceRaw {
  private List<MonthlyBalanceRaw> monthlyBalances;
  private Map<String, List<String>> categoryMatchingRules = new HashMap<>();
}
