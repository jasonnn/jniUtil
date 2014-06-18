package compiletesting;

import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;

/**
 * Created by jason on 6/17/14.
 */
public abstract class TestingProcessor implements Processor {
    @NotNull
    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    protected ProcessingEnvironment env;

    @Override
    public void init(@NotNull ProcessingEnvironment processingEnv) {
        env = processingEnv;
    }

    private void logException(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, sw.toString());
    }

    protected abstract void doProcess(Set<TypeElement> roots) throws IOException;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            doProcess(ElementFilter.typesIn(roundEnv.getRootElements()));
        }catch (IOException e){
            logException(e);
        }
        return false;
    }

    @NotNull
    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptySet();
    }
}
