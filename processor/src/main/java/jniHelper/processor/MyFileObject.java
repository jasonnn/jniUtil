package jniHelper.processor;

import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;

/**
 * Created by jason on 6/9/14.
 */
public class MyFileObject /*extends SimpleJavaFileObject*/ implements FileObject{
    final JavaFileManager.Location location;
    final CharSequence name;
    final Filer filer;

    public MyFileObject(JavaFileManager.Location location, CharSequence name, Filer filer) {
        this.location = location;
        this.name = name;
        this.filer = filer;
    }

    protected FileObject forReading() throws IOException {
        return filer.getResource(location,"",name);
    }
    protected FileObject forWriting() throws IOException{
        return filer.createResource(location,"",name);
    }

    @NotNull
    @Override
    public URI toUri() {
        try {
            return forWriting().toUri();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public InputStream openInputStream() throws IOException {
       return forReading().openInputStream();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return forWriting().openOutputStream();
    }

    @NotNull
    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return forReading().openReader(ignoreEncodingErrors);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return forReading().getCharContent(ignoreEncodingErrors);
    }

    @NotNull
    @Override
    public Writer openWriter() throws IOException {
        return forWriting().openWriter();
    }

    @Override
    public long getLastModified() {
        long result=0L;
        try {
            result= forReading().getLastModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean delete() {
        boolean result=false;
        try {
            result= forWriting().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
//    /**
//     * Construct a SimpleJavaFileObject of the given kind and with the
//     * given URI.
//     *
//     * @param uri  the URI for this file object
//     * @param kind the kind of this file object
//     */
//    protected MyFileObject(URI uri, Kind kind) {
//        super(uri, kind);
//    }



}
