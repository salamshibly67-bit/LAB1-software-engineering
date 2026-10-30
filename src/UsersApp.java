import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Lab 1 - Software Engineering, Winter 2026.
 * Reads users from users.txt, validates each line, keeps the valid users
 * in an ArrayList, prints the invalid lines with an error message,
 * and finally prints the valid users sorted by username.
 */
public class UsersApp {

    private static final String INPUT_FILE = "users.txt";
    private static final String OUTPUT_FILE = "out.txt";

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

        // 3.3 write the valid users, sorted by username, to the output file
        Collections.sort(users, (u1, u2) -> u1.getName().compareTo(u2.getName()));
        try (PrintWriter writer = new PrintWriter(OUTPUT_FILE)) {
            for (User user : users) {
                writer.println(user);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot write file: " + OUTPUT_FILE);
        }
    }




    /**
     * Reads the input file and returns only valid users.
     * Invalid entries are skipped and reported.
     */
    public static ArrayList<User> loadUsers(String fileName) {

        ArrayList<User> users = new ArrayList<>();

        try (Scanner reader = new Scanner(new File(fileName))) {

            while (reader.hasNextLine()) {

                String line = reader.nextLine();

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.trim().split("\\s+");

                String username = parts[0];
                String password = (parts.length == 2)
                        ? parts[1] : null;

                try {
                    if (password == null) {
                        throw new IllegalArgumentException(
                                User.INVALID_PASSWORD
                        );
                    }

                    users.add(new User(username, password));

                } catch (IllegalArgumentException e) {
                    printError(line, e.getMessage());
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file: " + fileName);
        }

        return users;
    }



    /**
     * Prints an invalid line and its error message on one line to the error
     * stream, so that only the valid users are written to the output file.
     */
    private static void printError(String line, String message) {
        System.err.println(line + " " + message);
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