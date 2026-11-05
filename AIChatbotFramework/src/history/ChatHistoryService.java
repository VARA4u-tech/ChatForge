package history;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatHistoryService {
    private final File historyFile;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatHistoryService(String filePath) {
        this.historyFile = new File(filePath);
        createFileIfNeeded();
    }

    public String startConversation(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        String conversationId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(dateFormatter);
        String header = "CONVERSATION|" + conversationId + "|" + username.trim() + "|" + timestamp;
        appendLine(header);
        return conversationId;
    }

    public void saveMessage(String username, String conversationId, String role, String message) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Conversation ID is required.");
        }

        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required.");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required.");
        }

        String safeMessage = Base64.getEncoder().encodeToString(message.trim().getBytes(StandardCharsets.UTF_8));
        String timestamp = LocalDateTime.now().format(dateFormatter);
        String line = "MESSAGE|" + conversationId.trim() + "|" + username.trim() + "|" + timestamp + "|"
                + role.trim().toUpperCase() + "|" + safeMessage;
        appendLine(line);
    }

    public List<String> getConversationHistory(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        Map<String, String> conversationDates = new LinkedHashMap<>();
        Map<String, List<String>> conversationMessages = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length >= 4 && "CONVERSATION".equals(parts[0])) {
                    String cid = parts[1];
                    String storedUsername = parts[2];
                    String timestamp = parts[3];

                    if (storedUsername.equals(username.trim())) {
                        conversationDates.put(cid, timestamp);
                        conversationMessages.putIfAbsent(cid, new ArrayList<>());
                    }
                }

                if (parts.length >= 6 && "MESSAGE".equals(parts[0])) {
                    String cid = parts[1];
                    String storedUsername = parts[2];
                    String role = parts[4];
                    String encodedMessage = parts[5];

                    if (storedUsername.equals(username.trim())) {
                        String decodedMessage = decodeBase64(encodedMessage);
                        String formatted = role + ": " + decodedMessage;
                        conversationMessages.computeIfAbsent(cid, id -> new ArrayList<>()).add(formatted);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read chat history.", e);
        }

        List<String> output = new ArrayList<>();

        for (Map.Entry<String, String> entry : conversationDates.entrySet()) {
            String conversationId = entry.getKey();
            String timestamp = entry.getValue();
            List<String> messages = conversationMessages.getOrDefault(conversationId, new ArrayList<>());

            output.add("Conversation ID: " + conversationId + " | Date: " + timestamp);

            if (messages.isEmpty()) {
                output.add("No messages found.");
            } else {
                for (String message : messages) {
                    output.add(message);
                }
            }

            output.add("----------------------------------------");
        }

        return output;
    }

    public List<String> searchChatHistory(String username, String keyword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword is required.");
        }

        String searchTerm = keyword.trim().toLowerCase();
        List<String> matches = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            String currentConversationId = null;
            String currentDate = null;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length >= 4 && "CONVERSATION".equals(parts[0])) {
                    String storedUsername = parts[2];
                    if (storedUsername.equals(username.trim())) {
                        currentConversationId = parts[1];
                        currentDate = parts[3];
                    }
                    continue;
                }

                if (parts.length >= 6 && "MESSAGE".equals(parts[0])) {
                    String storedUsername = parts[2];
                    if (!storedUsername.equals(username.trim())) {
                        continue;
                    }

                    String role = parts[4];
                    String decodedMessage = decodeBase64(parts[5]);
                    if (decodedMessage.toLowerCase().contains(searchTerm)) {
                        matches.add("Conversation ID: " + currentConversationId + " | Date: " + currentDate);
                        matches.add(role + ": " + decodedMessage);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to search chat history.", e);
        }

        return matches;
    }

    public boolean deleteConversation(String username, String conversationId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Conversation ID is required.");
        }

        List<String> remainingLines = new ArrayList<>();
        boolean deleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length >= 4 && "CONVERSATION".equals(parts[0])) {
                    if (parts[1].equals(conversationId.trim()) && parts[2].equals(username.trim())) {
                        deleted = true;
                        continue;
                    }
                }

                if (parts.length >= 6 && "MESSAGE".equals(parts[0])) {
                    if (parts[1].equals(conversationId.trim()) && parts[2].equals(username.trim())) {
                        deleted = true;
                        continue;
                    }
                }

                remainingLines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete conversation.", e);
        }

        if (deleted) {
            rewriteFile(remainingLines);
        }

        return deleted;
    }

    private void createFileIfNeeded() {
        File parent = historyFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create chat history file.", e);
            }
        }
    }

    private void appendLine(String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chat history.", e);
        }
    }

    private void rewriteFile(List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update chat history.", e);
        }
    }

    private String decodeBase64(String value) {
        byte[] decoded = Base64.getDecoder().decode(value);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
