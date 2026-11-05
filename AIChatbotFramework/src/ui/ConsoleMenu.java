
package ui;

import java.util.Scanner;

import auth.AuthService;
import auth.User;

public class ConsoleMenu {

    private final AuthService authService;
    private final Scanner scanner;

    // Constructor receives shared AuthService and Scanner
    public ConsoleMenu(AuthService authService, Scanner scanner) {
        this.authService = authService;
        this.scanner = scanner;
    }

    public void showMenu() {

        while (true) {
            System.out.println("\n================================");
            System.out.println("        WELCOME TO CHATFORGE");
            System.out.println("================================");
            System.out.println("1. User Signup");
            System.out.println("2. User Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choiceInput = scanner.nextLine();

            if (choiceInput == null || choiceInput.trim().isEmpty()) {
                System.out.println("Please select a valid option.");
                continue;
            }

            int choice;

            try {
                choice = Integer.parseInt(choiceInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter 1, 2, or 3.");
                continue;
            }

            if (choice == 1) {

                System.out.print("Enter full name: ");
                String fullName = scanner.nextLine();

                System.out.print("Enter username: ");
                String username = scanner.nextLine();

                System.out.print("Enter email: ");
                String email = scanner.nextLine();

                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                System.out.print("Confirm password: ");
                String confirmPassword = scanner.nextLine();

                try {
                    authService.signup(
                            fullName,
                            username,
                            email,
                            password,
                            confirmPassword
                    );

                    System.out.println(
                            "Signup successful! Welcome to ChatForge."
                    );

                } catch (IllegalArgumentException e) {
                    System.out.println("Signup failed: " + e.getMessage());
                }

            } else if (choice == 2) {

                System.out.print("Enter username or email: ");
                String usernameOrEmail = scanner.nextLine();

                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                try {
                    User loggedInUser = authService.login(
                            usernameOrEmail,
                            password
                    );

                    System.out.println(
                            "Login successful! You are now logged in."
                    );

                    DashboardMenu dashboardMenu =
                            new DashboardMenu(loggedInUser, scanner);

                    dashboardMenu.showMenu();

                } catch (IllegalArgumentException e) {
                    System.out.println("Login failed: " + e.getMessage());
                }

            } else if (choice == 3) {

                System.out.println(
                        "Thank you for using ChatForge. Goodbye!"
                );

                break;

            } else {
                System.out.println(
                        "Invalid option selected. Please choose 1, 2, or 3."
                );
            }
        }
    }
}
