import java.util.List;

public class AuthenticationService {

    private final List<User> users;

    public AuthenticationService(List<User> users) {
        this.users = List.copyOf(users);
    }

    // Check whether this email exists in the users file.
    public boolean emailExists(String email) {
        for (User user : users) {
            if (user.getName().equals(email)) {
                return true;
            }
        }
        return false;
    }

    // Authenticate an existing user.
    public boolean authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException(
                    "Email and password must not be null."
            );
        }

        for (User user : users) {
            if (user.getName().equals(email)) {
                return user.getPassword().equals(password);
            }
        }

        return false;
    }
}
