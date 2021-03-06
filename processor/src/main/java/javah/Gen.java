/*
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
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
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.*;

/**
 * An abstraction for generating support files required by native methods.
 * Subclasses are for specific native interfaces. At the time of its
 * original writing, this interface is rich enough to support JNI and the
 * old 1.0-style native method interface.
 * <p/>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.</b></p>
 *
 * @author Sucheta Dambalkar(Revised)
 */
@SuppressWarnings("StringConcatenationMissingWhitespace")
public abstract class Gen {
    protected static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    protected static final String lineSep = System.getProperty("line.separator");

    protected final Types types;
    protected final Elements elems;
    protected final Mangle mangler;
    protected final JNILogger log;
    protected final Filer filer;
    /*
     * Smartness with generated files.
     */
    protected final boolean force;
    /*
 * List of classes for which we must generate output.
 */
    protected Set<TypeElement> classes = null;

    @Nullable
    protected final FileObject outFile;
    @Nullable
    protected final String outDirName;


    protected Gen(JNILogger log, ProcessingEnvironment env, boolean force, @Nullable String relOutPath, @Nullable FileObject outFile) {
        this.log = log;
        this.types = env.getTypeUtils();
        this.elems = env.getElementUtils();
        this.filer = env.getFiler();
        this.mangler = new Mangle(elems, types);
        this.force = force;
        this.outDirName = relOutPath;
        this.outFile = outFile;


    }


    /**
     * Override this abstract method, generating content for the named
     * class into the outputstream.
     */
    protected abstract void write(OutputStream o, TypeElement clazz) throws Exit;

    /**
     * Override this method to provide a list of #include statements
     * required by the native interface.
     */
    protected abstract String getIncludes();


    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setClasses(Set<TypeElement> classes) {
        this.classes = classes;
    }


    /**
     * We explicitly need to write ASCII files because that is what C
     * compilers understand.
     */
    protected PrintWriter wrapWriter(OutputStream o) throws Exit {
        try {
            return new PrintWriter(new OutputStreamWriter(o, "ISO8859_1"), true);
        } catch (UnsupportedEncodingException ignored) {
            log.bug("encoding.iso8859_1.not.found");
            return null; /* dead code */
        }
    }

