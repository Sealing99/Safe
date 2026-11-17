package net.sealing99.safe.exception;

/**
 * An exception that gets thrown whenever an {@link java.sql.SQLException} is caught or any database-related problems.
 * @see java.sql.SQLException
 * @see EconomyException
 * @since v0.2.0
 * @author Sealing99
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
