package minetweaker;

public interface IUndoableAction {
    void apply();

    void undo();

    String describe();

    String describeUndo();

    boolean canUndo();

    Object getOverrideKey();
}
