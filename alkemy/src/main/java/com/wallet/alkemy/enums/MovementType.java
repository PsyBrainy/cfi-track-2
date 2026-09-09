package com.wallet.alkemy.enums;

public enum MovementType {
    DEPOSIT("DEPOSITO"),
    TRANSFER("TRANSFERENCIA"),
    WITHDRAWAL("RETIRO"),
    PAYMENT("PAGO");

    private final String databaseValue;

    MovementType(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }
}