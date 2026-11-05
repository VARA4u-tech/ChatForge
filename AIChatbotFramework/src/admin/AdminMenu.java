package admin;

import java.util.Scanner;

public class AdminMenu {
    private final AdminService adminService;
    private final Scanner scanner;

    public AdminMenu(AdminService adminService, Scanner scanner) {
        this.adminService = adminService;
        this.scanner = scanner;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n================================");
            System.out.println("         ADMIN PANEL");
            System.out.println("================================");
            System.out.println("1. Admin Registration");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            int choice;

            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter 1, 2, or 3.");
                continue;
            }

            if (choice == 1) {
                if (adminService.hasAnyAdmin()) {
                    System.out.println("Admin registration is restricted. Please contact an existing administrator.");
                    continue;
                }
                registerAdmin();
            } else if (choice == 2) {
                loginAdmin();
            } else if (choice == 3) {
                System.out.println("Admin menu closed.");
                break;
            } else {
                System.out.println("Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }

    private void registerAdmin() {
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
            Admin admin = adminService.register(fullName, username, email, password, confirmPassword);
            System.out.println("Admin registration successful for: " + admin.getFullName());
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void loginAdmin() {
        System.out.print("Enter username or email: ");
        String usernameOrEmail = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            Admin admin = adminService.login(usernameOrEmail, password);
            System.out.println("Admin login successful! Welcome, " + admin.getFullName());
            System.out.println("Admin Dashboard Opened.");

            ui.AdminMenu dashboard = new ui.AdminMenu(admin, scanner);
            dashboard.showMenu();

        } catch (IllegalArgumentException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }
}