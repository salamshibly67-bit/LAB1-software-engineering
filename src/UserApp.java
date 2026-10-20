import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;
/**
 * Lab 1 - Software Engineering, Winter 2026.
 * Reads users from users.txt, validates each line, keeps the valid users
 * in an ArrayList, prints the invalid lines with an error message,
 * and finally prints the valid users sorted by username.
 */
public class UserApp {
        private static final String INPUT_FILE = "users.txt";

        public static void main(String[] args) {
            // 3.1 ArrayList of type User
            ArrayList<User> users = new ArrayList<>();

            Scanner reader = openInput(args);
            if (reader == null) {
                return;
            }

            // 3.2 read the file line by line
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.trim().isEmpty()) {
                    continue; // skip empty lines
                }

                // username and password are separated by one or more spaces
                String[] parts = line.trim().split("\\s+");
                String username = parts[0];
                String password = (parts.length == 2) ? parts[1] : null;

                try {
                    if (password == null) {
                        // missing password, or password that contains spaces
                        throw new IllegalArgumentException(User.INVALID_PASSWORD);
                    }
                    users.add(new User(username, password));
                } catch (IllegalArgumentException e) {
                    printError(line, e.getMessage());
                }
            }
            reader.close();

            // 3.3 print the valid users sorted by username
            Collections.sort(users, (u1, u2) -> u1.getName().compareTo(u2.getName()));
            for (User user : users) {
                System.out.println(user);
            }
        }

        /** Prints an invalid line together with its error message. */
        private static void printError(String line, String message) {
            System.out.println(line);
            System.out.println(message);
        }

        /**
         * Opens the input: a file name given as an argument, otherwise users.txt,
         * otherwise standard input (supports running with input redirection).
         */
        private static Scanner openInput(String[] args) {
            String fileName = (args.length > 0) ? args[0] : INPUT_FILE;
            File readFile = new File(fileName);
            if (readFile.exists()) {
                try {
                    return new Scanner(readFile);
                } catch (FileNotFoundException e) {
                    System.out.println("Cannot open file: " + fileName);
                    return null;
                }
            }
            return new Scanner(System.in);
        }
    }

    /**
     * A system user. An instance is created only if both the username
     * and the password are valid; otherwise an exception is thrown.
     */
    class User {

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
        private static final String SYMBOLS = "!@#$%^&*()_+-=";
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

