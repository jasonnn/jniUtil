package javah;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.*;
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
            public void report(@NotNull Diagnostic<? extends JavaFileObject> diagnostic) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    pw.print(JNILogger.getMessage("err.prefix"));
                    pw.print(" ");
                }
                pw.println(diagnostic.getMessage(null));
            }
        };
    }
}
