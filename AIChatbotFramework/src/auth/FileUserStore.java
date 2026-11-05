
package auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class FileUserStore implements UserStore {

    private final File userFile;

    public FileUserStore(String filePath) {
        this.userFile = new File(filePath);
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        File parentDir = userFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(
                        "Unable to create user storage file: "
                                + userFile.getAbsolutePath(), e);
            }
        }
    }

    @Override
    public void saveUser(User user) {
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(userFile, true))) {

            writer.write(formatUser(user));
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("Failed to save user to file.", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findUserByField("username", username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findUserByField("email", email);
    }

    @Override
    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

    private Optional<User> findUserByField(
            String fieldName, String value) {

        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(userFile))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                User user = parseUser(line);

                if (user == null) {
                    continue;
                }

                if ("username".equalsIgnoreCase(fieldName)
                        && value.equalsIgnoreCase(user.getUsername())) {

                    return Optional.of(user);
                }

                if ("email".equalsIgnoreCase(fieldName)
                        && value.equalsIgnoreCase(user.getEmail())) {

                    return Optional.of(user);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read user file.", e);
        }

        return Optional.empty();
    }

    private User parseUser(String line) {

        String[] parts = line.split("\\|", -1);

        if (parts.length != 5) {
            return null;
        }

        User user = new User();

        user.setUserId(parts[0]);
        user.setFullName(parts[1]);
        user.setUsername(parts[2]);
        user.setEmail(parts[3]);
        user.setPasswordHash(parts[4]);

        return user;
    }

    private String formatUser(User user) {

        validateField(user.getUserId());
        validateField(user.getFullName());
        validateField(user.getUsername());
        validateField(user.getEmail());
        validateField(user.getPasswordHash());

        return String.join("|",
                user.getUserId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash()
        );
    }

    private void validateField(String value) {

        if (value == null
                || value.contains("|")
                || value.contains("\n")
                || value.contains("\r")) {

            throw new IllegalArgumentException(
                    "Invalid user field: contains unsupported characters.");
        }
    }
}
