package com.wallet.alkemy.enums;

public enum TransactionType {
    INCOME("INGRESO"),
    EXPENSE("EGRESO");

    private final String databaseValue;

    TransactionType(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }
}