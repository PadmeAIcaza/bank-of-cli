package com.bankofcli.bankofcli.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private long transactionId;
    private long accountId;
    private String transactionType;
    private BigDecimal amount;
    private Long recipientId; // Long can be null
    private LocalDateTime timestamp;

    public Transaction(long transactionId, long accountId, String transactionType, BigDecimal amount, Long recipientId, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.recipientId = recipientId;
        this.timestamp = timestamp;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}