package org.siki.cashcounter.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Saving {
    private final IntegerProperty amount;
    private final StringProperty comment;

    public Integer getAmount() { return amount.get(); }
    public void setAmount(Integer amount) { this.amount.set(amount); }
    public IntegerProperty amountProperty() { return amount; }

    public String getComment() { return comment.get(); }
    public void setComment(String comment) { this.comment.set(comment); }
    public StringProperty commentProperty() { return comment; }

    public Saving() {
        this.amount = new SimpleIntegerProperty();
        this.comment = new SimpleStringProperty();
    }
}
