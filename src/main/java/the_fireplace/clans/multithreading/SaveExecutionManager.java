package the_fireplace.clans.multithreading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SaveExecutionManager {
    //TODO Limit the number of running threads so we don't run the machine out of memory
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static void run(Runnable runnable) {
        if(!EXECUTOR_SERVICE.isShutdown())
            EXECUTOR_SERVICE.execute(runnable);
    }

    public static void waitForCompletion() throws InterruptedException {
        EXECUTOR_SERVICE.shutdown();
        EXECUTOR_SERVICE.awaitTermination(1, TimeUnit.DAYS);
    }
}
