package javah.ex;

import javah.JNILogger;

/**
 * Exit is used to replace the use of System.exit in the original javah.javah.
 */
@SuppressWarnings({"CheckedExceptionClass", "UncheckedExceptionClass"})
public class Exit extends Error {
    private static final long serialVersionUID = 430820978114067221L;

    public Exit(int exitValue) {
        this(exitValue, null);
    }

    public Exit(int exitValue, Throwable cause) {
        super(cause);
        this.exitValue = exitValue;
        this.cause = cause;
    }

    Exit(Exit e) {
        this(e.exitValue, e.cause);
    }

    public final int exitValue;
    public final Throwable cause;
}
