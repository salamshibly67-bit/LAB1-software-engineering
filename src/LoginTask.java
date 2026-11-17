import javafx.application.Platform;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class LoginTask implements Runnable {

    private final String email;
    private final String password;
    private final AuthenticationService authService;
    private final AttemptTracker tracker;
    private final BlockingQueue<FailedAttempt> failureQueue;
    private final Consumer<LoginResult> onResult;

    public LoginTask(
            String email,
            String password,
            AuthenticationService authService,
            AttemptTracker tracker,
            BlockingQueue<FailedAttempt> failureQueue,
            Consumer<LoginResult> onResult) {

        this.email = email;
        this.password = password;
        this.authService = authService;
        this.tracker = tracker;
        this.failureQueue = failureQueue;
        this.onResult = onResult;
    }

    @Override
    public void run() {

        LoginResult result;

        try {
            result = performLogin();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;

        } catch (RuntimeException e) {
            // Handle unexpected authentication errors.
            e.printStackTrace();
            result = LoginResult.error(-1);
        }

        // Only the JavaFX thread updates the interface.
        LoginResult finalResult = result;

        Platform.runLater(() -> onResult.accept(finalResult));
    }


    private LoginResult performLogin()
            throws InterruptedException {

        // Unknown emails are never tracked.
        if (!authService.emailExists(email)) {
            return LoginResult.error(-1);
        }

        // Serialize complete login operations for the same email.
        var loginLock = tracker.getLoginLock(email);

        loginLock.lockInterruptibly();

        try {
            // Check blocking status in a separate Thread.
            BlockCheckThread checker =
                    new BlockCheckThread(tracker, email);

            checker.start();

            try {
                checker.join();
            } catch (InterruptedException e) {
                checker.interrupt();
                throw e;
            }

            if (checker.isAccountBlocked()) {
                return LoginResult.blocked(
                        checker.getRemainingSeconds());
            }

            // Authenticate using the existing service.
            boolean valid;

            try {
                valid = authService.authenticate(email, password);
            } catch (IllegalArgumentException e) {
                valid = false;
            }

            if (valid) {
                tracker.reset(email);
                return LoginResult.success();
            }

            // Submit an incorrect-password event.
            FailedAttempt attempt = new FailedAttempt(email);

            failureQueue.put(attempt);

            // Wait for the worker response.
            return attempt.waitForResponse();

        } finally {
            loginLock.unlock();
        }
    }

}
