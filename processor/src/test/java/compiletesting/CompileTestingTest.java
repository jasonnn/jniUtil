package compiletesting;

import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import org.junit.Test;
import org.truth0.Truth;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * Created by jason on 6/13/14.
 */
public class CompileTestingTest {
    static final JavaFileObject src = JavaFileObjects.forSourceString("com.MyClass",
            "package com; public class MyClass{}");

    //  static final JavaFileObject EXPECTED_OUT = JavaFileObjects.forSourceString("myFile", "expected out");
    static final JavaFileObject EXPECTED_OUT = JavaFileObjects.forResource(CompileTestingTest.class.getResource("/myFile"));
    static final JavaFileObject ACTUALLY_GENERATED = JavaFileObjects.forSourceString("myFile", "actual out");

    @Test
    public void testGeneratedFilesAreDifferent() throws Exception {

        CompileTester.SuccessfulCompilationClause result = Truth.ASSERT.about(JavaSourceSubjectFactory.javaSource())
                .that(src).processedWith(new MyProcessor()).compilesWithoutError()
                .and().generatesFiles(EXPECTED_OUT);
        System.out.println(result);

    }

    @Test
    public void testCustomLocation() throws Exception {
        JavaFileManager.Location location = new JavaFileManager.Location() {
            @Override
            public String getName() {
                return "myLocation";
            }

            @Override
            public boolean isOutputLocation() {
                return true;
            }
        };

        CompileTester.SuccessfulCompilationClause result = Truth.ASSERT.about(JavaSourceSubjectFactory.javaSource())
                .that(src).processedWith(new MyProcessor(location)).compilesWithoutError()
                .and().generatesFiles(EXPECTED_OUT);

    }
}
