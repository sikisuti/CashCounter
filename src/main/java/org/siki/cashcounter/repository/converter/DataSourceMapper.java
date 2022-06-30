package org.siki.cashcounter.repository.converter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataSource;
import org.siki.cashcounter.repository.DataSourceRaw;

import java.util.List;

@Mapper
public abstract class DataSourceMapper {
  @Mapping(
      source = "monthlyBalances",
      target = "monthlyBalances",
      qualifiedByName = "convertToObservableList")
  public abstract void fromRaw(DataSourceRaw dataSourceRaw, @MappingTarget DataSource dataSource);

  @Named("convertToObservableList")
  public ObservableList<MonthlyBalance> convertToObservableList(
      List<MonthlyBalance> monthlyBalances) {
    return FXCollections.observableArrayList(monthlyBalances);
  }

  public abstract DataSourceRaw toRaw(DataSource dataSource);
}
