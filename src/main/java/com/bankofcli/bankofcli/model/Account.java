package com.bankofcli.bankofcli.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {

    private long accountId;
    private String pin;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public Account(long accountId, String pin, BigDecimal balance, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.pin = pin;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getPin() {
        return pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
