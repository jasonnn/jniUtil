package jniHelper.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Collections;
import java.util.Set;

/**
 * Created by jason on 6/9/14.
 */
public class JNIProcessor implements Processor {
    private static final Set<String> SUPPORTED_OPTIONS = Collections.emptySet();
    private static final Set<String> SUPPORTED_ANNOTATIONS = Collections.singleton("*");


    @Override
    public Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return SUPPORTED_ANNOTATIONS;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }


    protected Elements elements;
    protected Messager log;
    protected Filer filer;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        elements = processingEnv.getElementUtils();
        log = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptySet();
    }
}
