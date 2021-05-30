package org.siki.cashcounter.service.converter;

import org.mapstruct.Mapper;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;

@Mapper
public interface MonthlyBalanceMapper {
  ObservableMonthlyBalance toView(MonthlyBalance monthlyBalance);
}
