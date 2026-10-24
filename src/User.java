import java.util.regex.Pattern;

/**
 * A system user. An instance is created only if both the username
 * and the password are valid; otherwise an exception is thrown.
 */
public class User {

    // error messages (from the lab's table)
    static final String INVALID_EMAIL = "Please enter a valid Email as username";
    static final String USERNAME_TOO_LONG = "Username is too long, try something shorter";
    static final String PASSWORD_TOO_SHORT = "Your password is too short, add more characters";
    static final String PASSWORD_TOO_LONG = "Your password is too long, try a shorter one";
    static final String INVALID_PASSWORD = "Please enter a valid password";

    private static final int MAX_EMAIL_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 12;

    /*
     * Part 1: letters, digits and . _ % + -
     * Part 2: after '@', starts with a letter/digit, then letters, digits, '.', '-'
     * Part 3: after '.', at least two letters
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}$");

    // symbols from the number row of the keyboard
    private static final String SYMBOLS = "!@#$%^&*()_+-=~`";
    private static final Pattern PASSWORD_ALLOWED =
            Pattern.compile("^[A-Za-z0-9" + Pattern.quote(SYMBOLS) + "]+$");

    private final String name;
    private final String password;

    public User(String name, String password) {
        validateUsername(name);
        validatePassword(password);
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    private static void validateUsername(String name) {
        if (name == null) {
            throw new IllegalArgumentException(INVALID_EMAIL);
        }
        if (name.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException(USERNAME_TOO_LONG);
        }
        if (!EMAIL_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(INVALID_EMAIL);
        }
    }

    private static void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException(INVALID_PASSWORD);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(PASSWORD_TOO_SHORT);
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(PASSWORD_TOO_LONG);
        }
        if (!PASSWORD_ALLOWED.matcher(password).matches()) {
            throw new IllegalArgumentException(INVALID_PASSWORD);
        }

        boolean hasLetter = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (SYMBOLS.indexOf(c) >= 0) {
                hasSymbol = true;
            }
        }
        if (!hasLetter || !hasDigit || !hasSymbol) {
            throw new IllegalArgumentException(INVALID_PASSWORD);
        }
    }

    @Override
    public String toString() {
        return name + " " + password;
    }
}