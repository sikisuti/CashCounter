package org.siki.cashcounter.repository.converter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataSource;
import org.siki.cashcounter.repository.DataSourceRaw;

@Mapper(uses = DataSourceMapper.ObservableListFactory.class)
public interface DataSourceMapper {
  //  @Mapping(
  //      source = "monthlyBalances",
  //      target = "monthlyBalances",
  //      qualifiedByName = "convertToObservableList")
  void fromRaw(DataSourceRaw dataSourceRaw, @MappingTarget DataSource dataSource);

  //  @Named("convertToObservableList")
  //  default ObservableList<MonthlyBalance> convertToObservableList(
  //      List<MonthlyBalanceRaw> monthlyBalancesRaw) {
  //    var monthlyBalanceList =
  //
  // monthlyBalancesRaw.stream().map(this::convertMonthlyBalance).collect(Collectors.toList());
  //    return FXCollections.observableArrayList(monthlyBalanceList);
  //  }

  //  MonthlyBalance convertMonthlyBalance(MonthlyBalanceRaw monthlyBalanceRaw);

  DataSourceRaw toRaw(DataSource dataSource);

  class ObservableListFactory {
    public ObservableList<MonthlyBalance> createObservableMonthlyBalances() {
      return FXCollections.observableArrayList();
    }

    //    public ObservableList<DailyBalance> createObservableDailyBalances() {
    //      return FXCollections.observableArrayList();
    //    }
  }
}
