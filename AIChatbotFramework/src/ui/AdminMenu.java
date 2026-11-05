package ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import admin.Admin;
import auth.FileUserStore;
import auth.User;
import history.ChatHistoryService;

public class AdminMenu {
    private final Admin admin;
    private final Scanner scanner;
    private final FileUserStore userStore;
    private final ChatHistoryService chatHistoryService;

    public AdminMenu(Admin admin, Scanner scanner) {
        this.admin = admin;
        this.scanner = scanner;
        this.userStore = new FileUserStore("data/users.txt");
        this.chatHistoryService = new ChatHistoryService("data/chat_history.txt");
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n================================");
            System.out.println("          ADMIN DASHBOARD");
            System.out.println("================================");
            System.out.println("1. View All Registered Users");
            System.out.println("2. Search User");
            System.out.println("3. View User Details");
            System.out.println("4. Delete User");
            System.out.println("5. View All Chat History");
            System.out.println("6. Search User Chat History");
            System.out.println("7. Delete User Chat History");
            System.out.println("8. View Admin Profile");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");

            String choiceInput = scanner.nextLine();
            if (choiceInput == null || choiceInput.trim().isEmpty()) {
                System.out.println("Please enter a valid option.");
                continue;
            }

            int choice;
            try {
                choice = Integer.parseInt(choiceInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number from 1 to 9.");
                continue;
            }

            if (choice == 1) {
                viewAllUsers();
            } else if (choice == 2) {
                searchUser();
            } else if (choice == 3) {
                viewUserDetails();
            } else if (choice == 4) {
                deleteUser();
            } else if (choice == 5) {
                viewAllChatHistory();
            } else if (choice == 6) {
                searchUserChatHistory();
            } else if (choice == 7) {
                deleteUserChatHistory();
            } else if (choice == 8) {
                viewAdminProfile();
            } else if (choice == 9) {
                System.out.println("Admin logout successful. Returning to Admin Panel.");
                break;
            } else {
                System.out.println("Invalid option. Please choose a valid menu option.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = loadUsers();

        if (users.isEmpty()) {
            System.out.println("No registered users found.");
            return;
        }

        System.out.println("\n========== REGISTERED USERS ==========");
        for (User user : users) {
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Full Name: " + user.getFullName());
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("--------------------------------------");
        }
    }

    private void searchUser() {
        System.out.print("Enter username, email, or full name to search: ");
        String keyword = scanner.nextLine();

        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Search keyword is required.");
            return;
        }

        List<User> users = loadUsers();
        boolean found = false;

        for (User user : users) {
            if (user.getUsername().toLowerCase().contains(keyword.toLowerCase())
                    || user.getEmail().toLowerCase().contains(keyword.toLowerCase())
                    || user.getFullName().toLowerCase().contains(keyword.toLowerCase())) {
                System.out.println("\nUser found:");
                printUser(user);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching users found.");
        }
    }

    private void viewUserDetails() {
        System.out.print("Enter username or email: ");
        String identifier = scanner.nextLine();

        User user = findUserByUsernameOrEmail(identifier);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        printUser(user);
    }

    private void deleteUser() {
        System.out.print("Enter username or email to delete: ");
        String identifier = scanner.nextLine();

        User user = findUserByUsernameOrEmail(identifier);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Are you sure you want to delete user '" + user.getUsername() + "'? (y/n): ");
        String answer = scanner.nextLine();

        if (!"y".equalsIgnoreCase(answer.trim())) {
            System.out.println("User deletion cancelled.");
            return;
        }

        List<User> users = loadUsers();
        List<User> remaining = new ArrayList<>();

        for (User currentUser : users) {
            if (!currentUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                remaining.add(currentUser);
            }
        }

        rewriteUserFile(remaining);
        System.out.println("User deleted successfully: " + user.getUsername());
    }

    private void viewAllChatHistory() {
        File chatFile = new File("data/chat_history.txt");
        if (!chatFile.exists()) {
            System.out.println("No chat history found.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(chatFile))) {
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                found = true;
                String[] parts = line.split("\\|", -1);

                if (parts.length >= 4 && "CONVERSATION".equals(parts[0])) {
                    System.out.println("\nConversation ID: " + parts[1]);
                    System.out.println("Username: " + parts[2]);
                    System.out.println("Date: " + parts[3]);
                    continue;
                }

                if (parts.length >= 6 && "MESSAGE".equals(parts[0])) {
                    String decoded = decodeBase64(parts[5]);
                    System.out.println(parts[4] + ": " + decoded);
                }
            }

            if (!found) {
                System.out.println("No chat history found.");
            }
        } catch (IOException e) {
            System.out.println("Unable to read chat history: " + e.getMessage());
        }
    }

    private void searchUserChatHistory() {
        System.out.print("Enter username or email to search chat history: ");
        String identifier = scanner.nextLine();

        System.out.print("Enter keyword to search inside messages (optional): ");
        String keyword = scanner.nextLine();

        User user = findUserByUsernameOrEmail(identifier);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        try {
            List<String> results;
            if (keyword == null || keyword.trim().isEmpty()) {
                results = chatHistoryService.getConversationHistory(user.getUsername());
            } else {
                results = chatHistoryService.searchChatHistory(user.getUsername(), keyword);
            }

            if (results.isEmpty()) {
                System.out.println("No chat history found for this user.");
                return;
            }

            for (String item : results) {
                System.out.println(item);
            }
        } catch (RuntimeException e) {
            System.out.println("Unable to search chat history: " + e.getMessage());
        }
    }

    private void deleteUserChatHistory() {
        System.out.print("Enter Conversation ID to delete: ");
        String conversationId = scanner.nextLine();

        if (conversationId == null || conversationId.trim().isEmpty()) {
            System.out.println("Conversation ID is required.");
            return;
        }

        System.out.print("Are you sure you want to delete this conversation? (y/n): ");
        String answer = scanner.nextLine();
        if (!"y".equalsIgnoreCase(answer.trim())) {
            System.out.println("Conversation deletion cancelled.");
            return;
        }

        File chatFile = new File("data/chat_history.txt");
        if (!chatFile.exists()) {
            System.out.println("No chat history file found.");
            return;
        }

        List<String> remainingLines = new ArrayList<>();
        boolean deleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(chatFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 2 && "CONVERSATION".equals(parts[0]) && parts[1].equals(conversationId.trim())) {
                    deleted = true;
                    continue;
                }

                if (parts.length >= 2 && "MESSAGE".equals(parts[0]) && parts[1].equals(conversationId.trim())) {
                    deleted = true;
                    continue;
                }

                remainingLines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Unable to delete chat history: " + e.getMessage());
            return;
        }

        if (!deleted) {
            System.out.println("Conversation not found.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatFile))) {
            for (String line : remainingLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to update chat history file: " + e.getMessage());
        }

        System.out.println("Conversation deleted successfully.");
    }

    private void viewAdminProfile() {
        System.out.println("\n========== ADMIN PROFILE ==========");
        System.out.println("Admin ID: " + admin.getAdminId());
        System.out.println("Full Name: " + admin.getFullName());
        System.out.println("Username: " + admin.getUsername());
        System.out.println("Email: " + admin.getEmail());
    }

    private List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File("data/users.txt");

        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length != 5) {
                    continue;
                }

                User user = new User();
                user.setUserId(parts[0]);
                user.setFullName(parts[1]);
                user.setUsername(parts[2]);
                user.setEmail(parts[3]);
                user.setPasswordHash(parts[4]);
                users.add(user);
            }
        } catch (IOException e) {
            System.out.println("Unable to load user data: " + e.getMessage());
        }

        return users;
    }

    private User findUserByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }

        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(identifier.trim())
                    || user.getEmail().equalsIgnoreCase(identifier.trim())) {
                return user;
            }
        }

        return null;
    }

    private void printUser(User user) {
        System.out.println("User ID: " + user.getUserId());
        System.out.println("Full Name: " + user.getFullName());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
    }

    private void rewriteUserFile(List<User> users) {
        File file = new File("data/users.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (User user : users) {
                writer.write(user.getUserId() + "|" + user.getFullName() + "|"
                        + user.getUsername() + "|" + user.getEmail() + "|" + user.getPasswordHash());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Unable to rewrite user file: " + e.getMessage());
        }
    }

    private String decodeBase64(String value) {
        byte[] decoded = Base64.getDecoder().decode(value);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
