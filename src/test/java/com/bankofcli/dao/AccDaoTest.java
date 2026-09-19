package com.bankofcli.dao;
import com.bankofcli.bankofcli.dao.BankDAO;
import com.bankofcli.bankofcli.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import com.bankofcli.bankofcli.model.Transaction;
import java.util.List;

public class AccDaoTest {
    private BankDAO bankDAO;
    @BeforeEach
    void setUp() {
        bankDAO = new BankDAO();
    }

    //////////////////////////////////////////////////////////////////////// account methods ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void testCreateAccount() {

        Account account = bankDAO.createAccount("1234");

        assertNotNull(account);
        assertTrue(account.getAccountId() > 0);
        assertEquals("1234", account.getPin());
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void testFindById() {

        Account created = bankDAO.createAccount("1234");
        Account found = bankDAO.findById(created.getAccountId());

        assertNotNull(found);
        assertEquals(created.getAccountId(), found.getAccountId());
        assertEquals("1234", found.getPin());
    }

    @Test
    void testAuthenticateWithCorrectPin() {

        Account account = bankDAO.createAccount("1234");
        boolean authenticated = bankDAO.authenticate(account.getAccountId(), "1234");
        assertTrue(authenticated);
    }

    @Test
    void testDeleteAccount() {
        Account created = bankDAO.createAccount("1234");
        boolean deleted = bankDAO.deleteAccount(created.getAccountId());
        Account found = bankDAO.findById(created.getAccountId());
        assertTrue(deleted);
        assertNull(found);
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void testAuthenticateWithInvalidAccount() {

        boolean authenticated = bankDAO.authenticate(999999999, "1234");
        assertFalse(authenticated);
    }

    @Test
    void testFindByIdWithInvalidId() {
        Account account = bankDAO.findById(999999999);
        assertNull(account);
    }

    @Test
    void testAuthenticateWithWrongPin() {

        Account account = bankDAO.createAccount("1234");
        boolean authenticated = bankDAO.authenticate(account.getAccountId(), "9999");
        assertFalse(authenticated);
    }

//////////////////////////////////////////////////////////////////////// transaction methods ////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void depositShouldIncreaseBalance() {
        Account account = bankDAO.createAccount("1234");
        boolean result = bankDAO.deposit(account.getAccountId(), new BigDecimal("50.00"));

        Account updatedAccount = bankDAO.findById(account.getAccountId());

        assertTrue(result);
        assertEquals(0, new BigDecimal("50.00").compareTo(updatedAccount.getBalance()));
    }

    @Test
    void withdrawShouldDecreaseBalance() {
        Account account = bankDAO.createAccount("1234");
        bankDAO.deposit(account.getAccountId(), new BigDecimal("100.00"));

        long accountId = account.getAccountId();
        boolean result = bankDAO.withdraw(accountId, new BigDecimal("40.00"));

        Account updatedAccount = bankDAO.findById(accountId);

        assertTrue(result);
        assertEquals(0, new BigDecimal("60.00").compareTo(updatedAccount.getBalance()));
    }

    @Test
    void transferShouldMoveMoneyBetweenAccounts() {
        Account sender = bankDAO.createAccount("1234");
        bankDAO.deposit(sender.getAccountId(), new BigDecimal("100.00"));
        Account recipient = bankDAO.createAccount("5678");
        bankDAO.deposit(recipient.getAccountId(), new BigDecimal("100.00"));

        boolean result = bankDAO.transfer(sender.getAccountId(), recipient.getAccountId(), new BigDecimal("50.00"));

        Account updatedSender = bankDAO.findById(sender.getAccountId());
        Account updatedRecipient = bankDAO.findById(recipient.getAccountId());

        assertTrue(result);
        assertEquals(0, new BigDecimal("50.00").compareTo(updatedSender.getBalance()));
        assertEquals(0, new BigDecimal("150.00").compareTo(updatedRecipient.getBalance()));
    }

//////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void depositShouldReturnFalseWhenAccountDoesNotExist() {
        boolean result = bankDAO.deposit(999999L, new BigDecimal("50.00"));

