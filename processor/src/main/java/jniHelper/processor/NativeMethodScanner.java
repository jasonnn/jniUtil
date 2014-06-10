package jniHelper.processor;

import javax.lang.model.element.*;
import javax.lang.model.util.AbstractElementVisitor6;

/**
 * Created by jason on 6/9/14.
 */
public class NativeMethodScanner extends AbstractElementVisitor6<Boolean, Void> {
    public static final NativeMethodScanner INSTANCE = new NativeMethodScanner();

    @Override
    public Boolean visitPackage(PackageElement e, Void aVoid) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitType(TypeElement e, Void aVoid) {
        //TODO will this handle inner classes?
        for (Element element : e.getEnclosedElements()) {
            if (element.accept(this, null)) return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitVariable(VariableElement e, Void aVoid) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitExecutable(ExecutableElement e, Void aVoid) {
        return e.getModifiers().contains(Modifier.NATIVE);
    }

    @Override
    public Boolean visitTypeParameter(TypeParameterElement e, Void aVoid) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitUnknown(Element e, Void aVoid) {
        return Boolean.FALSE;
    }
}
