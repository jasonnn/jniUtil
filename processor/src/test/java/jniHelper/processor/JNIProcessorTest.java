package jniHelper.processor;

import com.google.testing.compile.JavaFileObjects;
import compiletesting.TestingProcessor;
import org.junit.Test;
import org.truth0.Truth;

import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.tools.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class JNIProcessorTest {
    static final JavaFileObject MY_CLASS_TEST = JavaFileObjects.forResource(JNIProcessorTest.class.getResource("/pkg/MyClass.java"));
    static final JavaFileObject MY_CLASS_EXPECTED = JavaFileObjects.forResource(JNIProcessorTest.class.getResource("/pkg/pkg_MyClass.h"));


    @Test
    public void testThatItWorks() throws Exception {
        Truth.ASSERT.about(javaSource())
                .that(MY_CLASS_TEST)
                .processedWith(new JNIProcessor())
                .compilesWithoutError()
                .and().generatesFiles(MY_CLASS_EXPECTED);

    }

    @Test
    public void testThatItWorks2() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager jfm = compiler.getStandardFileManager(collector, Locale.getDefault(), Charset.defaultCharset());
        File srcOut = new File("out/srcOut");
        srcOut.mkdirs();
        File compOut = new File("out/compOut");
        compOut.mkdirs();

        jfm.setLocation(StandardLocation.SOURCE_OUTPUT, Collections.singleton(srcOut));
        jfm.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(compOut));

        System.out.println("custom? "+jfm.hasLocation(CUSTOM));

        JavaCompiler.CompilationTask task = compiler.getTask(new PrintWriter(System.out, true), jfm, collector, Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.singletonList(MY_CLASS_TEST));
        task.setLocale(Locale.getDefault());
        task.setProcessors(Collections.singleton(processor));
        task.call();

        for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
            System.out.println("diagnostic = " + diagnostic);
        }

    }

    static final JavaFileManager.Location CUSTOM = new JavaFileManager.Location() {
        @Override
        public String getName() {
            return "CUSTOM";
        }

        @Override
        public boolean isOutputLocation() {
            return true;
        }
    };

    static final Processor processor = new TestingProcessor() {
        @Override
        protected void doProcess(Set<TypeElement> roots) throws IOException {
            FileObject res = env.getFiler().createResource(CUSTOM, "", "tst");
            Writer w = res.openWriter();
            w.write("asdasdasd");
            w.close();
        }
    };

    //    static Field resultField;
//    static Field genFilesField;
//
//    static {
//        try {
//            setUp();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//    static void setUp() throws Exception {
//        Class succBuilder = Class.forName("com.google.testing.compile.JavaSourcesSubject$SuccessfulCompilationBuilder");
//        Field f = succBuilder.getDeclaredField("result");
//        f.setAccessible(true);
//        resultField = f;
//        Class compResult = Class.forName("com.google.testing.compile.Compilation$Result");
//        Field genFiles = compResult.getDeclaredField("generatedFilesByKind");
//        genFiles.setAccessible(true);
//        genFilesField = genFiles;
//    }
//
//    @SuppressWarnings("unchecked")
//    static ImmutableListMultimap<JavaFileObject.Kind, JavaFileObject>
//    generatedFilesByKind(CompileTester.SuccessfulCompilationClause clause) throws IllegalAccessException {
//        Object resultObj = resultField.get(clause);
//        return (ImmutableListMultimap<JavaFileObject.Kind, JavaFileObject>) genFilesField.get(resultObj);
//    }


//    @Test
//    public void testThatItWorks() throws Exception {
//        CompileTester.SuccessfulCompilationClause result = Truth.ASSERT
//                .about(javaSource())
//                .that(MY_CLASS_TEST)
//                .processedWith(new JNIProcessor())
//                .compilesWithoutError();
//
//        JavaFileObject gen = generatedFilesByKind(result).get(JavaFileObject.Kind.OTHER).get(0);
//
//        String expected = MY_CLASS_EXPECTED.getCharContent(true).toString();
//        String actual = gen.getCharContent(true).toString();
//
//        assertEquals(expected, actual);
//
//        assertThat(gen.toUri().getPath(), endsWith(".h"));
//
//
//    }
}