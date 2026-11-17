import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FailedAttempt {

    private final String email;

    private final BlockingQueue<LoginResult> responseQueue =
            new ArrayBlockingQueue<>(1);

    public FailedAttempt(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void sendResponse(LoginResult result)
            throws InterruptedException {
        responseQueue.put(result);
    }

    public LoginResult waitForResponse()
            throws InterruptedException {
        return responseQueue.take();
    }
}
