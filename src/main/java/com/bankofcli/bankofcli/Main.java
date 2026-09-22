package com.bankofcli.bankofcli;
import com.bankofcli.bankofcli.model.Account;
import com.bankofcli.bankofcli.model.Transaction;
import com.bankofcli.bankofcli.service.BankService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BankService bankService = new BankService();
    public static void main(String[] args) {
        boolean running = true;

        // opening screen
        System.out.println();
        System.out.println("                    BANK OF CLI");
        System.out.println();
        System.out.println("                       $$$");
        System.out.println("                      $$$$$");
        System.out.println("                     $$$$$$$");
        System.out.println("                ╔═══════════════╗");
        System.out.println("                ║  BANK OF CLI  ║");
        System.out.println("        ╔═══════╩═══════════════╩═══════╗");
        System.out.println("        ║                               ║");
        System.out.println("        ║   ┌─────┐  ┌─────┐  ┌─────┐  ║");
        System.out.println("        ║   │     │  │     │  │     │  ║");
        System.out.println("        ║   │     │  │     │  │     │  ║");
        System.out.println("        ║   │     │  │     │  │     │  ║");
        System.out.println("        ║   └─────┘  └─────┘  └─────┘  ║");
        System.out.println("        ║                               ║");
        System.out.println("        ║          ┌─────────┐          ║");
        System.out.println("        ║          │         │          ║");
        System.out.println("        ║          │         │          ║");
        System.out.println("        ║          │         │          ║");
        System.out.println("        ╚══════════╧═════════╧══════════╝");
        System.out.println("        ═════════════════════════════════");
        System.out.println();

        // application loop
        while (running) {
            System.out.println("\n1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("\nSelect an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    createAccount();
                    break;
                case "2":
                    login();
                    break;
                case "3":
                    running = false;
                    System.out.println("\nThank you for using Bank of CLI!");
                    break;
                default:
                    System.out.println("\nInvalid option. Please try again.");
            }
        }
        scanner.close();
    }


    //////////////////////////////////////////////////////// CREATE ACCOUNT ////////////////////////////////////////////////////////
    private static void createAccount() {
        System.out.println("\n--- Create Account ---");
        System.out.print("Create a 4-digit PIN: ");
        String pin = scanner.nextLine();

        try {
            Account account = bankService.register(pin);

            System.out.println("\nAccount created successfully!");
            System.out.println("Your Account ID is: " + account.getAccountId());
            System.out.println("Please remember your Account ID.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////// LOGIN ////////////////////////////////////////////////////////
    private static void login() {
        System.out.println("\n--- Login ---");

        try {
            System.out.print("Account ID: ");
            long accountId = Long.parseLong(scanner.nextLine());
            System.out.print("PIN: ");
            String pin = scanner.nextLine();

            Account account = bankService.login(accountId, pin);
            if (account != null) {
                System.out.println("\nLogin successful!");
                accountMenu(account);
            } else {
                System.out.println("\nInvalid Account ID or PIN.");
            }
        } catch (NumberFormatException e) {
            System.out.println("\nAccount ID must be a number.");
        }
    }

    //////////////////////////////////////////////////////// ACCOUNT MENU ////////////////////////////////////////////////////////
    private static void accountMenu(Account account) {
        boolean loggedIn = true;
        while (loggedIn) {
            printAccountMenu(account.getAccountId());
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewBalance(account.getAccountId());
                    break;
                case "2":
                    deposit(account.getAccountId());
                    break;
                case "3":
                    withdraw(account.getAccountId());
                    break;
                case "4":
                    transfer(account.getAccountId());
                    break;
                case "5":
                    showTransactionHistory(account.getAccountId());
                    break;
                case "6":
                    loggedIn = false;
                    System.out.println("\n✓ Logged out successfully.");
                    break;

                default:
                    System.out.println("\n✗ Invalid option. Please try again.");
            }
        }
    }

    private static void printAccountMenu(long accountId) {
        BigDecimal balance = bankService.obtainBalance(accountId);

        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║            💰 BANK OF CLI           ║");
        System.out.println("╠══════════════════════════════════════╣");

        System.out.printf("║  Account: %-27s║%n", "#" + accountId);
        System.out.printf("║  Balance: $%,-26.2f║%n", balance);

        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        System.out.println("  [1]  💵 View Balance");
        System.out.println("  [2]  ＋ Deposit");
        System.out.println("  [3]  － Withdraw");
        System.out.println("  [4]  ⇄  Transfer");
        System.out.println("  [5]  📋 Transaction History");
        System.out.println("  [6]  ↩  Logout");

        System.out.println();
        System.out.print("  ➜ Select an option: ");
    }

    //////////////////////////////////////////////////////// VIEW BALANCE ////////////////////////////////////////////////////////
    private static void viewBalance(long accountId) {
        try {
            BigDecimal balance = bankService.obtainBalance(accountId);
            System.out.printf("\nCurrent balance: $%.2f%n", balance);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////// DEPOSIT ////////////////////////////////////////////////////////
    private static void deposit(long accountId) {
        System.out.println("\n--- Deposit ---");
        try {
            System.out.print("Enter deposit amount: $");
            BigDecimal amount = new BigDecimal(scanner.nextLine());
            bankService.deposit(accountId, amount);
            BigDecimal newBalance = bankService.obtainBalance(accountId);
            System.out.println("Deposit successful!");
            System.out.printf("New balance: $%.2f%n", newBalance);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////// WITHDRAW ////////////////////////////////////////////////////////
    private static void withdraw(long accountId) {
        System.out.println("\n--- Withdraw ---");
        try {
            System.out.print("Enter withdrawal amount: $");
            BigDecimal amount = new BigDecimal(scanner.nextLine());
            bankService.withdraw(accountId, amount);
            BigDecimal newBalance = bankService.obtainBalance(accountId);
            System.out.println("Withdrawal successful!");
            System.out.printf("New balance: $%.2f%n", newBalance);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////// TRANSFER ////////////////////////////////////////////////////////
    private static void transfer(long senderId) {
        System.out.println("\n--- Transfer ---");
        try {
            System.out.print("Recipient Account ID: ");
            long recipientId = Long.parseLong(scanner.nextLine());
            System.out.print("Transfer amount: $");
            BigDecimal amount = new BigDecimal(scanner.nextLine());
            bankService.transfer(senderId, recipientId, amount);
            BigDecimal newBalance = bankService.obtainBalance(senderId);
            System.out.println("Transfer successful!");
            System.out.printf("New balance: $%.2f%n", newBalance);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid Account ID and amount.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////// TRANSACTION HISTORY ////////////////////////////////////////////////////////
    private static void showTransactionHistory(long accountId) {
        System.out.println("\n--- Transaction History ---");
        try {
            List<Transaction> transactions =
                    bankService.getTransactionHistory(accountId);
            if (transactions.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }
            for (Transaction transaction : transactions) {
                System.out.println("-----------------------------");
                System.out.println("Transaction ID: " + transaction.getTransactionId());
                System.out.println("Type: " + transaction.getTransactionType());
                System.out.printf("Amount: $%.2f%n", transaction.getAmount());
                if (transaction.getRecipientId() != null) {
                    System.out.println("Recipient Account: " + transaction.getRecipientId());
                }

                System.out.println("Date: " + transaction.getTimestamp());
            }
            System.out.println("-----------------------------");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}