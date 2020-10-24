package the_fireplace.clans.multithreading;

public class ThreadedSaveHandler<T extends ThreadedSaveable> {
    private T saveObject;
    private boolean isChanged = false;
    private boolean saving = false;
    private boolean markedForDisposal = false;

    private ThreadedSaveHandler(T saveObject) {
        this.saveObject = saveObject;
    }

    public static <K extends ThreadedSaveable> ThreadedSaveHandler<K> create(K saveObject) {
        return new ThreadedSaveHandler<>(saveObject);
    }

    public void disposeReferences() {
        markedForDisposal = true;
    }

    public void markNeedsSave() {
        isChanged = true;
    }

    /**
     * Make a save on a new thread to avoid blocking existing threads. Only save if changed.
     */
    public void concurrentSave() {
        if(!isChanged || saving)
            return;
        saving = true;
        isChanged = false;
        ConcurrentExecutionManager.run(() -> {
            saveObject.blockingSave();
            saving = false;
            if(markedForDisposal) {
                if(isChanged)
                    concurrentSave();
                else //noinspection ConstantConditions
                    saveObject = null;
            }
        });
    }
}
