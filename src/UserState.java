public class UserState {

    private int failedAttempts = 0;
    private long blockedUntil = 0;

    // Check whether this account is currently blocked.
    public synchronized boolean isBlocked() {
        return System.currentTimeMillis() < blockedUntil;
    }

    // Get the remaining blocking time in seconds.
    public synchronized long getRemainingSeconds() {
        long remaining = blockedUntil - System.currentTimeMillis();

        if (remaining <= 0) {
            return 0;
        }

        return (remaining + 999) / 1000;
    }

    // Return the current number of failed attempts.
    public synchronized int getFailedAttempts() {
        return failedAttempts;
    }

    // Register a failed login attempt.
    public synchronized int recordFailure(int maxAttempts,
                                          int blockDurationSeconds) {

        // Do not count attempts while the account is blocked.
        if (isBlocked()) {
            return failedAttempts;
        }

        // Reset after an expired block.
        if (blockedUntil != 0) {
            failedAttempts = 0;
            blockedUntil = 0;
        }

        failedAttempts++;

        // Start blocking when the limit is reached.
        if (failedAttempts >= maxAttempts) {
            blockedUntil = System.currentTimeMillis()
                    + blockDurationSeconds * 1000L;
        }

        return failedAttempts;
    }

    // Reset failed attempts after successful authentication.
    public synchronized void reset() {
        failedAttempts = 0;
        blockedUntil = 0;
    }


    public synchronized LoginResult processFailure(
            int maxAttempts, int blockDurationSeconds) {

        long now = System.currentTimeMillis();

        // Reject attempts during an active block.
        if (now < blockedUntil) {
            long seconds = (blockedUntil - now + 999) / 1000;
            return LoginResult.blocked(seconds);
        }

        // Reset the counter after an expired block.
        if (blockedUntil != 0) {
            failedAttempts = 0;
            blockedUntil = 0;
        }

        failedAttempts++;

        // The nth failure starts a new block.
        if (failedAttempts >= maxAttempts) {
            blockedUntil = now + blockDurationSeconds * 1000L;
            return LoginResult.blocked(
                    (blockedUntil - now + 999) / 1000);
        }

        return LoginResult.error(maxAttempts - failedAttempts);
    }



}
