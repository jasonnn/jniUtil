package javah.ex;


/**
 * Exit is used to replace the use of System.exit in the original javah.javah.
 * error -- User did something wrong
 * bug   -- Bug has occurred in javah.javah
 * fatal -- We can't even find resources, so bail fast, don't localize
 */
@SuppressWarnings({"CheckedExceptionClass", "UncheckedExceptionClass"})
public class Exit extends Error {
    public static enum STATUS {
        ERROR(15), BUG(11), FATAL(10);

        final int exitValue;

        STATUS(int i) {
            this.exitValue = i;
        }

        static STATUS fromInt(int val) {
            for (STATUS status : values()) {
                if (status.exitValue == val) return status;
            }
            throw new IllegalArgumentException("???");
        }
    }

    private static final long serialVersionUID = 430820978114067221L;

    public Exit(STATUS status, Throwable cause) {
        this.exitValue = status;
        this.cause = cause;
    }

    public Exit(STATUS status) {
        this(status, null);
    }

//    public Exit(int exitValue) {
//        this(exitValue, null);
//    }
//
//    public Exit(int exitValue, Throwable cause) {
//        super(cause);
//        this.exitValue = STATUS.fromInt(exitValue);
//        this.cause = cause;
//    }

    Exit(Exit e) {
        this(e.exitValue, e.cause);
    }

    public final STATUS exitValue;
    public final Throwable cause;
}
