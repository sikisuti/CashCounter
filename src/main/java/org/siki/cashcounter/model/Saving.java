package org.siki.cashcounter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Saving {
    private final int amount;
    private final String comment;
}
