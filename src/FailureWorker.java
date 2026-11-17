import java.util.concurrent.BlockingQueue;

public class FailureWorker extends Thread {

    private final BlockingQueue<FailedAttempt> queue;
    private final AttemptTracker tracker;

    public FailureWorker(
            BlockingQueue<FailedAttempt> queue,
            AttemptTracker tracker,
            String name) {

        super(name);
        this.queue = queue;
        this.tracker = tracker;

        // Daemon threads do not keep the JVM alive.
        setDaemon(true);
    }


    @Override
    public void run() {

        while (!isInterrupted()) {
            try {
                FailedAttempt attempt = queue.take();

                LoginResult result =
                        tracker.processFailure(attempt.getEmail());

                attempt.sendResponse(result);

            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }

}
