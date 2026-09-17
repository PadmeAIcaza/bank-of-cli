package com.bankofcli.bankofcli.service;
import com.bankofcli.bankofcli.dao.BankDAO;
import com.bankofcli.bankofcli.model.Account;
import com.bankofcli.bankofcli.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

public class BankService {
    private final BankDAO bankDAO;

    public BankService() {
        this.bankDAO = new BankDAO();
    }

    public Account register(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must contain exactly 4 digits.");
        }

        return bankDAO.createAccount(pin); // if valid, ask the dao to create the account in PSQL
    }

    public Account login(long accountId, String pin) {
        if (bankDAO.authenticate(accountId, pin)) {
            return bankDAO.findById(accountId);
        }

        return null;
    }

    public BigDecimal obtainBalance(long accountId) {
        Account account = bankDAO.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        return account.getBalance();
    }

    public void deposit(long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than $0.00");
        }

        Account account = bankDAO.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        boolean success = bankDAO.deposit(accountId, amount);

        if (!success) {
            throw new IllegalStateException("Deposit could not be completed");
        }

//        BigDecimal newBalance = account.getBalance().add(amount);
//
//        bankDAO.updateBalance(accountId, newBalance);
//        bankDAO.createTransaction(accountId, "DEPOSIT", amount, null);
//        account.setBalance(newBalance);

    }

    public void withdraw(long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than $0.00");
        }

        Account account = bankDAO.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        BigDecimal balance = account.getBalance();
        if (balance.compareTo(amount) < 0){
            throw new IllegalArgumentException("Not enough balance");
        }
        boolean success = bankDAO.withdraw(accountId, amount);

        if (!success) {
            throw new IllegalStateException("Withdraw could not be completed");
        }

//        BigDecimal newBalance = balance.subtract(amount);
//
//        bankDAO.updateBalance(accountId, newBalance);
//        bankDAO.createTransaction(accountId, "WITHDRAWAL", amount, null);
//        account.setBalance(newBalance);

    }

    public void transfer(long senderId, long recipientId, BigDecimal amount) {
        if (senderId == recipientId) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than $0.00");
        }

        Account sender = bankDAO.findById(senderId);
        Account recipient = bankDAO.findById(recipientId);

        if (sender == null) {
            throw new IllegalArgumentException("Sender not found");
        }
        if (recipient == null) {
            throw new IllegalArgumentException("Recipient account not found");
        }
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        boolean success = bankDAO.transfer(senderId, recipientId, amount);

        if (!success) {
            throw new IllegalStateException("Transfer could not be completed");
        }

    }


}