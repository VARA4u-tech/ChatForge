package admin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class FileAdminStore implements AdminStore {
    private final File adminFile;

    public FileAdminStore(String filePath) {
        this.adminFile = new File(filePath);
        createFileIfNeeded();
    }

    private void createFileIfNeeded() {
        File parent = adminFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!adminFile.exists()) {
            try {
                adminFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create admin storage file.", e);
            }
        }
    }

    @Override
    public void saveAdmin(Admin admin) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(adminFile, true))) {
            writer.write(formatAdmin(admin));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save admin.", e);
        }
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return findAdminByField("username", username);
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        return findAdminByField("email", email);
    }

    @Override
    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean hasAnyAdmin() {
        try (BufferedReader reader = new BufferedReader(new FileReader(adminFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read admin file.", e);
        }

        return false;
    }

    private Optional<Admin> findAdminByField(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(adminFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                Admin admin = parseAdmin(line);
                if (admin == null) {
                    continue;
                }

                if ("username".equalsIgnoreCase(fieldName)
                        && value.equalsIgnoreCase(admin.getUsername())) {
                    return Optional.of(admin);
                }

                if ("email".equalsIgnoreCase(fieldName)
                        && value.equalsIgnoreCase(admin.getEmail())) {
                    return Optional.of(admin);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read admin file.", e);
        }

        return Optional.empty();
    }

    private Admin parseAdmin(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 5) {
            return null;
        }

        Admin admin = new Admin();
        admin.setAdminId(parts[0]);
        admin.setFullName(parts[1]);
        admin.setUsername(parts[2]);
        admin.setEmail(parts[3]);
        admin.setPasswordHash(parts[4]);
        return admin;
    }

    private String formatAdmin(Admin admin) {
        return String.join("|",
                admin.getAdminId(),
                admin.getFullName(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getPasswordHash());
    }
}