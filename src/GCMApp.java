
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class GCMApp extends Application {

    private int maxAttempts;
    private int blockDuration;

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Read the arguments before opening the Login screen.
        // n = maximum failed attempts
        // t = blocking duration in seconds
        maxAttempts = readPositiveArgument(0, "n",
                "Maximum number of failed attempts");

        blockDuration = readPositiveArgument(1, "t",
                "Blocking duration in seconds");

        // Read and validate users using Lab 1 logic.
        ArrayList<User> users = UsersApp.loadUsers("Users.txt");

        // Load the login interface.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Login.fxml")
        );

        Parent root = loader.load();

        // Give the controller the valid users.
        LoginController controller = loader.getController();
        controller.setUsers(users);

        // Create and display the login window.
        Scene scene = new Scene(root);

        primaryStage.setTitle("GCM Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private int readPositiveArgument(int index, String name,
                                     String description) {

        String[] args = getParameters().getRaw().toArray(new String[0]);
        // First try the program arguments.
        if (index < args.length) {
            try {
                int value = Integer.parseInt(args[index]);

                if (value >= 1) {
                    return value;
                }

            } catch (NumberFormatException e) {
                // Invalid argument: ask the user manually.
            }
        }

        // Ask the user until a valid positive integer is entered.
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Invalid or Missing Argument");
            dialog.setHeaderText("Please enter a valid value for " + name);
            dialog.setContentText(description + " (integer >= 1):");

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                throw new IllegalStateException(
                        "Required argument " + name + " was not provided."
                );
            }

            try {
                int value = Integer.parseInt(result.get().trim());

                if (value >= 1) {
                    return value;
                }

            } catch (NumberFormatException e) {
                // Invalid input: show the dialog again.
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
