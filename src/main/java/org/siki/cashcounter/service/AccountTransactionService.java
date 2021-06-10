package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableDailyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
public class AccountTransactionService {
  @Autowired private final DataForViewService dataForViewService;
  private Long lastTransactionId;

  public ObservableAccountTransaction createObservableTransactionsFromCSV(String csvLine) {
    csvLine = csvLine.replace("\"", "");
    String[] elements = csvLine.split(";");

    AccountTransaction newTransaction =
        AccountTransaction.builder()
            .id(getNextTransactionId())
            .amount(Integer.parseInt(elements[2]))
            .date(LocalDate.parse(elements[4], DateTimeFormatter.ofPattern("yyyyMMdd")))
            .accountNumber(elements[7])
            .owner(elements[8])
            .comment(elements[9] + " " + elements[11])
            .counter("")
            .type(elements[12])
            .build();

    return ObservableAccountTransaction.of(newTransaction);
  }

  public void storeObservableTransactions(
      List<ObservableAccountTransaction> observableAccountTransactions,
      ObservableDailyBalance observableDailyBalance) {
    for (var transaction : observableAccountTransactions) {
      observableDailyBalance.addObservableTransaction(transaction);
    }
  }

  private Long getNextTransactionId() {
    if (lastTransactionId == null) {
      lastTransactionId =
          dataForViewService.getObservableMonthlyBalances().stream()
              .flatMap(
                  mb ->
                      mb.getObservableDailyBalances().stream()
                          .flatMap(db -> db.getObservableTransactions().stream()))
              .mapToLong(t -> t.idProperty().get())
              .max()
              .orElse(0);
    }

    return ++lastTransactionId;
  }
}
