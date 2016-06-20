package monitoringtool;

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
