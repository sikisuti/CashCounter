package org.siki.cashcounter.view.statistics;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class CategoryRow {
  private final StringProperty categoryName = new SimpleStringProperty();
  private final Map<YearMonth, CategoryCell> categoryCellMap = new LinkedHashMap<>();

  public String getCategoryName() {
    return categoryName.get();
  }

  public void setCategoryName(String categoryName) {
    this.categoryName.set(categoryName);
  }

  public void putCategoryCell(YearMonth yearMonth, int amount) {
    categoryCellMap.put(yearMonth, CategoryCell.builder().amount(amount).build());
  }

  public Optional<CategoryCell> getCategoryCell(YearMonth yearMonth) {
    return ofNullable(categoryCellMap.get(yearMonth));
  }
}
