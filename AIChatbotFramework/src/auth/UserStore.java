package auth;

import java.util.Optional;

public interface UserStore {

    void saveUser(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean usernameExists(String username);

    boolean emailExists(String email);
}
