package org.siki.cashcounter.model;

import lombok.Data;

@Data
public class CategoryMatchingRule {
  private String pattern;
  private String category;
}
