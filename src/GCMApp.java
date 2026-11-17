import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GCMApp extends Application {

    private int maxAttempts;
    private int blockDuration;

    private ArrayList<User> users;

    private FailureWorker worker1;
    private FailureWorker worker2;

    // init() is not executed on the JavaFX UI thread.
    @Override
    public void init() {
        users = UsersApp.loadUsers("Users.txt");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Read and validate program arguments.
        maxAttempts = readPositiveArgument(
                0, "n", "Maximum number of failed attempts");

        blockDuration = readPositiveArgument(
                1, "t", "Blocking duration in seconds");

        // One shared tracker for all login attempts.
        AttemptTracker tracker = new AttemptTracker(
                maxAttempts, blockDuration);

        // Shared queue for failed login events.
        BlockingQueue<FailedAttempt> failureQueue =
                new LinkedBlockingQueue<>();

        // Start two daemon failure workers.
        worker1 = new FailureWorker(
                failureQueue, tracker, "FailureWorker-1");

        worker2 = new FailureWorker(
                failureQueue, tracker, "FailureWorker-2");

        worker1.start();
        worker2.start();

        // Load existing Login.fxml.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Login.fxml")
        );

        Parent root = loader.load();

        // Connect the controller to the shared services.
        LoginController controller = loader.getController();

        controller.setDependencies(
                users, tracker, failureQueue);

        Scene scene = new Scene(root);

        primaryStage.setTitle("GCM Login");
        primaryStage.setScene(scene);

        // Stop the threads when the application closes.
        primaryStage.setOnCloseRequest(event -> {
            controller.shutdown();
            worker1.interrupt();
            worker2.interrupt();
        });

        primaryStage.show();
    }

    private int readPositiveArgument(
            int index, String name, String description) {

        String[] args = getParameters().getRaw()
                .toArray(new String[0]);

        if (index < args.length) {
            try {
                int value = Integer.parseInt(args[index]);

                if (value >= 1) {
                    return value;
                }

            } catch (NumberFormatException e) {
                // Request manual input below.
            }
        }

        while (true) {
            TextInputDialog dialog = new TextInputDialog();

            dialog.setTitle("Invalid or Missing Argument");
            dialog.setHeaderText(
                    "Please enter a valid value for " + name);

            dialog.setContentText(
                    description + " (integer >= 1):");

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                throw new IllegalStateException(
                        "Required argument " + name
                                + " was not provided.");
            }

            try {
                int value = Integer.parseInt(
                        result.get().trim());

                if (value >= 1) {
                    return value;
                }

            } catch (NumberFormatException e) {
                // Ask again.
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
