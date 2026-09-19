package com.bankofcli.bankofcli.dao;
import com.bankofcli.bankofcli.model.Account;
import com.bankofcli.bankofcli.model.Transaction;
import com.bankofcli.bankofcli.util.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankDAO {
    private static final Logger logger = LoggerFactory.getLogger(BankDAO.class);

    //////////////////////////////////////////////////////// Account operations ////////////////////////////////////////////////////////
    public Account createAccount(String pin) {
        String sql = """
            INSERT INTO account (pin, balance)
            VALUES (?, ?)
            RETURNING *
            """; // creates a new row in the account table. After creating the account, return the newly created info

        try ( // to close db connection and prepared statement automatically
              Connection connection = DatabaseConnection.getConnection(); // connect to postgreSQL
              PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, pin); // fills first ? (pin placeholder)
            statement.setBigDecimal(2, BigDecimal.ZERO); // fills second ? (balance placeholder)
            ResultSet result = statement.executeQuery(); // Java sends the query to postgreSQL

            if (result.next()) { // if postgreSQL return a row, read the individual columns
                Account account =  new Account( // turn the database row into a Java obj
                        result.getLong("account_id"),
                        result.getString("pin"),
                        result.getBigDecimal("balance"),
                        result.getTimestamp("created_at").toLocalDateTime()
                );
                logger.info("Account {} created successfully", account.getAccountId());
                return account;
            }

        } catch (SQLException e) {
            logger.error("Database error while creating account", e);
        }

        return null;
    }

    public Account findById(long accountId) {
        String sql = """
            SELECT *
            FROM account
            WHERE account_id = ?
            """;
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, accountId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return new Account(
                        result.getLong("account_id"),
                        result.getString("pin"),
                        result.getBigDecimal("balance"),
                        result.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            logger.error("Database error while finding account {}", accountId, e);
        }

        return null;
    }

    public boolean authenticate(long accountId, String pin) {
        String sql = """
            SELECT *
            FROM account
            WHERE account_id = ? AND pin = ?
            """;
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, accountId);
            statement.setString(2, pin);
            ResultSet result = statement.executeQuery();

            if (result.next()) { // if a matching account exists
                return true;
            }

        } catch (SQLException e) {
            logger.error("Database error while authenticating account {}", accountId, e);
        }

        return false; // otherwise, return false
    }

    public boolean deleteAccount(long accountId) {
        String sql = """
            DELETE FROM account
            WHERE account_id = ?
            """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, accountId);
            int result = statement.executeUpdate(); // returns the num of rows affected by the delete

            if (result > 0) {
                logger.info("Account {} deleted successfully", accountId);
                return true;
            }
            logger.warn("Account {} could not be deleted because it was not found", accountId);
            return false;

        } catch (SQLException e) {
            logger.error("Database error while deleting account {}", accountId, e);
            return false;
        }
    }

    public boolean deposit(long accountId, BigDecimal amount) {
        String Accountsql = """
        UPDATE account
        SET balance = balance + ?
        WHERE account_id = ?
        """;
        String Transactionsql = """
        INSERT INTO transactions
            (account_id, transaction_type, amount, recipient_id)
        VALUES (?, 'DEPOSIT', ?, ?)
        """;

        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            // update account balance
            try (PreparedStatement statement = connection.prepareStatement(Accountsql)) {
                statement.setBigDecimal(1, amount);
                statement.setLong(2, accountId);
                int result = statement.executeUpdate();

                if (result == 0) {
                    connection.rollback();
                    logger.warn("Deposit failed for account {}. Transaction rolled back", accountId);
                    return false;
                }
            }

            // record deposit transaction
            try (PreparedStatement statement = connection.prepareStatement(Transactionsql)) {
                statement.setLong(1, accountId);
                statement.setBigDecimal(2, amount);
                statement.setNull(3, java.sql.Types.BIGINT);
                statement.executeUpdate();
            }

            // both succeeded
            connection.commit();
            logger.info("Deposit of {} completed for account {}", amount, accountId);

            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warn("Deposit transaction rolled back for account {}", accountId);

                } catch (SQLException rollbackException) {
                    logger.error("Failed to rollback deposit for account {}", accountId, rollbackException);
                }
            }
            logger.error("Database error while processing deposit for account {}", accountId, e);
            return false;

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Failed to close database connection after deposit", e);
                }
            }
        }
    }

    public boolean withdraw(long accountId, BigDecimal amount){
        String Accountsql = """
        UPDATE account
        SET balance = balance - ?
        WHERE account_id = ?
        AND balance >= ?
        """;
        String Transactionsql = """
        INSERT INTO transactions
            (account_id, transaction_type, amount, recipient_id)
        VALUES (?, 'WITHDRAWAL', ?, ?)
        """;
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            // update account balance
            try (PreparedStatement statement = connection.prepareStatement(Accountsql)) {
                statement.setBigDecimal(1, amount);
                statement.setLong(2, accountId);
                statement.setBigDecimal(3, amount);
                int result = statement.executeUpdate();

                if (result == 0) {
                    connection.rollback();
                    logger.debug("Withdrawal not completed for account {}: account not found or insufficient funds", accountId);
                    return false;
                }
            }

            // record withdrawal transaction
            try (PreparedStatement statement = connection.prepareStatement(Transactionsql)) {
                statement.setLong(1, accountId);
                statement.setBigDecimal(2, amount);
                statement.setNull(3, java.sql.Types.BIGINT);
                statement.executeUpdate();
            }

            // both succeeded
            connection.commit();
            logger.info("Withdrawal of {} completed for account {}", amount, accountId);
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warn("Withdrawal transaction rolled back for account {}", accountId);

                } catch (SQLException rollbackException) {
                    logger.error("Failed to rollback withdrawal for account {}", accountId, rollbackException);
                }
            }
            logger.error("Database error while processing withdrawal for account {}", accountId, e);
            return false;

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Failed to close database connection after withdrawal", e);
                }
            }
        }

    }

    public boolean transfer(long senderId, long recipientId, BigDecimal amount) {
        String Transfersql = """
        INSERT INTO transactions
            (account_id, transaction_type, amount, recipient_id)
        VALUES (?, 'TRANSFER', ?, ?)
        """;
        String Withdrawsql = """
        UPDATE account
        SET balance = balance - ?
        WHERE account_id = ?
        AND balance >= ?
        """;
        String Depositsql = """
        UPDATE account
        SET balance = balance + ?
        WHERE account_id = ?
        """;

        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false); // start transaction
            // remove money from sender
            try (
                    PreparedStatement statement = connection.prepareStatement(Withdrawsql);
            ) {
                statement.setBigDecimal(1, amount);
                statement.setLong(2, senderId);
                statement.setBigDecimal(3, amount);
                int result = statement.executeUpdate();

                if (result == 0) {
                    connection.rollback();
                    logger.warn("Transfer from account {} failed while withdrawing funds", senderId);
                    return false;
                }
            }
            // add money to recipient
            try (
                    PreparedStatement statement = connection.prepareStatement(Depositsql);
            ) {
                statement.setBigDecimal(1, amount);
                statement.setLong(2, recipientId);
                int result = statement.executeUpdate();

                if (result == 0) {
                    connection.rollback();
                    logger.warn("Transfer from account {} to account {} failed. Transaction rolled back", senderId, recipientId);
                    return false;
                }
            }
            // record transaction
            try (PreparedStatement statement = connection.prepareStatement(Transfersql);) {
                statement.setLong(1, senderId);
                statement.setBigDecimal(2, amount);
                statement.setLong(3, recipientId);
                statement.executeUpdate();
            }

            // everything worked
            connection.commit();
            logger.info("Transfer of {} from account {} to account {} completed successfully", amount, senderId, recipientId);
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warn("Transfer from account {} to account {} rolled back", senderId, recipientId);
                } catch (SQLException rollbackException) {
                    logger.error("Failed to rollback transfer from account {} to account {}", senderId, recipientId, rollbackException);
                }
            }
            logger.error("Database error during transfer from account {} to account {}", senderId, recipientId, e);
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Failed to close database connection after transfer", e);
                }
            }
        }
    }

    public List<Transaction> getTransactionHistory(long accountId) {
       List<Transaction> transactions = new ArrayList<>();
       String sql = """
               SELECT *
               FROM transactions
               WHERE account_id = ? OR recipient_id = ?
               ORDER BY timestamp DESC
               """;

       try (
               Connection connection = DatabaseConnection.getConnection();
               PreparedStatement statement = connection.prepareStatement(sql)
       ) {
           statement.setLong(1, accountId);
           statement.setLong(2, accountId);
           ResultSet result = statement.executeQuery();

           while (result.next()) {
               Long recipientId = result.getObject("recipient_id", Long.class);

               Transaction transaction = new Transaction(
                       result.getLong("transaction_id"),
                       result.getLong("account_id"),
                       result.getString("transaction_type"),
                       result.getBigDecimal("amount"), recipientId,
                       result.getTimestamp("timestamp").toLocalDateTime()
               );

               transactions.add(transaction);
           }
       } catch (SQLException e){
           logger.error("Database error while retrieving transaction history for account {}", accountId, e);
       }
       return transactions;
    }
}
