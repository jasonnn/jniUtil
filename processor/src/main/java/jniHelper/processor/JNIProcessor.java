package jniHelper.processor;

import javah.IOUtils;
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
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jason on 6/9/14.
 */
public class JNIProcessor implements Processor {
    private static final Set<String> SUPPORTED_OPTIONS = Collections.emptySet(); //singleton("options");
    private static final Set<String> SUPPORTED_ANNOTATIONS = Collections.singleton("*");


    @NotNull
    @Override
    public Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
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


    protected Elements elements;
    protected Messager log;
    protected Filer filer;
    protected ProcessingEnvironment env;

    @Override
    public void init(@NotNull ProcessingEnvironment processingEnv) {
        this.env = processingEnv;
        elements = processingEnv.getElementUtils();
        log = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        //  parseOptions(processingEnv.getOptions().get("options"));

    }

    protected void doProcess(Set<? extends Element> rootElements) throws IOException, ClassNotFoundException {
        HashSet<TypeElement> natives = new HashSet<TypeElement>(rootElements.size());
        for (Element element : rootElements) {
            if (element.accept(NativeMethodScanner.INSTANCE, null)) {
                natives.add((TypeElement) element);
            }
        }

        DiagnosticListener<JavaFileObject> diagnosticListener = IOUtils.getDiagnosticListenerForStream(System.err);


        JNILogger util = new JNILogger(IOUtils.getPrintWriterForStream(System.out), diagnosticListener);
        util.verbose = true;
        JNI jni = new JNI(util);//new MyJNI(util,filer);
        jni.setProcessingEnvironment(env);
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
            log.printMessage(Diagnostic.Kind.ERROR, sw.toString());
        }

        return false;
    }

    @NotNull
    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptySet();
    }
}
