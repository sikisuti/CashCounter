package org.siki.cashcounter.repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import org.siki.cashcounter.model.MonthlyBalance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DataSource {
  private ObservableList<MonthlyBalance> monthlyBalances = FXCollections.observableArrayList();
  private Map<String, List<String>> categoryMatchingRules = new HashMap<>();
}
