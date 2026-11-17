import java.util.concurrent.CountDownLatch;

public class ConcurrencyTest {

    private static final int THREAD_COUNT = 20;
    private static final int FAILURES_PER_THREAD = 499;
    private static final int MAX_ATTEMPTS = 10000;
    private static final int BLOCK_SECONDS = 30;

    public static String runTest()
            throws InterruptedException {

        // Use a separate tracker for testing.
        AttemptTracker tracker =
                new AttemptTracker(MAX_ATTEMPTS, BLOCK_SECONDS);

        String email = "concurrency@test.com";

        Thread[] threads = new Thread[THREAD_COUNT];

        // Make all worker threads start together.
        CountDownLatch startSignal = new CountDownLatch(1);

        for (int i = 0; i < THREAD_COUNT; i++) {

            threads[i] = new Thread(() -> {
                try {
                    startSignal.await();

                    for (int j = 0;
                         j < FAILURES_PER_THREAD; j++) {

                        tracker.processFailure(email);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "TestThread-" + (i + 1));

            threads[i].start();
        }

        // Release all 20 threads at once.
        startSignal.countDown();

        // Wait until all threads finish.
        for (Thread thread : threads) {
            thread.join();
        }

        int expected = THREAD_COUNT * FAILURES_PER_THREAD;
        int actual = tracker.getState(email).getFailedAttempts();

        boolean blockedAfter9980 = tracker.isBlocked(email);

        // Add 19 more failures.
        for (int i = 0; i < 19; i++) {
            tracker.processFailure(email);
        }

        int after19 = tracker.getState(email).getFailedAttempts();
        boolean blockedAfter19 = tracker.isBlocked(email);

        // The 20th additional failure must trigger blocking.
        LoginResult finalResult = tracker.processFailure(email);

        boolean blockedAfter20 = tracker.isBlocked(email);

        boolean passed =
                actual == expected
                        && !blockedAfter9980
                        && after19 == 9999
                        && !blockedAfter19
                        && blockedAfter20
                        && finalResult.getStatus()
                        == LoginResult.Status.BLOCKED;

        String output =
                "=== CONCURRENCY TEST ===\n"
                        + "Threads: " + THREAD_COUNT + "\n"
                        + "Failures per thread: " + FAILURES_PER_THREAD + "\n"
                        + "Expected failures: " + expected + "\n"
                        + "Actual failures: " + actual + "\n"
                        + "Blocked after 9980: "
                        + blockedAfter9980 + "\n"
                        + "Count after 19 additional failures: "
                        + after19 + "\n"
                        + "Blocked after 19: "
                        + blockedAfter19 + "\n"
                        + "Blocked after 20: "
                        + blockedAfter20 + "\n"
                        + "RESULT: " + (passed ? "PASS" : "FAIL");

        System.out.println(output);

        return output;
    }
}
