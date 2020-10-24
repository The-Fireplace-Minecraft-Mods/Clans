package the_fireplace.clans.multithreading;

public class ThreadedSaveHandler<T extends ThreadedSaveable> {
    private T saveObject;
    private boolean isChanged = false;
    private boolean saving = false;
    private boolean disposed = false;

    private ThreadedSaveHandler(T saveObject) {
        this.saveObject = saveObject;
    }

    public static <K extends ThreadedSaveable> ThreadedSaveHandler<K> create(K saveObject) {
        return new ThreadedSaveHandler<>(saveObject);
    }

    public void disposeReference() {
        disposed = true;
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
        SaveExecutionManager.run(() -> {
            saveObject.blockingSave();
            saving = false;
            if(disposed) {
                if(isChanged)
                    concurrentSave();
                else //noinspection ConstantConditions
                    saveObject = null;
            }
        });
    }
}
