package com.bankofcli.bankofcli;
import com.bankofcli.bankofcli.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("Connected");

        } catch (SQLException e) {

            System.out.println("Connection failed");
            e.printStackTrace();


        }
    }
}
