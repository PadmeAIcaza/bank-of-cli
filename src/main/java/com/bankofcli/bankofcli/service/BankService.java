package com.bankofcli.bankofcli.service;
import com.bankofcli.bankofcli.dao.AccountDAO;
import com.bankofcli.bankofcli.model.Account;

import java.math.BigDecimal;

public class BankService {
    private final AccountDAO accountDAO;

    public BankService() {
        this.accountDAO = new AccountDAO();
    }

    public Account register(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must contain exactly 4 digits.");
        }

        return accountDAO.createAccount(pin); // if valid, ask the dao to create the account in PSQL
    }

    public Account login(long accountId, String pin) {
        if (accountDAO.authenticate(accountId, pin)) {
            return accountDAO.findById(accountId);
        }

        return null;
    }

    public BigDecimal obtainBalance(long accountId) {
        Account found = accountDAO.findById(accountId);
        return found.getBalance();
    }

    public Account deposit(long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be more than $0.0");
        }
        Account account = accountDAO.findById(accountId);
        BigDecimal newBalance = account.getBalance().add(amount);

        accountDAO.updateBalance(accountId, newBalance);
        account.setBalance(newBalance);

        return account;
    }

    public Account withdraw(long accountId, BigDecimal amount) {
        Account account = accountDAO.findById(accountId);
        BigDecimal balance = account.getBalance();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be more than $0.0");
        } else if (balance.compareTo(amount) < 0){
            throw new IllegalArgumentException("Not enough balance");
        }
        BigDecimal newBalance = balance.subtract(amount);

        accountDAO.updateBalance(accountId, newBalance);
        account.setBalance(newBalance);

        return account;
    }

}