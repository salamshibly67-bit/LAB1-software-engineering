import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button testButton;

    @FXML
    private Label errorLabel;

    private AuthenticationService authService;
    private AttemptTracker tracker;
    private BlockingQueue<FailedAttempt> failureQueue;

    private Thread countdownThread;

    private final Set<Thread> loginThreads =
            ConcurrentHashMap.newKeySet();

    private volatile boolean closed = false;

    // Receive shared services from GCMApp.
    public void setDependencies(
            ArrayList<User> users,
            AttemptTracker tracker,
            BlockingQueue<FailedAttempt> failureQueue) {

        this.authService = new AuthenticationService(users);
        this.tracker = tracker;
        this.failureQueue = failureQueue;
    }

    @FXML
    private void handleLogin() {

        if (closed) {
            return;
        }

        String email = usernameField.getText();
        String password = passwordField.getText();

        if (authService == null || tracker == null
                || failureQueue == null) {
            errorLabel.setText("User data is unavailable");
            return;
        }

        // Disable inputs during authentication.
        setInputsDisabled(true);
        errorLabel.setText("Checking credentials...");

        LoginTask task = new LoginTask(
                email,
                password,
                authService,
                tracker,
                failureQueue,
                result -> handleLoginResult(email, result)
        );

        Thread thread = new Thread(() -> {
            try {
                task.run();
            } finally {
                loginThreads.remove(Thread.currentThread());
            }
        }, "LoginTask");

        thread.setDaemon(true);
        loginThreads.add(thread);
        thread.start();
    }

    private void handleLoginResult(
            String email, LoginResult result) {

        if (closed) {
            return;
        }

        switch (result.getStatus()) {

            case SUCCESS:
                errorLabel.setText("");
                openWelcomeScreen();
                break;

            case ERROR:
                if (result.getRemainingAttempts() < 0) {
                    errorLabel.setText(
                            "Invalid username or password");
                } else {
                    errorLabel.setText(
                            "Invalid username or password. "
                                    + result.getRemainingAttempts()
                                    + " attempts remaining.");
                }

                setInputsDisabled(false);
                break;

            case BLOCKED:
                startCountdown(email);
                break;
        }
    }

    private void startCountdown(String email) {

        stopCountdown();

        setInputsDisabled(true);

        CountdownTask task = new CountdownTask(
                tracker,
                email,

                // Called every second on the JavaFX thread.
                remaining -> {
                    if (!closed) {
                        errorLabel.setText(
                                "Account blocked. "
                                        + remaining
                                        + " seconds remaining.");
                    }
                },

                // Called when the blocking time expires.
                () -> {
                    if (!closed) {
                        errorLabel.setText("");
                        setInputsDisabled(false);
                    }
                }
        );

        countdownThread = new Thread(task, "CountdownTask");
        countdownThread.setDaemon(true);
        countdownThread.start();
    }

    private void stopCountdown() {
        if (countdownThread != null) {
            countdownThread.interrupt();
            countdownThread = null;
        }
    }

    private void setInputsDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
        testButton.setDisable(disabled);
    }

    // Open the existing Lab 2 Welcome screen.
    private void openWelcomeScreen() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Welcome.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage)
                    usernameField.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Welcome to GCM");

        } catch (IOException e) {
            errorLabel.setText("Cannot open welcome screen");
            setInputsDisabled(false);
            e.printStackTrace();
        }
    }


    @FXML
    private void handleConcurrencyTest() {

        if (closed || testButton.isDisabled()) {
            return;
        }

        // Keep the interface responsive during the test.
        setInputsDisabled(true);
        testButton.setDisable(true);
        errorLabel.setText("Running concurrency test...");

        Thread testThread = new Thread(() -> {

            String output;

            try {
                output = ConcurrencyTest.runTest();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;

            } catch (Exception e) {
                output = "Test failed: " + e.getMessage();
            }

            String finalOutput = output;

            Platform.runLater(() -> {

                if (closed) {
                    return;
                }

                setInputsDisabled(false);
                testButton.setDisable(false);
                errorLabel.setText("");

                Alert alert = new Alert(
                        Alert.AlertType.INFORMATION);

                alert.setTitle("Concurrency Test");
                alert.setHeaderText("20-Thread Test Results");
                alert.setContentText(finalOutput);
                alert.setResizable(true);
                alert.getDialogPane().setPrefWidth(550);
                alert.show();
            });

        }, "ConcurrencyTestRunner");

        testThread.setDaemon(true);
        loginThreads.add(testThread);
        testThread.start();
    }


    // Stop background tasks when the window closes.
    public void shutdown() {

        closed = true;

        stopCountdown();

        for (Thread thread : loginThreads) {
            thread.interrupt();
        }
    }
}