    /**
     * After initializing state of an instance, use this method to start
     * processing.
     * <p/>
     * Buffer size chosen as an approximation from a single sampling of:
     * expr `du -sk` / `ls *.h | wc -l`
     */
    public void run() throws IOException, ClassNotFoundException, Exit {

        if (outFile != null) {
            /* Everything goes to one big file... */
            ByteArrayOutputStream bout = new ByteArrayOutputStream(8192);
            writeFileTop(bout); /* only once */

            for (TypeElement t : classes) {
                write(bout, t);
            }

            writeIfChanged(bout.toByteArray(), outFile);
        } else {
            /* Each class goes to its own file... */
            for (TypeElement t : classes) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream(8192);
                writeFileTop(bout);
                write(bout, t);
                writeIfChanged(bout.toByteArray(), getFileObject(t.getQualifiedName()));
            }
        }
    }

    /*
     * Write the contents of byte[] b to a file named file.  Writing
     * is done if either the file doesn't exist or if the contents are
     * different.
     */

    private void writeIfChanged(byte[] b, FileObject file) throws IOException {
        boolean mustWrite = false;
        String event = "[No need to update file ";

        if (force) {
            mustWrite = true;
            event = "[Forcefully writing file ";
        } else {
            InputStream in = null;
            try {
                // regrettably, there's no API to get the length in bytes
                // for a FileObject, so we can't short-circuit reading the
                // file here
                in = file.openInputStream();
                byte[] a = readBytes(in);
                if (!Arrays.equals(a, b)) {
                    mustWrite = true;
                    event = "[Overwriting file ";

                }
            } catch (FileNotFoundException ignored) {
                mustWrite = true;
                event = "[Creating file ";
            } finally {
                if (in != null) in.close();
            }
        }

        if (log.isVerbose()) log.log(event + file + ']');

        if (mustWrite) {
            OutputStream out = file.openOutputStream();
            out.write(b); /* No buffering, just one big write! */
            out.close();
        }
    }

    @SuppressWarnings("NestedAssignment")
    protected static byte[] readBytes(InputStream in) throws IOException {
        try {
            byte[] array = new byte[in.available() + 1];
            int offset = 0;
            int n;
            while ((n = in.read(array, offset, array.length - offset)) != -1) {
                offset += n;
                if (offset == array.length)
                    array = Arrays.copyOf(array, array.length * 2);
            }

            return Arrays.copyOf(array, offset);
        } finally {
            in.close();
        }
    }

    protected String defineForStatic(TypeElement c, VariableElement f)
            throws Exit {
        CharSequence cnamedoc = c.getQualifiedName();
        CharSequence fnamedoc = f.getSimpleName();

        String cname = Mangle.mangle(cnamedoc, Mangle.Type.CLASS);
        String fname = Mangle.mangle(fnamedoc, Mangle.Type.FIELDSTUB);

        if (!f.getModifiers().contains(Modifier.STATIC))
            log.bug("tried.to.define.non.static");

        if (f.getModifiers().contains(Modifier.FINAL)) {

            Object value = f.getConstantValue();

            if (value != null) { /* so it is a ConstantExpression */
                String constString = null;
                if ((value instanceof Integer)
                        || (value instanceof Byte)
                        || (value instanceof Short)) {
                    /* covers byte, short, int */
                    constString = value.toString() + 'L';
                } else if (value instanceof Boolean) {
                    constString = ((Boolean) value) ? "1L" : "0L";
                } else if (value instanceof Character) {
                    Character ch = (Character) value;
                    constString = String.valueOf(((int) ch) & 0xffff) + 'L';
                } else if (value instanceof Long) {
                    // Visual C++ supports the i64 suffix, not LL.
                    if (isWindows)
                        constString = value.toString() + "i64";
                    else
                        constString = value.toString() + "LL";
                } else if (value instanceof Float) {
                    /* bug for bug */
                    float fv = (Float) value;
                    if (Float.isInfinite(fv))
                        constString = ((fv < 0) ? "-" : "") + "Inff";
                    else
                        constString = value.toString() + 'f';
                } else if (value instanceof Double) {
                    /* bug for bug */
                    double d = (Double) value;
                    if (Double.isInfinite(d))
                        constString = ((d < 0) ? "-" : "") + "InfD";
                    else
                        constString = value.toString();
                }
                if (constString != null) {
                    return "#undef " + cname + '_' + fname + lineSep + "#define " + cname + '_' + fname + ' ' + constString;
                }

            }
        }
        return null;
    }

    /*
     * Deal with the C pre-processor.
     */
    protected String cppGuardBegin() {
        return "#ifdef __cplusplus" + lineSep + "extern \"C\" {" + lineSep + "#endif";
    }

    protected String cppGuardEnd() {
        return "#ifdef __cplusplus" + lineSep + '}' + lineSep + "#endif";
    }

    protected String guardBegin(String cname) {
        return "/* Header for class " + cname + " */" + lineSep + lineSep +
                "#ifndef _Included_" + cname + lineSep +
                "#define _Included_" + cname;
    }

    @SuppressWarnings("UnusedParameters")
    protected String guardEnd(String cname) {
        return "#endif";
    }

    /*
     * File name and file preamble related operations.
     */
    protected void writeFileTop(OutputStream o) throws Exit {
        PrintWriter pw = wrapWriter(o);
        pw.println("/* DO NOT EDIT THIS FILE - it is machine generated */" + lineSep +
                getIncludes());
    }

    protected String baseFileName(CharSequence className) {
        return Mangle.mangle(className, Mangle.Type.CLASS);
    }

    protected FileObject getFileObject(CharSequence className) throws IOException {
        String name = baseFileName(className) + getFileSuffix();
        return new RWFileObject(StandardLocation.SOURCE_OUTPUT, name, filer, outDirName);
    }


    protected String getFileSuffix() {
        return ".h";
    }

    /**
     * Including super classes' fields.
     */

    List<VariableElement> getAllFields(TypeElement subclazz) {
        List<VariableElement> fields = new ArrayList<VariableElement>();
        ArrayDeque<TypeElement> s = new ArrayDeque<TypeElement>();

        TypeElement cd = subclazz;
        while (true) {
            s.push(cd);
            TypeElement c = (TypeElement) (types.asElement(cd.getSuperclass()));
            if (c == null)
                break;
            cd = c;
        }

        while (!s.isEmpty()) {
            cd = s.pop();
            fields.addAll(ElementFilter.fieldsIn(cd.getEnclosedElements()));
        }

        return fields;
    }

    // c.f. MethodDoc.signature
    String signature(ExecutableElement e) {
        StringBuilder sb = new StringBuilder("(");
        String sep = "";
        for (VariableElement p : e.getParameters()) {
            sb.append(sep);
            sb.append(types.erasure(p.asType()).toString());
            sep = ",";
        }
        sb.append(')');
        return sb.toString();
    }
}

