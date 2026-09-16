package com.bankofcli.dao;
import com.bankofcli.bankofcli.dao.AccountDAO;
import com.bankofcli.bankofcli.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class AccDaoTest {
    private AccountDAO accountDAO;
    @BeforeEach
    void setUp() {
        accountDAO = new AccountDAO();
    }

    // positive tests
    @Test
    void testCreateAccount() {

        Account account = accountDAO.createAccount("1234");

        assertNotNull(account);
        assertTrue(account.getAccountId() > 0);
        assertEquals("1234", account.getPin());
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void testFindById() {

        Account created = accountDAO.createAccount("1234");
        Account found = accountDAO.findById(created.getAccountId());

        assertNotNull(found);
        assertEquals(created.getAccountId(), found.getAccountId());
        assertEquals("1234", found.getPin());
    }

    @Test
    void testAuthenticateWithCorrectPin() {

        Account account = accountDAO.createAccount("1234");
        boolean authenticated = accountDAO.authenticate(account.getAccountId(), "1234");
        assertTrue(authenticated);
    }

    @Test
    void testDeleteAccount() {
        Account created = accountDAO.createAccount("1234");
        boolean deleted = accountDAO.deleteAccount(created.getAccountId());
        Account found = accountDAO.findById(created.getAccountId());
        assertTrue(deleted);
        assertNull(found);
    }

    // negative tests
    @Test
    void testAuthenticateWithInvalidAccount() {

        boolean authenticated = accountDAO.authenticate(999999999, "1234");
        assertFalse(authenticated);
    }

    @Test
    void testFindByIdWithInvalidId() {
        Account account = accountDAO.findById(999999999);
        assertNull(account);
    }

    @Test
    void testAuthenticateWithWrongPin() {

        Account account = accountDAO.createAccount("1234");
        boolean authenticated = accountDAO.authenticate(account.getAccountId(), "9999");
        assertFalse(authenticated);
    }

}
