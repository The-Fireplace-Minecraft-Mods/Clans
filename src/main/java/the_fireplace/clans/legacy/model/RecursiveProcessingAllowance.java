package the_fireplace.clans.legacy.model;

/**
 * Lazy solution that allows working around the dynmap stackoverflow crash. This system was not designed to handle claims with millions of connected chunks and a proper fix would take longer than it's worth on 1.12.2.
 */
public final class RecursiveProcessingAllowance
{
    private int remainingLimit;
    private final boolean unlimited;

    /**
     * @param limit set to 0 for no limit
     */
    public RecursiveProcessingAllowance(int limit) {
        remainingLimit = limit;
        unlimited = limit <= 0;
    }

    public boolean canContinueProcessing() {
        return unlimited || remainingLimit > 0;
    }

    public void decrementProcessAmount() {
        if (!unlimited) {
            this.remainingLimit--;
        }
    }
}
