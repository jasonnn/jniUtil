package jniHelper.processor;

import javah.JNI;
import javah.JNILogger;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import java.io.IOException;

/**
 * Created by jason on 6/9/14.
 */
@SuppressWarnings("StringConcatenationMissingWhitespace")
public class MyJNI extends JNI {
    private final Filer filer;

    public MyJNI(JNILogger log, Filer filer) {
        super(log);
        this.filer = filer;
    }

    @Override
    protected FileObject getFileObject(CharSequence className) throws IOException {
        String name = baseFileName(className) + getFileSuffix();
        return new MyFileObject(NATIVE_HEADERS_DIR.INSTANCE, name, filer);
        //return filer.getResource(StandardLocation.NATIVE_HEADER_OUTPUT,"",name);
    }
}
