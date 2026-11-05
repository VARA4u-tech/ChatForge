package admin;

import java.util.Optional;

public interface AdminStore {
    void saveAdmin(Admin admin);

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    boolean usernameExists(String username);

    boolean emailExists(String email);

    boolean hasAnyAdmin();
}