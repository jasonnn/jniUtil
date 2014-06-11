package jniHelper.processor;

import javah.JNI;
import javah.JNILogger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jason on 6/9/14.
 */
/*
call order:
init
sourceVersion
annotationTypes
getOptions
process
process
 */
public class JNIProcessor implements Processor {
    private static final Set<String> SUPPORTED_ANNOTATIONS = Collections.singleton("*");


    @NotNull
    @Override
    public Set<String> getSupportedOptions() {
        return JNIProcessorOption.getSupportedOptions();
    }

    @NotNull
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return SUPPORTED_ANNOTATIONS;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }


    protected Elements elements = null;
    protected Filer filer = null;
    protected ProcessingEnvironment env = null;
    JNI jni = null;

    @Override
    public void init(@NotNull ProcessingEnvironment processingEnv) {
        this.env = processingEnv;
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        //  config = JNIProcessorConfig.fromMap(processingEnv.getOptions());


        jni = new JNI(JNILogger.getDefault());
        jni.setProcessingEnvironment(env);

    }

    protected void doProcess(Set<? extends Element> rootElements) throws IOException, ClassNotFoundException {
        HashSet<TypeElement> natives = new HashSet<TypeElement>(rootElements.size());
        for (Element element : rootElements) {
            if (element.accept(NativeMethodScanner.INSTANCE, null)) {
                natives.add((TypeElement) element);
            }
        }

        jni.setClasses(natives);
        jni.run();

    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            doProcess(roundEnv.getRootElements());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, sw.toString());
        }

        return false;
    }

    @NotNull
    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptySet();
    }
}
