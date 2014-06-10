package jniHelper.processor;

import javah.JNI;
import javah.Util;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;

/**
 * Created by jason on 6/9/14.
 */
@SuppressWarnings("StringConcatenationMissingWhitespace")
public class MyJNI extends JNI {
    private final Filer filer;

    public MyJNI(Util util, Filer filer) {
        super(util);
        this.filer = filer;
    }

    @Override
    protected FileObject getFileObject(CharSequence className) throws IOException {
        String name = baseFileName(className) + getFileSuffix();
        return new MyFileObject(NATIVE_HEADERS_DIR.INSTANCE, name, filer);
        //return filer.getResource(StandardLocation.NATIVE_HEADER_OUTPUT,"",name);
    }
}
