package jniHelper.processor;

import com.google.common.collect.ImmutableListMultimap;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.truth0.Truth;

import javax.tools.JavaFileObject;
import java.lang.reflect.Field;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.Assert.assertEquals;

public class JNIProcessorTest {
    static final JavaFileObject MY_CLASS_TEST = JavaFileObjects.forResource(JNIProcessorTest.class.getResource("/pkg/MyClass.java"));
    static final JavaFileObject MY_CLASS_EXPECTED = JavaFileObjects.forResource(JNIProcessorTest.class.getResource("/pkg/pkg_MyClass.h"));

    static Field resultField;
    static Field genFilesField;

    static {
        try {
            setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void setUp() throws Exception {
        Class succBuilder = Class.forName("com.google.testing.compile.JavaSourcesSubject$SuccessfulCompilationBuilder");
        Field f = succBuilder.getDeclaredField("result");
        f.setAccessible(true);
        resultField = f;
        Class compResult = Class.forName("com.google.testing.compile.Compilation$Result");
        Field genFiles = compResult.getDeclaredField("generatedFilesByKind");
        genFiles.setAccessible(true);
        genFilesField = genFiles;
    }

    @SuppressWarnings("unchecked")
    static ImmutableListMultimap<JavaFileObject.Kind, JavaFileObject>
    generatedFilesByKind(CompileTester.SuccessfulCompilationClause clause) throws IllegalAccessException {
        Object resultObj = resultField.get(clause);
        return (ImmutableListMultimap<JavaFileObject.Kind, JavaFileObject>) genFilesField.get(resultObj);
    }

    @Test
    public void testSomething() throws Exception {
        CompileTester.SuccessfulCompilationClause result = Truth.ASSERT
                .about(javaSource())
                .that(MY_CLASS_TEST)
                .processedWith(new JNIProcessor())
                .compilesWithoutError();


        JavaFileObject gen = generatedFilesByKind(result).get(JavaFileObject.Kind.OTHER).get(0);

        String expected = MY_CLASS_EXPECTED.getCharContent(true).toString();
        String actual = gen.getCharContent(true).toString();

        assertEquals(expected, actual);
        // assertThat(expected, equalToIgnoringWhiteSpace(actual));


    }
}