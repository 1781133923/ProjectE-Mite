package cpw.mods.fml.common.eventhandler;

public class Event {
    private boolean isCanceled;
    private Result result = Result.DEFAULT;

    public boolean isCancelable() {
        return false;
    }

    public boolean isCanceled() {
        return this.isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        if (!this.isCancelable()) {
            throw new UnsupportedOperationException("Attempted to cancel a non-cancelable event: " + getClass());
        }
        this.isCanceled = isCanceled;
    }

    public boolean hasResult() {
        return false;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result value) {
        if (!this.hasResult()) {
            throw new UnsupportedOperationException("Attempted to set result on event with no result: " + getClass());
        }
        this.result = value;
    }

    public enum Result {
        DENY,
        DEFAULT,
        ALLOW
    }

    public static class HasResult extends Event {
        @Override
        public boolean hasResult() {
            return true;
        }
    }
}
