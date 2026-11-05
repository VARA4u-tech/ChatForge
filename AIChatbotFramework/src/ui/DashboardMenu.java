package ui;

import java.util.List;
import java.util.Scanner;

import auth.User;
import core.ChatEngine;
import history.ChatHistoryService;

public class DashboardMenu {
    private final User user;
    private final Scanner scanner;
    private final ChatHistoryService chatHistoryService;

    public DashboardMenu(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
        this.chatHistoryService = new ChatHistoryService("data/chat_history.txt");
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n================================");
            System.out.println("Welcome, " + user.getFullName());
            System.out.println("================================");
            System.out.println("1. Start New Chat");
            System.out.println("2. View Chat History");
            System.out.println("3. Search Chat History");
            System.out.println("4. Delete Conversation");
            System.out.println("5. Logout");
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
                System.out.println("Invalid input. Please enter 1 to 5.");
                continue;
            }

            if (choice == 1) {
                startChat();
            } else if (choice == 2) {
                viewChatHistory();
            } else if (choice == 3) {
                searchChatHistory();
            } else if (choice == 4) {
                deleteConversation();
            } else if (choice == 5) {
                System.out.println("Logging out... Goodbye, " + user.getFullName() + "!");
                break;
            } else {
                System.out.println("Invalid option. Please choose 1 to 5.");
            }
        }
    }

    private void startChat() {
        ChatEngine chatEngine = new ChatEngine();
        String conversationId = chatHistoryService.startConversation(user.getUsername());

        System.out.println("\nChat started. Type 'help' to view commands or 'bye' to exit.");

        while (true) {
            System.out.print("You: ");
            String message = scanner.nextLine();

            if (message == null || message.trim().isEmpty()) {
                System.out.println("Please enter a valid message.");
                continue;
            }

            String response = chatEngine.processMessage(message);
            System.out.println("Bot: " + response);

            chatHistoryService.saveMessage(user.getUsername(), conversationId, "USER", message);
            chatHistoryService.saveMessage(user.getUsername(), conversationId, "BOT", response);

            if (message.trim().equalsIgnoreCase("bye")) {
                System.out.println("Chat saved successfully.");
                break;
            }
        }
    }

    private void viewChatHistory() {
        try {
            List<String> history = chatHistoryService.getConversationHistory(user.getUsername());

            if (history.isEmpty()) {
                System.out.println("No chat history found.");
                return;
            }

            for (String item : history) {
                System.out.println(item);
            }
        } catch (RuntimeException e) {
            System.out.println("Unable to load chat history: " + e.getMessage());
        }
    }

    private void searchChatHistory() {
        System.out.print("Enter keyword to search: ");
        String keyword = scanner.nextLine();

        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Please enter a valid keyword.");
            return;
        }

        try {
            List<String> results = chatHistoryService.searchChatHistory(user.getUsername(), keyword);

            if (results.isEmpty()) {
                System.out.println("No matching chat history found.");
                return;
            }

            for (String item : results) {
                System.out.println(item);
            }
        } catch (RuntimeException e) {
            System.out.println("Unable to search chat history: " + e.getMessage());
        }
    }

    private void deleteConversation() {
        System.out.print("Enter Conversation ID to delete: ");
        String conversationId = scanner.nextLine();

        if (conversationId == null || conversationId.trim().isEmpty()) {
            System.out.println("Conversation ID is required.");
            return;
        }

        try {
            boolean deleted = chatHistoryService.deleteConversation(user.getUsername(), conversationId.trim());

            if (deleted) {
                System.out.println("Conversation deleted successfully.");
            } else {
                System.out.println("Conversation not found.");
            }
        } catch (RuntimeException e) {
            System.out.println("Unable to delete conversation: " + e.getMessage());
        }
    }
}