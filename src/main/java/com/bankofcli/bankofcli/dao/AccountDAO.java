package com.bankofcli.bankofcli.dao;
import com.bankofcli.bankofcli.model.Account;
import com.bankofcli.bankofcli.util.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class AccountDAO {

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

    public void updateBalance(long accountId, BigDecimal balance) {
        String sql = """
            UPDATE account
            SET balance = ?
            WHERE account_id = ?
            """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setBigDecimal(1, balance);
            statement.setLong(2, accountId);
            int result = statement.executeUpdate();

            if (result > 0) { // if a matching account exists
                System.out.print("Balance updated");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
