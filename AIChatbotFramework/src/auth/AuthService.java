package auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AuthService {

    private final UserStore userStore;
    private final int passwordIterations = 120_000;
    private final int keyLength = 256;

    public AuthService(UserStore userStore) {
        this.userStore = userStore;
    }

    public User signup(String fullName, String username, String email, String password, String confirmPassword) {
        validateRequiredFields(fullName, username, email, password, confirmPassword);

        String trimmedFullName = fullName.trim();
        String trimmedUsername = username.trim();
        String trimmedEmail = email.trim();

        if (userStore.usernameExists(trimmedUsername)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        if (userStore.emailExists(trimmedEmail)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }

        validatePassword(password);

        String passwordHash = hashPassword(password);

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setFullName(trimmedFullName);
        user.setUsername(trimmedUsername);
        user.setEmail(trimmedEmail);
        user.setPasswordHash(passwordHash);

        userStore.saveUser(user);
        return user;
    }

    public User login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email is required.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        String identifier = usernameOrEmail.trim();
        Optional<User> userOpt = findUserByUsernameOrEmail(identifier);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username/email or password.");
        }

        User user = userOpt.get();
        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username/email or password.");
        }

        return user;
    }

    private Optional<User> findUserByUsernameOrEmail(String identifier) {
        Optional<User> byUsername = userStore.findByUsername(identifier);
        if (byUsername.isPresent()) {
            return byUsername;
        }

        return userStore.findByEmail(identifier);
    }

    private void validateRequiredFields(String fullName, String username, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("Confirm password is required.");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                hasLetter = true;
            }
            if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }

        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("Password must contain both letters and numbers.");
        }
    }

    private String hashPassword(String password) {
        byte[] salt = generateSalt();
        byte[] hash = pbkdf2(password.toCharArray(), salt, passwordIterations, keyLength);

        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash) + ":" + passwordIterations;
    }

    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            int iterations = Integer.parseInt(parts[2]);

            byte[] computedHash = pbkdf2(password.toCharArray(), salt, iterations, hash.length * 8);
            return MessageDigest.isEqual(hash, computedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing failed.", e);
        }
    }
}
