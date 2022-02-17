package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AccountTransaction {

  private long id;
  private String type;
  @JsonIgnore private LocalDate date;
  private LocalDateTime transactionDateTime;
  private int amount;
  private String accountNumber;
  private String owner;
  private String comment;

  @JsonProperty("subCategory")
  private StringProperty category;

  @JsonIgnore private ObservableList<Correction> pairedCorrections;

  @JsonIgnore private BooleanProperty paired;

  public BooleanProperty pairedProperty() {
    return paired;
  }

  public String getCategory() {
    return category.get();
  }

  public void setCategory(String value) {
    category.set(value);
  }

  public StringProperty categoryProperty() {
    return category;
  }

  @JsonIgnore
  public boolean isPaired() {
    return paired.get();
  }

  public AccountTransaction() {
    category = new SimpleStringProperty();
    pairedCorrections = FXCollections.observableArrayList();
    paired = new SimpleBooleanProperty();
    paired.bind(
        Bindings.createBooleanBinding(() -> !pairedCorrections.isEmpty(), pairedCorrections));
  }

  public AccountTransaction(AccountTransaction clonable) {
    this();
    this.setId(clonable.getId());
    this.setType(clonable.getType());
    this.setDate(clonable.getDate());
    this.setTransactionDateTime(clonable.getTransactionDateTime());
    this.setAmount(clonable.getAmount());
    this.setAccountNumber(clonable.getAccountNumber());
    this.setOwner(clonable.getOwner());
    this.setComment(clonable.getComment());
    this.setCategory(clonable.getCategory());
    this.pairedCorrections.addAll(clonable.getPairedCorrections());
  }

  @JsonIgnore
  public Integer getUnpairedAmount() {
    return getAmount() - pairedCorrections.stream().mapToInt(Correction::getAmount).sum();
  }

  public void addPairedCorrection(Correction correction) {
    pairedCorrections.add(correction);
  }

  public void addAllPairedCorrection(List<Correction> correction) {
    pairedCorrections.addAll(correction);
  }

  public void removePairedCorrection(Correction correction) {
    pairedCorrections.remove(
        pairedCorrections.stream()
            .filter(c -> c.getId() == correction.getId())
            .findFirst()
            .orElse(null));
  }

  @JsonIgnore
  public boolean isValid() {
    return getCategory() != null && !getCategory().isEmpty();
  }

  public boolean hasNoCategory() {
    return !isValid();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }

    AccountTransaction other = (AccountTransaction) obj;

    if (this.getCategory() == null) {
      if (other.getCategory() != null) {
        return false;
      }
    } else {
      if (!this.getCategory().equalsIgnoreCase(other.getCategory())) {
        return false;
      }
    }

    return this.getId() == other.getId()
        && this.getType().equals(other.getType())
        && this.getAmount() == other.getAmount()
        && this.getAccountNumber().equals(other.getAccountNumber())
        && this.getOwner().equals(other.getOwner())
        && this.getComment().equals(other.getComment())
    /*&& this.getCounter().equals(other.getCounter())*/ ;
  }

  public boolean similar(AccountTransaction other) {
    if (other == null) {
      return false;
    }

    return this.getType().replaceAll("\\s", "").equals(other.getType().replaceAll("\\s", ""))
        && this.getAmount() == other.getAmount()
        && this.getAccountNumber().equals(other.getAccountNumber())
        && this.getOwner().replaceAll("\\s", "").equals(other.getOwner().replaceAll("\\s", ""))
        && this.getComment().replaceAll("\\s", "").equals(other.getComment().replaceAll("\\s", ""));
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, type, date, amount, accountNumber, owner, comment /*, counter*/, category);
  }
}
