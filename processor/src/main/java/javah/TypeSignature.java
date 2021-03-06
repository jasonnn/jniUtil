/*
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

import javah.ex.SignatureException;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Returns internal type signature.
 * <p/>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.</b></p>
 *
 * @author Sucheta Dambalkar
 */

public class TypeSignature {

    private final Elements elems;

    /* Signature Characters */

    private static final String SIG_VOID = "V";
    private static final String SIG_BOOLEAN = "Z";
    private static final String SIG_BYTE = "B";
    private static final String SIG_CHAR = "C";
    private static final String SIG_SHORT = "S";
    private static final String SIG_INT = "I";
    private static final String SIG_LONG = "J";
    private static final String SIG_FLOAT = "F";
    private static final String SIG_DOUBLE = "D";
//    private static final String SIG_ARRAY = "[";
//    private static final String SIG_CLASS = "L";


    public TypeSignature(Elements elems) {
        this.elems = elems;
    }

    /*
     * Returns the type signature of a field according to JVM specs
     */
//    public String getTypeSignature(String javasignature) throws SignatureException {
//        return getParamJVMSignature(javasignature);
//    }

    /*
     * Returns the type signature of a method according to JVM specs
     */
    public String getTypeSignature(String javasignature, TypeMirror returnType)
            throws SignatureException {
        StringBuilder typeSignature = new StringBuilder();//null; //Internal type signature.
        List<String> params = new ArrayList<String>(); //List of parameters.
        StringBuilder returnJVMType = new StringBuilder();// = null; //Internal return type signature.

        int startIndex = -1;
        int endIndex = -1;

        // Gets the actual java signature without parentheses.
        if (javasignature != null) {
            startIndex = javasignature.indexOf('(');
            endIndex = javasignature.indexOf(')');
        }

        String signature = null; //Java type signature.
        if (((startIndex != -1) && (endIndex != -1))
                && (startIndex + 1 < javasignature.length())
                && (endIndex < javasignature.length())) {
            signature = javasignature.substring(startIndex + 1, endIndex);
        }

        // Separates parameters.
        if (signature != null) {
            if (signature.contains(",")) {
                StringTokenizer st = new StringTokenizer(signature, ",");
                while (st.hasMoreTokens()) {
                    params.add(st.nextToken());
                }
            } else {
                params.add(signature);
            }
        }

        /* JVM type signature. */
        typeSignature.append('(');

        // Gets indivisual internal parameter signature.
        int i = 0;
        while (!params.isEmpty()) {
            String paramsig = params.remove(i).trim(); //Java parameter signature.
            String paramJVMSig = getParamJVMSignature(paramsig); //Internal parameter signature.
            if (paramJVMSig != null) {
                typeSignature.append(paramJVMSig);
            }
        }

        typeSignature.append(')');

        // Get internal return type signature.

        //returnJVMType = "";
        int dimensions = 0; //Array dimension.
        if (returnType != null) {
            dimensions = dimensions(returnType);
        }

        //Gets array dimension of return type.
        while (dimensions-- > 0) {
            returnJVMType.append('[');
        }
        if (returnType != null) {
            String returnSig = qualifiedTypeName(returnType); //Java return type signature.
            returnJVMType.append(getComponentType(returnSig));
        } else {
            //TODO JNILogger
            System.out.println("Invalid return type.");
        }

        typeSignature.append(returnJVMType);

        return typeSignature.toString();
    }

    /*
     * Returns internal signature of a parameter.
     */
    private String getParamJVMSignature(String paramsig) throws SignatureException {
        StringBuilder paramJVMSig = new StringBuilder();

        if (paramsig != null) {

            String componentType;// = "";
            if (paramsig.contains("[]")) {
                // Gets array dimension.
                int endindex = paramsig.indexOf("[]");
                componentType = paramsig.substring(0, endindex);
                String dimensionString = paramsig.substring(endindex);
                while (dimensionString.contains("[]")) {
                    paramJVMSig.append('[');
                    int beginindex = dimensionString.indexOf(']') + 1;
                    if (beginindex < dimensionString.length()) {
                        dimensionString = dimensionString.substring(beginindex);
                    } else
                        dimensionString = "";
                }
            } else componentType = paramsig;

            paramJVMSig.append(getComponentType(componentType));
        }
        return paramJVMSig.toString();
    }

    /*
     * Returns internal signature of a component.
     */
    private String getComponentType(String componentType) throws SignatureException {

        String JVMSig = "";

        if (componentType != null) {
            if (componentType.equals("void")) JVMSig += SIG_VOID;
            else if (componentType.equals("boolean")) JVMSig += SIG_BOOLEAN;
            else if (componentType.equals("byte")) JVMSig += SIG_BYTE;
            else if (componentType.equals("char")) JVMSig += SIG_CHAR;
            else if (componentType.equals("short")) JVMSig += SIG_SHORT;
            else if (componentType.equals("int")) JVMSig += SIG_INT;
            else if (componentType.equals("long")) JVMSig += SIG_LONG;
            else if (componentType.equals("float")) JVMSig += SIG_FLOAT;
            else if (componentType.equals("double")) JVMSig += SIG_DOUBLE;
            else {
                if (!componentType.isEmpty()) {
                    TypeElement classNameDoc = elems.getTypeElement(componentType);

                    if (classNameDoc == null) {
                        throw new SignatureException(componentType);
                    } else {
                        String classname = classNameDoc.getQualifiedName().toString();
                        String newclassname = classname.replace('.', '/');
                        JVMSig += "L";
                        JVMSig += newclassname;
                        JVMSig += ";";
                    }
                }
            }
        }
        return JVMSig;
    }

    int dimensions(TypeMirror t) {
        if (t.getKind() != TypeKind.ARRAY)
            return 0;
        return 1 + dimensions(((ArrayType) t).getComponentType());
    }


    String qualifiedTypeName(TypeMirror type) {
        TypeVisitor<Name, Void> v = new SimpleTypeVisitor6<Name, Void>() {
            @Override
            public Name visitArray(ArrayType t, Void p) {
                return t.getComponentType().accept(this, p);
            }

            @Override
            public Name visitDeclared(DeclaredType t, Void p) {
                return ((TypeElement) t.asElement()).getQualifiedName();
            }

            @Override
            public Name visitPrimitive(@NotNull PrimitiveType t, Void p) {
                return elems.getName(t.toString());
            }

            @Override
            public Name visitNoType(@NotNull NoType t, Void p) {
                if (t.getKind() == TypeKind.VOID)
                    return elems.getName("void");
                return defaultAction(t, p);
            }

            @Override
            public Name visitTypeVariable(TypeVariable t, Void p) {
                return t.getUpperBound().accept(this, p);
            }
        };
        return v.visit(type).toString();
    }
}
