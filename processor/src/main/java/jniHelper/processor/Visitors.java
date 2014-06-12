package jniHelper.processor;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;

/**
 * Created by jason on 6/11/14.
 */
public class Visitors {
    private Visitors() {
    }


    public static boolean hasNativeMethods(TypeElement e) {
        return e.accept(NATIVE_METHOD_VISITOR, noVerify());
    }

    public static boolean hasNativeMethods_verify(TypeElement e, Types types) {
        return e.accept(NATIVE_METHOD_VISITOR, verify(types));
    }

    static NativeCtx noVerify() {
        return NativeCtx.NO_VERIFY;
    }

    static NativeCtx verify(Types types) {
        return new NativeCtx(true, types);
    }

    private static class NativeCtx {
        private static final NativeCtx NO_VERIFY = new NativeCtx(false, null);

        final boolean verify;
        final Types types;

        public NativeCtx(boolean verify, Types types) {
            this.verify = verify;
            this.types = types;
        }
    }

    static boolean isNativeMethod(ExecutableElement e) {
        return e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.NATIVE);
    }

    static final ElementVisitor<Boolean, NativeCtx> NATIVE_METHOD_VISITOR = new SimpleElementVisitor6<Boolean, NativeCtx>(Boolean.FALSE) {
        @Override
        public Boolean visitType(@NotNull TypeElement e, NativeCtx ctx) {
            for (Element child : e.getEnclosedElements()) {
                if (child.accept(this, ctx).equals(Boolean.TRUE)) return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitExecutable(@NotNull ExecutableElement e, NativeCtx ctx) {
            boolean isNativeMethod = isNativeMethod(e);
            if (isNativeMethod && ctx.verify) {
                for (VariableElement p : e.getParameters()) {
                    verifyMethodParameters(p.asType(), ctx.types);
                }

            }
            return isNativeMethod;
        }
    };


    static void verifyMethodParameters(TypeMirror mirror, Types types) {
        mirror.accept(METHOD_PARAMETER_CHECKER, types);
    }

    static TypeVisitor<Void, Types> METHOD_PARAMETER_CHECKER = new SimpleTypeVisitor6<Void, Types>() {
        @Override
        public Void visitArray(ArrayType t, Types types) {
            visit(t.getComponentType(), types);
            return null;
        }

        @Override
        public Void visitDeclared(DeclaredType t, Types types) {
            t.asElement().getKind(); // ensure class exists
            for (TypeMirror st : types.directSupertypes(t))
                visit(st, types);
            return null;
        }

    };
}
