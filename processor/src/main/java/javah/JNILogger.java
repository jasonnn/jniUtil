/*
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package javah;

import javah.ex.Exit;
import jniHelper.processor.JNIProcessorConfig;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Messages, verbose and error handling support.
 * <p/>
 * For errors, the failure modes are:
 * error -- User did something wrong
 * bug   -- Bug has occurred in javah.javah
 * fatal -- We can't even find resources, so bail fast, don't localize
 * <p/>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.</b></p>
 */
//TODO use messager
public class JNILogger {

    public static JNILogger getDefault(JNIProcessorConfig config) {
        // DiagnosticListener<JavaFileObject> diagnosticListener = IOUtils.getDiagnosticListenerForStream(System.err);

        return new JNILogger(config.getMessager());
    }

    public static JNILogger getDefault() {
        return getDefault(JNIProcessorConfig.DEFAULT);
    }

    public boolean isVerbose() {
        return verbose;
    }

    private final boolean verbose;


    private final Messager messager;

    public JNILogger(Messager messager) {
        this(true, messager);
    }

    public JNILogger(boolean verbose, Messager messager) {
        this.verbose = verbose;
        this.messager = messager;
    }

    public void log(String s) {
        messager.printMessage(Kind.NOTE, s);
    }


    /*
     * Failure modes.
     */
    public void bug(@PropertyKey(resourceBundle = "javah.l10n") String key) throws Exit {
        bug(key, null);
    }

    public void bug(@PropertyKey(resourceBundle = "javah.l10n") String key, Exception e) throws Exit {
        messager.printMessage(Kind.ERROR, getMessage(key));
        messager.printMessage(Kind.OTHER, getMessage("bug.report"));
        throw new Exit(Exit.STATUS.BUG, e);
    }

    public void error(@PropertyKey(resourceBundle = "javah.l10n") String key, Object... args) throws Exit {
        messager.printMessage(Kind.ERROR, getMessage(key, args));
        throw new Exit(Exit.STATUS.ERROR);
    }

//    private void fatal(String msg, Exception e) throws Exit {
//        dl.report(createDiagnostic(Kind.ERROR, "", msg));
//        throw new Exit(Exit.STATUS.FATAL, e);
//    }

//    private static Diagnostic<JavaFileObject> createDiagnostic(
//            final Kind kind, final String code, final Object... args) {
//        return new Diagnostic<JavaFileObject>() {
//            public String getCode() {
//                return code;
//            }
//
//            public long getColumnNumber() {
//                return Diagnostic.NOPOS;
//            }
//
//            public long getEndPosition() {
//                return Diagnostic.NOPOS;
//            }
//
//            public Kind getKind() {
//                return kind;
//            }
//
//            public long getLineNumber() {
//                return Diagnostic.NOPOS;
//            }
//
//            public String getMessage(Locale locale) {
//                if (code.length() == 0)
//                    return (String) args[0];
//                return JNILogger.getMessage(code, args); // FIXME locale
//            }
//
//            public long getPosition() {
//                return Diagnostic.NOPOS;
//            }
//
//            public JavaFileObject getSource() {
//                return null;
//            }
//
//            public long getStartPosition() {
//                return Diagnostic.NOPOS;
//            }
//        };
//    }

    public static String getMessage(@PropertyKey(resourceBundle = "javah.l10n") String key, Object... args) {
        return MessageFormat.format(resourceBundle.getString(key), args);
    }

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("javah.l10n");
}