        assertFalse(result);
    }

    @Test
    void withdrawShouldFailWhenInsufficientFunds() {
        Account account = bankDAO.createAccount("1234");
        bankDAO.deposit(account.getAccountId(), new BigDecimal("100.00"));

        long accountId = account.getAccountId();
        boolean result = bankDAO.withdraw(accountId, new BigDecimal("150.00"));

        Account updatedAccount = bankDAO.findById(accountId);

        assertFalse(result);
        assertEquals(0, new BigDecimal("100.00").compareTo(updatedAccount.getBalance()));
    }

    @Test
    void transferShouldRollbackWhenRecipientDoesNotExist() {
        Account sender = bankDAO.createAccount("1234");
        bankDAO.deposit(sender.getAccountId(), new BigDecimal("100.00"));

        long fakeRecipientId = 999999L;

        boolean result = bankDAO.transfer(sender.getAccountId(), fakeRecipientId, new BigDecimal("100.00"));

        Account updatedSender = bankDAO.findById(sender.getAccountId());

        assertFalse(result);
        assertEquals(0, new BigDecimal("100.00").compareTo(updatedSender.getBalance()));
    }

    //////////////////////////////////////////////////////// transaction history tests ////////////////////////////////////////////////////////
    @Test
    void depositShouldAppearInTransactionHistory() {
        Account account = bankDAO.createAccount("1234");

        bankDAO.deposit(account.getAccountId(), new BigDecimal("50.00"));

        List<Transaction> history = bankDAO.getTransactionHistory(account.getAccountId());
        assertEquals(1, history.size());
        Transaction transaction = history.get(0);

        assertEquals("DEPOSIT", transaction.getTransactionType());
        assertEquals(0, new BigDecimal("50.00").compareTo(transaction.getAmount()));
        assertEquals(account.getAccountId(), transaction.getAccountId());
        assertNull(transaction.getRecipientId());
    }

    @Test
    void withdrawalShouldAppearInTransactionHistory() {
        Account account = bankDAO.createAccount("1234");

        bankDAO.deposit(account.getAccountId(), new BigDecimal("100.00"));
        bankDAO.withdraw(account.getAccountId(), new BigDecimal("40.00"));

        List<Transaction> history = bankDAO.getTransactionHistory(account.getAccountId());

        // deposit + withdrawal
        assertEquals(2, history.size());

        // history is ORDER BY timestamp DESC, so withdrawal should be first
        Transaction transaction = history.get(0);

        assertEquals("WITHDRAWAL", transaction.getTransactionType());
        assertEquals(0, new BigDecimal("40.00").compareTo(transaction.getAmount()));
        assertEquals(account.getAccountId(), transaction.getAccountId());
        assertNull(transaction.getRecipientId());
    }

    @Test
    void transferShouldAppearInBothAccountsTransactionHistory() {
        Account sender = bankDAO.createAccount("1234");
        Account recipient = bankDAO.createAccount("5678");

        bankDAO.deposit(sender.getAccountId(), new BigDecimal("100.00"));
        bankDAO.transfer(sender.getAccountId(), recipient.getAccountId(), new BigDecimal("50.00"));

        List<Transaction> senderHistory = bankDAO.getTransactionHistory(sender.getAccountId());
        List<Transaction> recipientHistory = bankDAO.getTransactionHistory(recipient.getAccountId());

        // sender has DEPOSIT + TRANSFER
        assertEquals(2, senderHistory.size());

        // recipient only has incoming TRANSFER
        assertEquals(1, recipientHistory.size());

        Transaction senderTransfer = senderHistory.get(0);
        Transaction recipientTransfer = recipientHistory.get(0);

        assertEquals("TRANSFER", senderTransfer.getTransactionType());
        assertEquals("TRANSFER", recipientTransfer.getTransactionType());
        assertEquals(0, new BigDecimal("50.00").compareTo(senderTransfer.getAmount()));
        assertEquals(sender.getAccountId(), senderTransfer.getAccountId());
        assertEquals(recipient.getAccountId(), senderTransfer.getRecipientId());
    }

    @Test
    void newAccountShouldHaveEmptyTransactionHistory() {
        Account account = bankDAO.createAccount("1234");

        List<Transaction> history = bankDAO.getTransactionHistory(account.getAccountId());

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void failedWithdrawalShouldNotAppearInTransactionHistory() {
        Account account = bankDAO.createAccount("1234");

        bankDAO.deposit(account.getAccountId(), new BigDecimal("100.00"));

        boolean result = bankDAO.withdraw(account.getAccountId(), new BigDecimal("200.00"));

        List<Transaction> history = bankDAO.getTransactionHistory(account.getAccountId());

        assertFalse(result);
        assertEquals(1, history.size()); // only the original $100 deposit should exist
        assertEquals("DEPOSIT", history.get(0).getTransactionType());
    }

}
