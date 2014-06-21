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
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
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

    protected Elements elements = null;
    protected Filer filer = null;
    protected ProcessingEnvironment env = null;
    JNI jni = null;

    protected boolean verify = false;

    @Override
    public void init(@NotNull ProcessingEnvironment processingEnv) {
        this.env = processingEnv;
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        JNIProcessorConfig config = JNIProcessorConfig.fromEnv(processingEnv);

        this.verify = config.verify;


        try {
            jni = createJNI(config);
        } catch (IOException e) {
            handleException(e);
        }

    }

    JNI createJNI(JNIProcessorConfig config) throws IOException {

        JNILogger logger = new JNILogger(config.verbose, env.getMessager());
        FileObject outFile = config.outFile != null ? filer.createResource(StandardLocation.SOURCE_OUTPUT, "", config.outFile) : null;
        return new JNI(logger, env, config.force, config.outDir, outFile);


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

    private static final Set<String> SUPPORTED_ANNOTATIONS = Collections.singleton("*");

    @NotNull
    @Override
    public Set<String> getSupportedOptions() {
        return JNIProcessorConfig.getSupportedOptions();
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

    @NotNull
    @Override
    public Iterable<? extends Completion> getCompletions(Element element,
                                                         AnnotationMirror annotation,
                                                         ExecutableElement member,
                                                         String userText) {
        return Collections.emptySet();
    }
}
