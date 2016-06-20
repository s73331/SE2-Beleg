package monitoringtool;

/**
 * An instance of this interface must be set as callback in PSQLHelper's constructor.
 * @author martin
 *
 */
public interface PSQLListener {
    /**
     * Called, when PSQLHelper catches a SQLException.
     */
    public void psqlErrorOccured();
    /**
     * Called, when the connection has been reestablished.
     */
    public void psqlErrorFixed();
}
