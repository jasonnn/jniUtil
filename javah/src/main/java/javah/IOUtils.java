package javah;

import org.jetbrains.annotations.PropertyKey;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by jason on 6/9/14.
 */
public class IOUtils {
    private IOUtils() {
    }

    public static PrintWriter getPrintWriterForStream(OutputStream s) {
        return new PrintWriter(s, true);
    }

    public static PrintWriter getPrintWriterForWriter(Writer w) {
        if (w == null)
            return getPrintWriterForStream(null);
        else if (w instanceof PrintWriter)
            return (PrintWriter) w;
        else
            return new PrintWriter(w, true);
    }

    public static DiagnosticListener<JavaFileObject> getDiagnosticListenerForStream(OutputStream s) {
        return getDiagnosticListenerForWriter(getPrintWriterForStream(s));
    }

    public static DiagnosticListener<JavaFileObject> getDiagnosticListenerForWriter(Writer w) {
        final PrintWriter pw = getPrintWriterForWriter(w);
        return new DiagnosticListener<JavaFileObject>() {
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    pw.print(getMessage("err.prefix"));
                    pw.print(" ");
                }
                pw.println(diagnostic.getMessage(null));
            }
        };
    }


    static final Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();

    public static String getMessage(@PropertyKey(resourceBundle = "javah.l10n") String key, Object... args) {
        return getMessage(Locale.getDefault(), key, args);
    }

    public static String getMessage(Locale locale, @PropertyKey(resourceBundle = "javah.l10n") String key, Object... args) {

        ResourceBundle b = bundles.get(locale);
        if (b == null) {
            try {

                b = ResourceBundle.getBundle("javah.l10n", locale);
                bundles.put(locale, b);
            } catch (MissingResourceException e) {
                throw new InternalError("Cannot find javah resource bundle for locale " + locale, e);
            }
        }

        try {
            return MessageFormat.format(b.getString(key), args);
        } catch (MissingResourceException e) {
            return key;
            //throw new InternalError(e, key);
        }
    }
}
