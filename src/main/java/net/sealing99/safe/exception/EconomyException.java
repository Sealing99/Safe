package net.sealing99.safe.exception;

/**
 * An exception that gets thrown whenever an illegal state is present in any economic context.
 * @see DatabaseException
 * @since v0.2.0
 * @author Sealing99
 */
public class EconomyException extends RuntimeException {
    public EconomyException(String message) {
        super(message);
    }
}
