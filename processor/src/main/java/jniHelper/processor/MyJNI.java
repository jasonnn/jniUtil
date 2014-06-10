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
public class MyJNI extends JNI{
    Filer filer;
    public MyJNI(Util util,Filer filer) {
        super(util);
        this.filer=filer;
    }

    @Override
    protected FileObject getFileObject(CharSequence className) throws IOException {
        String name = baseFileName(className) + getFileSuffix();
        return new MyFileObject(StandardLocation.NATIVE_HEADER_OUTPUT,name,filer);
        //return filer.getResource(StandardLocation.NATIVE_HEADER_OUTPUT,"",name);
    // return    filer.createResource(StandardLocation.NATIVE_HEADER_OUTPUT,"",name);
    }
}
