package jniHelper.processor;

import java.util.Map;

/**
 * Created by jason on 6/10/14.
 */
public class JNIProcessorConfig {

    public static JNIProcessorConfig DEFAULT = new JNIProcessorConfig(){
        @Override
        public void setVerbose(boolean verbose) {throw new UnsupportedOperationException("immutable");}

        @Override
        public boolean isVerbose() {
            return false;
        }

        @Override
        public void setForce(boolean force) {
            throw new UnsupportedOperationException("immutable");
        }

        @Override
        public boolean isForce() {
            return false;
        }

        @Override
        public void setOutDir(String outDir) {
            throw new UnsupportedOperationException("immutable");
        }

        @Override
        public String getOutDir() {
            return "";
        }

        @Override
        public void setOutFile(String outFile) {
            throw new UnsupportedOperationException("immutable");
        }

        @Override
        public String getOutFile() {
            return "";
        }
    };

    public static JNIProcessorConfig fromMap(Map<String, String> config) {
        return JNIProcessorOption.createConfig(config);
    }

    private boolean verbose = false;
    private boolean force = false;
    private String outDir = null;
    private String outFile = null;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isForce() {
        return force;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    public String getOutDir() {
        return outDir;
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    public String getOutFile() {
        return outFile;
    }
}
