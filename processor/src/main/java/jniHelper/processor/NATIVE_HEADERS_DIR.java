package jniHelper.processor;

import javax.tools.JavaFileManager;

/**
 * Created by jason on 6/9/14.
 */
public enum NATIVE_HEADERS_DIR implements JavaFileManager.Location{
    INSTANCE;
    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean isOutputLocation() {
        return true;
    }
}
