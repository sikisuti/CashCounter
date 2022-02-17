package org.siki.cashcounter.view.statistics;

import javafx.beans.property.SimpleStringProperty;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryRow {
  private final SimpleStringProperty categoryName = new SimpleStringProperty();
  private final Map<YearMonth, Integer> categoryValueMap = new LinkedHashMap<>();

  public String getCategoryName() {
    return categoryName.get();
  }

  public void setCategoryName(String categoryName) {
    this.categoryName.set(categoryName);
  }

  public Integer getCategoryValue(YearMonth key) {
    return categoryValueMap.get(key);
  }

  public void putCategoryValue(YearMonth key, Integer value) {
    categoryValueMap.put(key, value);
  }
}
