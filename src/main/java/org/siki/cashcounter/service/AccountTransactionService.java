package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.view.model.ObservableDailyBalance;
import org.siki.cashcounter.view.model.ObservableTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.siki.cashcounter.service.AccountTransactionService.CSVColumn.ACCOUNT_NUMBER;
import static org.siki.cashcounter.service.AccountTransactionService.CSVColumn.AMOUNT;
import static org.siki.cashcounter.service.AccountTransactionService.CSVColumn.COMMENT_1;
import static org.siki.cashcounter.service.AccountTransactionService.CSVColumn.COMMENT_2;
import static org.siki.cashcounter.service.AccountTransactionService.CSVColumn.DATE;
import static org.siki.cashcounter.service.AccountTransactionService.CSVColumn.OWNER;
import static org.siki.cashcounter.service.AccountTransactionService.CSVColumn.TYPE;

@RequiredArgsConstructor
public class AccountTransactionService {
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final CategoryService categoryService;
  private Long lastTransactionId;

  public void createObservableTransactionsFromCSV(
      String csvLine, List<ObservableTransaction> newTransactions) {
    csvLine = csvLine.replace("\"", "");
    String[] elements = csvLine.split(";");

    if (elements.length > 12 && !elements[DATE.getNumber()].isEmpty()) {
      AccountTransaction newTransaction =
          AccountTransaction.builder()
              .id(getNextTransactionId())
              .amount(Integer.parseInt(elements[AMOUNT.getNumber()]))
              .date(
                  LocalDate.parse(
                      elements[DATE.getNumber()], DateTimeFormatter.ofPattern("yyyyMMdd")))
              .accountNumber(elements[ACCOUNT_NUMBER.getNumber()])
              .owner(elements[OWNER.getNumber()])
              .comment(elements[COMMENT_1.getNumber()] + elements[COMMENT_2.getNumber()])
              .counter("")
              .type(elements[TYPE.getNumber()])
              //              .pairedCorrections(new ArrayList<>())
              .build();

      categoryService.setCategory(newTransaction);
      newTransactions.add(ObservableTransaction.of(newTransaction));
    }
  }

  public void storeObservableTransactions(
      List<ObservableTransaction> observableTransactions,
      ObservableDailyBalance observableDailyBalance) {
    for (var transaction : observableTransactions) {
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
              .mapToLong(ObservableTransaction::getId)
              .max()
              .orElse(0);
    }

    return ++lastTransactionId;
  }

  enum CSVColumn {
    AMOUNT(2),
    DATE(4),
    ACCOUNT_NUMBER(7),
    OWNER(8),
    COMMENT_1(9),
    COMMENT_2(10),
    TYPE(12);

    private final int number;

    CSVColumn(int number) {
      this.number = number;
    }

    private int getNumber() {
      return number;
    }
  }
}
