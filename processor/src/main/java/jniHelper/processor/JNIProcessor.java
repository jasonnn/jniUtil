package jniHelper.processor;

import javah.JNI;
import javah.JNILogger;
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
import java.util.*;

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

    protected ProcessingEnvironment env = null;
    JNI jni = null;

    protected boolean verify = false;

    @Override
    public void init(@NotNull ProcessingEnvironment processingEnv) {
        this.env = processingEnv;


        JNIProcessorConfig config = JNIProcessorConfig.fromEnv(processingEnv);
        this.verify = config.verify;

        JNILogger logger = JNILogger.configuredWith(config);
        if (config.verbose) logger.log(config.toString());


        try {
            jni = JNI.builder()
                    .withLogger(logger)
                    .configuredWith(config)
                    .build();
        } catch (IOException e) {
            handleException(e);
        }

    }


    protected void doProcess(Set<? extends TypeElement> rootElements) throws IOException, ClassNotFoundException {
        HashSet<TypeElement> natives = new HashSet<TypeElement>(rootElements.size());

        NativeMethodFinder finder = verify ? NativeMethodFinder.withVerification(env.getTypeUtils())
                : NativeMethodFinder.noVerify();

        for (TypeElement element : rootElements) {

            if (finder.hasNativeMethods(element)) {
                natives.add(element);
            }
        }
        jni.setClasses(natives);
        jni.run();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {

            doProcess(getAllClasses(ElementFilter.typesIn(roundEnv.getRootElements())));
        } catch (Exception e) {
            handleException(e);
        }

        return false;
    }

    void handleException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, sw.toString());
    }

    private static Set<TypeElement> getAllClasses(Set<? extends TypeElement> classes) {
        Set<TypeElement> ret = new LinkedHashSet<TypeElement>();
        ArrayDeque<TypeElement> accum = new ArrayDeque<TypeElement>(classes);
        while (!accum.isEmpty()) {
            TypeElement e = accum.pop();
            ret.add(e);
            accum.addAll(ElementFilter.typesIn(e.getEnclosedElements()));
        }
        return ret;
    }

    @NotNull
    @Override
    public Set<String> getSupportedOptions() {
        return JNIProcessorConfig.getSupportedOptions();
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

    @NotNull
    @Override
    public Iterable<? extends Completion> getCompletions(Element element,
                                                         AnnotationMirror annotation,
                                                         ExecutableElement member,
                                                         String userText) {
        return Collections.emptySet();
    }
}
