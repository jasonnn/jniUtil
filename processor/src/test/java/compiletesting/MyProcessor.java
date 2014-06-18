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
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

/**
 * Created by jason on 6/13/14.
 */
class MyProcessor implements Processor {

    JavaFileManager.Location location;

    public MyProcessor(JavaFileManager.Location location) {
        this.location = location;
    }

    MyProcessor() {
        this(StandardLocation.SOURCE_OUTPUT);
    }

    void makeFile() throws IOException {
        FileObject obj = env.getFiler().createResource(location, "", "myFile");
        Writer writer = obj.openWriter();
        writer.write("actual out");
        writer.close();
    }

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

    ProcessingEnvironment env = null;

    @Override
    public void init(@NotNull ProcessingEnvironment processingEnv) {
        this.env = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                makeFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @NotNull
    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptySet();
    }

}
