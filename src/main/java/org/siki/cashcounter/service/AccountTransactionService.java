package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.repository.DataManager;
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
  @Autowired private final CategoryService categoryService;
  @Autowired private final DataManager dataManager;
  private Long lastTransactionId;

  public void createObservableTransactionsFromCSV(
      String csvLine, List<AccountTransaction> newTransactions) {
    csvLine = csvLine.replace("\"", "");
    String[] elements = csvLine.split(";");

    if (elements.length > 12 && !elements[DATE.getNumber()].isEmpty()) {
      var newTransaction = new AccountTransaction();
      newTransaction.setId(getNextTransactionId());
      newTransaction.setAmount(Integer.parseInt(elements[AMOUNT.getNumber()]));
      newTransaction.setDate(
          LocalDate.parse(elements[DATE.getNumber()], DateTimeFormatter.ofPattern("yyyyMMdd")));
      newTransaction.setAccountNumber(elements[ACCOUNT_NUMBER.getNumber()]);
      newTransaction.setOwner(elements[OWNER.getNumber()]);
      newTransaction.setComment(elements[COMMENT_1.getNumber()] + elements[COMMENT_2.getNumber()]);
      newTransaction.setType(elements[TYPE.getNumber()]);

      categoryService.setCategory(newTransaction);
      newTransactions.add(newTransaction);
    }
  }

  public int storeObservableTransactions(
      List<AccountTransaction> transactions, DailyBalance dailyBalance) {
    var counter = 0;
    for (var transaction : transactions) {
      if (dailyBalance.getTransactions().stream().noneMatch(t -> t.similar(transaction))) {
        dailyBalance.addTransaction(transaction);
        counter++;
      }
    }

    return counter;
  }

  private Long getNextTransactionId() {
    if (lastTransactionId == null) {
      lastTransactionId =
          dataManager.getMonthlyBalances().stream()
              .flatMap(
                  mb -> mb.getDailyBalances().stream().flatMap(db -> db.getTransactions().stream()))
              .mapToLong(AccountTransaction::getId)
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
