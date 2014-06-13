package jniHelper.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.truth0.Truth;

import javax.tools.JavaFileObject;

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