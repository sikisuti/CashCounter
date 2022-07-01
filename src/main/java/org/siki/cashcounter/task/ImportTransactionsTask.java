package org.siki.cashcounter.task;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.view.dialog.ExceptionDialog;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.siki.cashcounter.task.ImportTransactionsTask.XlsxColumn.*;

@Slf4j
public class ImportTransactionsTask extends Task<List<AccountTransaction>> {
  public static final String ACCOUNT_NUMBER = "1177353504210012";
  private final CategoryService categoryService;
  private final DataManager dataManager;
  private Long lastTransactionId;
  private final DataFormatter dataFormatter = new DataFormatter();
  private final File transactionsFile;

  public ImportTransactionsTask(
      File transactionsFile, CategoryService categoryService, DataManager dataManager) {
    this.transactionsFile = transactionsFile;
    this.categoryService = categoryService;
    this.dataManager = dataManager;
  }

  @Override
  protected List<AccountTransaction> call() {
    final List<AccountTransaction> newTransactions;
    if (transactionsFile.getName().endsWith(".xlsx")) {
      newTransactions = importTransactionsFromXlsx(transactionsFile);
    } else {
      newTransactions = new ArrayList<>();
    }

    return newTransactions;
  }

  private List<AccountTransaction> importTransactionsFromXlsx(File file) {
    try (var inputStream = new FileInputStream(file)) {
      var workbook = new XSSFWorkbook(inputStream);
      var sheet = workbook.getSheetAt(0);
      return parseXlsxSheet(sheet);
    } catch (Exception e) {
      log.error("", e);
      ExceptionDialog.get(e).showAndWait();
      return List.of();
    }
  }

  private List<AccountTransaction> parseXlsxSheet(XSSFSheet sheet) {
    var newTransactions = new ArrayList<AccountTransaction>();
    for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
      var row = sheet.getRow(rowIndex);
      try {
        var accountNumber =
            ofNullable(row.getCell(XlsxColumn.ACCOUNT_NUMBER.getNumber()))
                .map(dataFormatter::formatCellValue)
                .orElse(null);
        if (ACCOUNT_NUMBER.equals(accountNumber)) {
          var transaction = parseXlsxRow(row);
          newTransactions.add(transaction);
        }
      } catch (Exception e) {
        log.info("Error in xlsx file in row: {}", rowIndex);
      }
    }

    return newTransactions;
  }

  private AccountTransaction parseXlsxRow(XSSFRow row) {
    var transaction = new AccountTransaction();
    transaction.setId(getNextTransactionId());
    ofNullable(row.getCell(TRANSACTION_DATE_TIME.getNumber()))
        .ifPresent(
            cell ->
                transaction.setTransactionDateTime(
                    LocalDateTime.parse(
                        dataFormatter.formatCellValue(cell),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
    ofNullable(row.getCell(DATE.getNumber()))
        .ifPresent(
            cell -> transaction.setDate(LocalDate.parse(dataFormatter.formatCellValue(cell))));
    ofNullable(row.getCell(TYPE.getNumber()))
        .ifPresent(cell -> transaction.setType(dataFormatter.formatCellValue(cell)));
    ofNullable(row.getCell(OWNER.getNumber()))
        .ifPresent(cell -> transaction.setOwner(dataFormatter.formatCellValue(cell)));
    ofNullable(row.getCell(PARTNER_ACCOUNT_NUMBER.getNumber()))
        .ifPresent(
            cell ->
                transaction.setAccountNumber(
                    dataFormatter.formatCellValue(cell).replaceAll("0{8}$", "")));
    ofNullable(row.getCell(COMMENT.getNumber()))
        .ifPresent(cell -> transaction.setComment(dataFormatter.formatCellValue(cell)));
    ofNullable(row.getCell(AMOUNT.getNumber()))
        .ifPresent(cell -> transaction.setAmount((int) Math.round(cell.getNumericCellValue())));

    categoryService.setCategory(transaction);
    return transaction;
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

  enum XlsxColumn {
    TRANSACTION_DATE_TIME(0),
    DATE(1),
    TYPE(2),
    OWNER(4),
    PARTNER_ACCOUNT_NUMBER(5),
    COMMENT(7),
    ACCOUNT_NUMBER(9),
    AMOUNT(10);

    private final int number;

    XlsxColumn(int number) {
      this.number = number;
    }

    private int getNumber() {
      return number;
    }
  }
}
