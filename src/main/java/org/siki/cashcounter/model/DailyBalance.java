package org.siki.cashcounter.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;

public final class DailyBalance {
    private DailyBalance prevDailyBalance;
    private final ObjectProperty<LocalDate> date;
    private final IntegerProperty balance;
    private final IntegerProperty cash;
    private final BooleanProperty predicted;
    private final IntegerProperty totalMoney;
    private final BooleanProperty reviewed;
    private final IntegerProperty dailySpend;

    private final ObservableList<Saving> savings;
    private final ObservableList<Correction> corrections;
    private final ObservableList<AccountTransaction> transactions;

    public void setPrevDailyBalance(DailyBalance prevDailyBalance) { 
        this.prevDailyBalance = prevDailyBalance; 
        if (prevDailyBalance != null) {
            setDailySpend(getTotalMoney() - prevDailyBalance.getTotalMoney() - getTotalCorrections());
        } else {
            setDailySpend(0);
        }
    }

    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public ObjectProperty<LocalDate> dateProperty() { return date; }

    public Integer getBalance() { return balance.get(); }
    public void setBalance(Integer balance) { 
        this.balance.set(balance); 
        this.setTotalMoney(getBalance() + getCash());
    }
    public IntegerProperty balanceProperty() { return balance; }

    public Integer getCash() { return cash.get(); }
    public void setCash(Integer cash) { 
        this.cash.set(cash); 
        this.setTotalMoney(getBalance() + getCash());
    }
    public IntegerProperty cashProperty() { return cash; }

    public Boolean isPredicted() { return predicted.get(); }
    public void setPredicted(Boolean predicted) { 
        this.predicted.set(predicted); 
    }
    public BooleanProperty predictedProperty() { return predicted; }

    public Integer getTotalMoney() { return totalMoney.get(); }
    private void setTotalMoney(Integer value) {
        totalMoneyProperty().set(value); 
        if (prevDailyBalance != null) {
            setDailySpend(getTotalMoney() - prevDailyBalance.getTotalMoney() - getTotalCorrections());
        } else {
            setDailySpend(0);
        }
    }
    public IntegerProperty totalMoneyProperty() { return totalMoney; }

    public Boolean isReviewed() { return reviewed.get(); }
    public void setReviewed(Boolean reviewed) { this.reviewed.set(reviewed); }
    public BooleanProperty reviewedProperty() { return reviewed; }

    public Integer getDailySpend() { return dailySpend.get(); }
    private void setDailySpend(Integer value) { this.dailySpend.set(value); }
    public IntegerProperty dailySpendProperty() { return dailySpend; }

    public DailyBalance() {
        this.date = new SimpleObjectProperty<>();
        this.balance = new SimpleIntegerProperty();
        this.cash = new SimpleIntegerProperty();
        this.predicted = new SimpleBooleanProperty();
        this.totalMoney = new SimpleIntegerProperty();
        this.reviewed = new SimpleBooleanProperty();
        this.dailySpend = new SimpleIntegerProperty();
        savings = FXCollections.observableArrayList();
        corrections = FXCollections.observableArrayList();
        transactions = FXCollections.observableArrayList();
    }
    
    private DailyBalance(Builder builder) {
        this();
        setPrevDailyBalance(builder.prevDailyBalance);
        setDate(builder.date);
        setBalance(builder.balance == null ? 0 : builder.balance);
        setCash(builder.cash == null ? 0 : builder.cash);
        setPredicted(builder.predicted);
        setReviewed(builder.reviewed);
    }
    
    public void addSaving(Saving saving) {
        savings.add(saving);
    }
    
    public ObservableList<Saving> getSavings() {
        return savings;
    }
    
    public Integer getTotalSavings() {
        return savings.stream().mapToInt(Saving::getAmount).sum();
    }
    
    public void addCorrection(Correction correction) {
        corrections.add(correction);
        if (prevDailyBalance != null) {
            setDailySpend(getTotalMoney() - prevDailyBalance.getTotalMoney() - getTotalCorrections());
        } else {
            setDailySpend(0);
        }
    }
    
