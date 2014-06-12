package jniHelper.processor;

import org.jetbrains.annotations.NotNull;

import javax.tools.JavaFileManager;

/**
 * Created by jason on 6/9/14.
 */
public enum NativeHeadersLocation implements JavaFileManager.Location {
    INSTANCE;

    @Override
    public String getName() {
        return "headers";
    }

    @Override
    public boolean isOutputLocation() {
        return true;
    }

    @NotNull
    @Override
    public String toString() {
        return "NATIVE_HEADERS_DIR::" + name() + "::" + getName();
    }
}
