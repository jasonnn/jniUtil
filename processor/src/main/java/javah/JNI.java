/*
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
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
import javah.ex.SignatureException;
import jniHelper.processor.JNIProcessorConfig;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Header file generator for JNI.
 * <p/>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.</b></p>
 *
 * @author Sucheta Dambalkar(Revised)
 */
public class JNI extends Gen {

    public static JNI.Builder builder() {
        return new JNI.Builder();
    }

    public static class Builder {
        private JNILogger logger = null;
        private JNIProcessorConfig config = null;

        public JNI.Builder withLogger(JNILogger logger) {
            this.logger = logger;
            return this;
        }

        public JNI.Builder configuredWith(JNIProcessorConfig config) {
            this.config = config;
            return this;
        }

        public JNI build() throws IOException {
            FileObject outFile = config.outFile != null ?
                    config.env.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", config.outFile) : null;
            return new JNI(logger, config.env, config.force, config.outDir, outFile);
        }

    }


    JNI(JNILogger log, ProcessingEnvironment env, boolean force, @Nullable String relOutPath, @Nullable FileObject outFile) {
        super(log, env, force, relOutPath, outFile);
    }

    @Override
    public String getIncludes() {
        return "#include <jni.h>";
    }

    @Override
    public void write(OutputStream o, TypeElement clazz) throws Exit {
        try {
            String cname = Mangle.mangle(clazz.getQualifiedName(), Mangle.Type.CLASS);
            PrintWriter pw = wrapWriter(o);
            pw.println(guardBegin(cname));
            pw.println(cppGuardBegin());

            /* Write statics. */
            List<VariableElement> classfields = getAllFields(clazz);

            for (VariableElement v : classfields) {
                if (!v.getModifiers().contains(Modifier.STATIC))
                    continue;
                String s = defineForStatic(clazz, v);
                if (s != null) {
                    pw.println(s);
                }
            }

            /* Write methods. */
            List<ExecutableElement> classmethods = ElementFilter.methodsIn(clazz.getEnclosedElements());
            for (ExecutableElement md : classmethods) {
                if (md.getModifiers().contains(Modifier.NATIVE)) {
                    TypeMirror mtr = types.erasure(md.getReturnType());
                    String sig = signature(md);
                    TypeSignature newtypesig = new TypeSignature(elems);
                    CharSequence methodName = md.getSimpleName();
                    boolean longName = false;
                    for (ExecutableElement md2 : classmethods) {
                        //TODO ??? ( was instance equality)
                        if ((!md2.equals(md))
                                && (methodName.equals(md2.getSimpleName()))
                                && (md2.getModifiers().contains(Modifier.NATIVE)))
                            longName = true;

                    }
                    pw.println("/*");
                    pw.println(" * Class:     " + cname);
                    pw.println(" * Method:    " +
                            Mangle.mangle(methodName, Mangle.Type.FIELDSTUB));
                    pw.println(" * Signature: " + newtypesig.getTypeSignature(sig, mtr));
                    pw.println(" */");
                    pw.println("JNIEXPORT " + jniType(mtr) +
                            " JNICALL " +
                            mangler.mangleMethod(md, clazz,
                                    (longName) ?
                                            Mangle.Type.METHOD_JNI_LONG :
                                            Mangle.Type.METHOD_JNI_SHORT));
                    pw.print("  (JNIEnv *, ");
                    List<? extends VariableElement> paramargs = md.getParameters();
                    List<TypeMirror> args = new ArrayList<TypeMirror>(paramargs.size());
                    for (VariableElement p : paramargs) {
                        args.add(types.erasure(p.asType()));
                    }
                    if (md.getModifiers().contains(Modifier.STATIC))
                        pw.print("jclass");
                    else
                        pw.print("jobject");

                    for (TypeMirror arg : args) {
                        pw.print(", ");
                        pw.print(jniType(arg));
                    }
                    pw.println(");" + lineSep);
                }
            }
            pw.println(cppGuardEnd());
            pw.println(guardEnd(cname));
        } catch (SignatureException e) {
            log.error("jni.sigerror", e.getMessage());
        }
    }


    protected final String jniType(TypeMirror t) throws Exit {
        TypeElement throwable = elems.getTypeElement("java.lang.Throwable");
        TypeElement jClass = elems.getTypeElement("java.lang.Class");
        TypeElement jString = elems.getTypeElement("java.lang.String");
        Element tclassDoc = types.asElement(t);


        switch (t.getKind()) {
            case ARRAY: {
                TypeMirror ct = ((ArrayType) t).getComponentType();
                switch (ct.getKind()) {
                    case BOOLEAN:
                        return "jbooleanArray";
                    case BYTE:
                        return "jbyteArray";
                    case CHAR:
                        return "jcharArray";
                    case SHORT:
                        return "jshortArray";
                    case INT:
                        return "jintArray";
                    case LONG:
                        return "jlongArray";
                    case FLOAT:
                        return "jfloatArray";
                    case DOUBLE:
                        return "jdoubleArray";
                    case ARRAY:
                    case DECLARED:
                        return "jobjectArray";
                    default:
                        throw new Error(ct.toString());
                }
            }

            case VOID:
                return "void";
            case BOOLEAN:
                return "jboolean";
            case BYTE:
                return "jbyte";
            case CHAR:
                return "jchar";
            case SHORT:
                return "jshort";
            case INT:
                return "jint";
            case LONG:
                return "jlong";
            case FLOAT:
                return "jfloat";
            case DOUBLE:
                return "jdouble";

            case DECLARED: {
                if (tclassDoc.equals(jString))
                    return "jstring";
                else if (types.isAssignable(t, throwable.asType()))
                    return "jthrowable";
                else if (types.isAssignable(t, jClass.asType()))
                    return "jclass";
                else
                    return "jobject";
            }
        }

        log.bug("jni.unknown.type");

        return null; /* dead code. */
    }
}