    public void removeCorrection(Correction correction) {
        corrections.remove(correction);
        if (prevDailyBalance != null) {
            setDailySpend(getTotalMoney() - prevDailyBalance.getTotalMoney() - getTotalCorrections());
        } else {
            setDailySpend(0);
        }
    }
    
    public ObservableList<Correction> getCorrections() {
        return corrections;
    }
    
    public void addTransaction(AccountTransaction transaction) {
        transaction.setDailyBalance(this);
        transactions.add(transaction);
    }

    public void addNonExistingTransactions(List<AccountTransaction> newTransactions) {
        findPossibleDuplicates(newTransactions);
        if (newTransactions.stream().filter(AccountTransaction::isPossibleDuplicate).count() == transactions.size()) {
            newTransactions.stream().filter(t -> !t.isPossibleDuplicate()).forEach(this::addTransaction);
        } else {
            newTransactions.forEach(this::addTransaction);
        }
    }

    private void findPossibleDuplicates(List<AccountTransaction> newTransactions) {
        for (AccountTransaction newTransaction : newTransactions) {
            if (transactions.stream().anyMatch(t -> t.similar(newTransaction))) {
                newTransaction.setPossibleDuplicate(true);
            }
        }
    }

    public void calculateBalance() {
        Integer diff = transactions.stream().mapToInt(AccountTransaction::getAmount).sum();
        setBalance(prevDailyBalance.getBalance() + diff);
    }
    
    public ObservableList<AccountTransaction> getTransactions() {
        return transactions;
    }
    
    public Integer getTotalCorrections() {
        return corrections.stream().mapToInt(Correction::getAmount).sum();
    }
    
    public static class Builder {
        DailyBalance prevDailyBalance;
        LocalDate date;
        Integer balance;
        Integer cash;
        Boolean predicted;
        Boolean reviewed;

        public Builder setPrevDailyBalance(DailyBalance prevDailyBalance) {
            this.prevDailyBalance = prevDailyBalance;
            return this;
        }

        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder setBalance(Integer balance) {
            this.balance = balance;
            return this;
        }

        public Builder setCash(Integer cash) {
            this.cash = cash;
            return this;
        }

        public Builder setPredicted(Boolean predicted) {
            this.predicted = predicted;
            return this;
        }    

        public Builder setReviewed(Boolean reviewed) {
            this.reviewed = reviewed;
            return this;
        }  
        
        public DailyBalance build() {
            return new DailyBalance(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        DailyBalance other = (DailyBalance)obj;
        boolean rtn = 
                this.getDate().equals(other.getDate()) &&
                this.getBalance().equals(other.getBalance()) &&
                this.getCash().equals(other.getCash()) &&
                this.isPredicted().equals(other.isPredicted()) &&
                this.isReviewed().equals(other.isReviewed());
        
        if (!rtn) {
            return false;
        }
        
        if (this.getSavings().size() != other.getSavings().size()) {
            return false;
        }

        if (this.getCorrections().size() != other.getCorrections().size()) {
            return false;
        }

        if (this.getTransactions().size() != other.getTransactions().size()) {
            return false;
        }
        
        var i = 0;
        while (i < this.getSavings().size()) {
            rtn = this.getSavings().get(i).equals(other.getSavings().get(i));
            if (!rtn) {
                return false;
            }

            i++;
        }
        
        i = 0;
        while (i < this.getCorrections().size()) {
            rtn = this.getCorrections().get(i).equals(other.getCorrections().get(i));
            if (!rtn) {
                return false;
            }

            i++;
        }
        
        i = 0;
        while (i < this.getTransactions().size()) {
            rtn = this.getTransactions().get(i).equals(other.getTransactions().get(i));
            if (!rtn) {
                return false;
            }

            i++;
        }
        
        return true;
    }

    public boolean isValid() {
        return transactions.stream().allMatch(AccountTransaction::isValid);
    }
}
