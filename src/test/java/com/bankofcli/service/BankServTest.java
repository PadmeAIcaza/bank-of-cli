package com.bankofcli.service;
import com.bankofcli.bankofcli.dao.BankDAO;
import com.bankofcli.bankofcli.model.Account;
import com.bankofcli.bankofcli.model.Transaction;
import com.bankofcli.bankofcli.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BankServTest {

    private BankDAO bankDAO;
    private BankService bankService;

    @BeforeEach
    void setUp() {
        bankDAO = mock(BankDAO.class);
        bankService = new BankService(bankDAO);
    }


    //////////////////////////////////////////////////////////////////////// register ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void registerShouldCreateAccountWhenPinIsValid() {
        Account expectedAccount = new Account(1L, "1234", BigDecimal.ZERO, LocalDateTime.now());

        when(bankDAO.createAccount("1234")).thenReturn(expectedAccount);
        Account result = bankService.register("1234");

        assertNotNull(result);
        assertEquals(expectedAccount, result);
        verify(bankDAO).createAccount("1234");
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void registerShouldFailWhenPinIsInvalid() {

        assertThrows(IllegalArgumentException.class, () -> bankService.register("123"));

        verify(bankDAO, never()).createAccount(anyString()); // DAO should never be reached
    }


    //////////////////////////////////////////////////////////////////////// loging ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void loginShouldReturnAccountWhenCredentialsAreCorrect() {
        long accountId = 1L;

        Account expectedAccount = new Account(accountId, "1234", new BigDecimal("100.00"), LocalDateTime.now());

        when(bankDAO.authenticate(accountId, "1234")).thenReturn(true);
        when(bankDAO.findById(accountId)).thenReturn(expectedAccount);
        Account result = bankService.login(accountId, "1234");

        assertNotNull(result);
        assertEquals(expectedAccount, result);

        verify(bankDAO).authenticate(accountId, "1234");
        verify(bankDAO).findById(accountId);
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void loginShouldReturnNullWhenCredentialsAreIncorrect() {
        long accountId = 1L;

        when(bankDAO.authenticate(accountId, "9999")).thenReturn(false);

        Account result = bankService.login(accountId, "9999");

        assertNull(result);

        verify(bankDAO, never()).findById(accountId); // since authentication failed, findById should never happen
    }


    //////////////////////////////////////////////////////////////////////// obtain balance ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void obtainBalanceShouldReturnAccountBalance() {
        long accountId = 1L;

        Account account = new Account(accountId, "1234", new BigDecimal("250.00"), LocalDateTime.now());

        when(bankDAO.findById(accountId)).thenReturn(account);
        BigDecimal result = bankService.obtainBalance(accountId);

        assertEquals(0, new BigDecimal("250.00").compareTo(result));
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void obtainBalanceShouldFailWhenAccountDoesNotExist() {
        long accountId = 999L;

        when(bankDAO.findById(accountId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> bankService.obtainBalance(accountId));
    }


    //////////////////////////////////////////////////////////////////////// deposit  ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void depositShouldSucceedWhenAccountAndAmountAreValid() {
        long accountId = 1L;
        BigDecimal amount = new BigDecimal("50.00");

        Account account = new Account(accountId, "1234", new BigDecimal("100.00"), LocalDateTime.now());

        when(bankDAO.findById(accountId)).thenReturn(account);
        when(bankDAO.deposit(accountId, amount)).thenReturn(true);

        assertDoesNotThrow(() -> bankService.deposit(accountId, amount);
        verify(bankDAO).deposit(accountId, amount);
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////    @Test
    void depositShouldFailWhenAmountIsNegative() {
        long accountId = 1L;
        BigDecimal amount = new BigDecimal("-50.00");

        assertThrows(IllegalArgumentException.class, () -> bankService.deposit(accountId, amount));

        verify(bankDAO, never()).deposit(anyLong(), any()); // invalid amount should be caught before accessing DAO
    }


    //////////////////////////////////////////////////////////////////////// withdraw ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void withdrawShouldSucceedWhenAccountHasEnoughFunds() {
        long accountId = 1L;
        BigDecimal amount = new BigDecimal("40.00");

        Account account = new Account(accountId, "1234", new BigDecimal("100.00"), LocalDateTime.now());

        when(bankDAO.findById(accountId)).thenReturn(account);
        when(bankDAO.withdraw(accountId, amount)).thenReturn(true);

        assertDoesNotThrow(() -> bankService.withdraw(accountId, amount));
        verify(bankDAO).withdraw(accountId, amount);
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void withdrawShouldFailWhenAccountHasInsufficientFunds() {
        long accountId = 1L;
        BigDecimal amount = new BigDecimal("150.00");

        Account account = new Account(accountId, "1234", new BigDecimal("100.00"), LocalDateTime.now());
        when(bankDAO.findById(accountId)).thenReturn(account);

        assertThrows(IllegalArgumentException.class, () -> bankService.withdraw(accountId, amount));
        verify(bankDAO, never()).withdraw(anyLong(), any()); // DAO withdrawal should never happen
    }


    //////////////////////////////////////////////////////////////////////// transfer ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void transferShouldSucceedWhenTransferIsValid() {
        long senderId = 1L;
        long recipientId = 2L;
        BigDecimal amount = new BigDecimal("50.00");

        Account sender = new Account(senderId, "1234", new BigDecimal("100.00"), LocalDateTime.now());
        Account recipient = new Account(recipientId, "5678", new BigDecimal("20.00"), LocalDateTime.now());

        when(bankDAO.findById(senderId)).thenReturn(sender);
        when(bankDAO.findById(recipientId)).thenReturn(recipient);
        when(bankDAO.transfer(senderId, recipientId, amount)).thenReturn(true);

        assertDoesNotThrow(() -> bankService.transfer(senderId, recipientId, amount));
        verify(bankDAO).transfer(senderId, recipientId, amount);
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void transferShouldFailWhenSenderHasInsufficientFunds() {
        long senderId = 1L;
        long recipientId = 2L;
        BigDecimal amount = new BigDecimal("150.00");

        Account sender = new Account(senderId, "1234", new BigDecimal("100.00"), LocalDateTime.now());
        Account recipient = new Account(recipientId, "5678", BigDecimal.ZERO, LocalDateTime.now());

        when(bankDAO.findById(senderId)).thenReturn(sender);
        when(bankDAO.findById(recipientId)).thenReturn(recipient);

        assertThrows(IllegalArgumentException.class, () -> bankService.transfer(senderId, recipientId, amount));
        verify(bankDAO, never()).transfer(anyLong(), anyLong(), any());
    }


    //////////////////////////////////////////////////////////////////////// transaction ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////// positive tests ////////////////////////////////////////////////////////
    @Test
    void getTransactionHistoryShouldReturnTransactions() {
        long accountId = 1L;

        Account account = new Account(accountId, "1234", new BigDecimal("100.00"), LocalDateTime.now());
        Transaction transaction = new Transaction(1L, accountId, "DEPOSIT", new BigDecimal("100.00"), null, LocalDateTime.now());

        List<Transaction> expectedHistory = List.of(transaction);
        when(bankDAO.findById(accountId)).thenReturn(account);
        when(bankDAO.getTransactionHistory(accountId)).thenReturn(expectedHistory);

        List<Transaction> result = bankService.getTransactionHistory(accountId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedHistory, result);

        verify(bankDAO).getTransactionHistory(accountId);
    }

    //////////////////////////////////////////////////////// negative tests ////////////////////////////////////////////////////////
    @Test
    void getTransactionHistoryShouldFailWhenAccountDoesNotExist() {
        long accountId = 999L;

        when(bankDAO.findById(accountId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> bankService.getTransactionHistory(accountId));
        verify(bankDAO, never()).getTransactionHistory(accountId);
    }
}