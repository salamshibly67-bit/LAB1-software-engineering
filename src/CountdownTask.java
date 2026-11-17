import javafx.application.Platform;

import java.util.function.Consumer;

public class CountdownTask implements Runnable {

    private final AttemptTracker tracker;
    private final String email;
    private final Consumer<Long> onUpdate;
    private final Runnable onFinished;

    public CountdownTask(
            AttemptTracker tracker,
            String email,
            Consumer<Long> onUpdate,
            Runnable onFinished) {

        this.tracker = tracker;
        this.email = email;
        this.onUpdate = onUpdate;
        this.onFinished = onFinished;
    }

    @Override
    public void run() {

        try {
            while (!Thread.currentThread().isInterrupted()) {

                long remaining =
                        tracker.getRemainingSeconds(email);

                if (remaining <= 0) {
                    Platform.runLater(onFinished);
                    return;
                }

                // Update the JavaFX interface safely.
                Platform.runLater(
                        () -> onUpdate.accept(remaining)
                );

                // Sleep on the background thread,
                // never on the JavaFX thread.
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
