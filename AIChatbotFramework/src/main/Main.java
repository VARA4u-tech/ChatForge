
package main;

import java.util.Scanner;

import admin.AdminMenu;
import admin.AdminService;
import admin.FileAdminStore;
import auth.AuthService;
import auth.FileUserStore;
import ui.ConsoleMenu;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Initialize User Authentication
        FileUserStore userStore = new FileUserStore("data/users.txt");
        AuthService authService = new AuthService(userStore);

        // Initialize Admin Authentication
        FileAdminStore adminStore = new FileAdminStore("data/admins.txt");
        AdminService adminService = new AdminService(adminStore);

        // Main ChatForge Portal
        while (true) {

            System.out.println("\n================================");
            System.out.println("        CHATFORGE PORTAL");
            System.out.println("================================");
            System.out.println("1. User Portal");
            System.out.println("2. Admin Portal");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();

            if (input == null || input.trim().isEmpty()) {
                System.out.println("Please select a valid option.");
                continue;
            }

            int choice;

            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter 1, 2, or 3."
                );
                continue;
            }

            // User Portal
            if (choice == 1) {

                ConsoleMenu consoleMenu =
                        new ConsoleMenu(authService, scanner);

                consoleMenu.showMenu();

            }

            // Admin Portal
            else if (choice == 2) {

                AdminMenu adminMenu =
                        new AdminMenu(adminService, scanner);

                adminMenu.showMenu();

            }

            // Exit Application
            else if (choice == 3) {

                System.out.println("Goodbye!");
                break;

            }

            // Invalid Option
            else {

                System.out.println(
                        "Invalid option. Please choose 1, 2, or 3."
                );

            }
        }

        // Close Scanner only when the application exits
        scanner.close();
    }
}
