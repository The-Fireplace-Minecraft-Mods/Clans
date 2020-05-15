package the_fireplace.clans.model;

public enum TerritoryDisplayMode {
    CHAT(false, true),
    ACTION_BAR(true, false),
    CHAT_NODESC(false, false),
    OFF(false, false);

    boolean showDescription, isAction;
    public boolean showsDescription() {
        return showDescription;
    }
    public boolean isAction() {
        return isAction;
    }

    TerritoryDisplayMode(boolean isAction, boolean showDescription) {
        this.showDescription = showDescription;
    }
}
