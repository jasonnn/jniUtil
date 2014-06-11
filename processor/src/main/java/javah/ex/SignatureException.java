package javah.ex;

/**
* Created by jason on 6/10/14.
*/
public class SignatureException extends Exception {
    private static final long serialVersionUID = 1L;
    public SignatureException(String reason) {
        super(reason);
    }
}
