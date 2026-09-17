package com.bankofcli.bankofcli.dao;
import com.bankofcli.bankofcli.model.Account;
import com.bankofcli.bankofcli.model.Transaction;
import com.bankofcli.bankofcli.util.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankDAO {

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
                return new Account( // turn the database row into a Java obj
                        result.getLong("account_id"),
                        result.getString("pin"),
                        result.getBigDecimal("balance"),
                        result.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

        return false; // otherwise, return false
    }

//    public void updateBalance(long accountId, BigDecimal balance) {
//        String sql = """
//            UPDATE account
//            SET balance = ?
//            WHERE account_id = ?
//            """;
//
//        try (
//                Connection connection = DatabaseConnection.getConnection();
//                PreparedStatement statement = connection.prepareStatement(sql)
//        ) {
//
//            statement.setBigDecimal(1, balance);
//            statement.setLong(2, accountId);
//            statement.executeUpdate();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

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
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

//////////////////////////////////////////////////////// Transaction operations ////////////////////////////////////////////////////////
    public void createTransaction(long accountId, String type, BigDecimal amount, Long recipientId) {
        String sql = """
            INSERT INTO transactions
                (account_id, transaction_type, amount, recipient_id)
            VALUES (?, ?, ?, ?)
            """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, accountId);
            statement.setString(2, type);
            statement.setBigDecimal(3, amount);

            if (recipientId != null) {
                statement.setLong(4, recipientId);
            } else {
                statement.setNull(4, Types.BIGINT);
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean deposit(long accountId, BigDecimal amount){
        String sql = """
        UPDATE accounts
        SET balance = balance + ?
        WHERE account_id = ?
        """;

        try (
              Connection connection = DatabaseConnection.getConnection();
              PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setBigDecimal(1, amount);
            statement.setLong(2, accountId);
            int result = statement.executeUpdate();

            if (result > 0) {
                return true;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean withdraw(long accountId, BigDecimal amount){
        String sql = """
        UPDATE accounts
        SET balance = balance - ?
        WHERE account_id = ?
        AND balance >= ?
        """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setBigDecimal(1, amount);
            statement.setLong(2, accountId);
            statement.setBigDecimal(3, amount);
            int result = statement.executeUpdate();

            if (result > 0) {
                return true;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean transfer(long senderId, long recipientId, BigDecimal amount) {
        String Transfersql = """
        INSERT INTO transactions
            (account_id, transaction_type, amount, recipient_id)
        VALUES (?, 'TRANSFER', ?, ?)
        """;
        String Withdrawsql = """
        UPDATE accounts
        SET balance = balance - ?
        WHERE account_id = ?
        AND balance >= ?
        """;
        String Depositsql = """
        UPDATE accounts
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
                    return false;
                }
            }
            // record transaction
            try (
                    PreparedStatement statement = connection.prepareStatement(Transfersql);
            ) {
                statement.setLong(1, senderId);
                statement.setBigDecimal(2, amount);
                statement.setLong(3, recipientId);
                statement.executeUpdate();
            }


            // everything worked
            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
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
                       result.getBigDecimal("amount"),
                       recipientId,
                       result.getTimestamp("timestamp").toLocalDateTime()
               );

               transactions.add(transaction);
           }
       } catch (SQLException e){
           e.printStackTrace();
       }

       return transactions;
    }
}
