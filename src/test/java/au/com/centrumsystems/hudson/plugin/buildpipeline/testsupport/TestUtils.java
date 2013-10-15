package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class TestUtils {

    public static void waitFor(Condition condition) {
        waitFor(condition, 10, SECONDS);
    }

    public static void waitFor(Condition condition, int timeout, TimeUnit timeUnit) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeUnit.toMillis(timeout);
        while (!isSatisfied(condition)) {
            try {
                if ((System.currentTimeMillis() - startTime) > timeoutMillis) {
                    String message = String.format("Timed out waiting for %s after %dms", condition.describe(), timeoutMillis);
                    throw new AssertionError(message);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    public static boolean isSatisfied(Condition condition) {
        try {
            return condition.isSatisfied();
        } catch (Exception e) {
            return false;
        }
    }

    public static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
