package javah;

import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import java.io.*;
import java.net.URI;

/**
 * Created by jason on 6/10/14.
 */
public class RWFileObject implements FileObject {

    private final JavaFileManager.Location location;
    private final CharSequence name;
    private final Filer filer;
    private final String relativeDir;

    public RWFileObject(JavaFileManager.Location location, CharSequence name, Filer filer, String relativeDir) {
        this.location = location;
        this.name = name;
        this.filer = filer;
        this.relativeDir = relativeDir;
    }

    protected FileObject forReading() throws IOException {
        return filer.getResource(location, relativeDir, name);
    }

    protected FileObject forWriting() throws IOException {
        return filer.createResource(location, relativeDir, name);
    }

    @NotNull
    @Override
    public URI toUri() {
        try {
            return forWriting().toUri();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        long result = 0L;
        try {
            result = forReading().getLastModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean delete() {
        boolean result = false;
        try {
            result = forWriting().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String toString() {
        return "RW_FILE_OBJECT{" +
                "name=" + name +
                ", relDir=" + relativeDir +
                ", location=" + location +
                '}';
    }
}
