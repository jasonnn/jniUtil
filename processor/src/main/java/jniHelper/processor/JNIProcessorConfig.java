package jniHelper.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jason on 6/10/14.
 */
public class JNIProcessorConfig {

    public static enum RecognizedOption {
        VERBOSE("v", "verbose"),
        VERIFY("verify"),
        FORCE("f", "force"),
        OUT_DIR("o", "outDir"),
        OUT_FILE("of", "outFile");

        private final String[] names;

        void handle(Builder builder, String value) {
            switch (this) {
                case VERBOSE:
                    builder.setVerbose(booleanVal(value));
                    break;
                case VERIFY:
                    builder.setVerify(booleanVal(value));
                    break;
                case FORCE:
                    builder.setForce(booleanVal(value));
                    break;
                case OUT_DIR:
                    builder.setOutDir(value);
                    break;
                case OUT_FILE:
                    builder.setOutFile(value);
                    break;
            }
        }

        RecognizedOption(String... names) {
            this.names = names;
        }

        public String[] getOptionNames() {
            String[] oNames = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                oNames[i] = "javah." + names[i];
            }
            return oNames;
        }
    }

    private static final Map<String, RecognizedOption> opts;

    static {
        opts = new HashMap<String, RecognizedOption>(RecognizedOption.values().length * 2);
        for (RecognizedOption option : RecognizedOption.values()) {
            for (String name : option.getOptionNames()) {
                opts.put(name, option);
            }
        }


    }

    public static Set<String> getSupportedOptions() {
        return opts.keySet();
    }

    public static JNIProcessorConfig fromEnv(ProcessingEnvironment environment) {
        Builder builder = newBuilder().setMessager(environment.getMessager());
        for (Map.Entry<String, String> entry : environment.getOptions().entrySet()) {
            opts.get(entry.getKey()).handle(builder, entry.getValue());
        }
        return builder.build();
    }

    static boolean booleanVal(String str) throws IllegalArgumentException {
        if (str == null) return true;
        str = str.trim().toLowerCase();
        if ("true".equals(str) || "t".equals(str) || "yes".equals(str) || "y".equals(str)) return true;
        if ("false".equals(str) || "f".equals(str) || "no".equals(str) || "n".equals(str)) return false;
        throw new IllegalArgumentException("expected one of [true,false] but got " + str);
    }

    public JNIProcessorConfig(@NotNull Messager messager, boolean verbose, boolean force, boolean verify, @Nullable String outDir, @Nullable String outFile) {
        this.messager = messager;
        this.verbose = verbose;
        this.force = force;
        this.verify = verify;


        if (outDir == null && outFile == null) {
            this.outDir = "headers";
            this.outFile = null;
        } else if ((outDir == null) ^ (outFile == null)) {
            this.outDir = outDir;
            this.outFile = outFile;
        } else {
            throw new IllegalArgumentException("cant set outDir and outFile");
        }


    }

    @NotNull
    public final Messager messager;
    public final boolean verbose;
    public final boolean force;
    public final boolean verify;
    @Nullable
    public final String outDir;
    @Nullable
    public final String outFile;

    public boolean singleFile() {
        return outFile != null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Messager messager = null;
        private boolean verbose = false;
        private boolean force = false;
        private boolean verify = false;
        private String outDir = null;
        private String outFile = null;

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder setForce(boolean force) {
            this.force = force;
            return this;
        }

        public Builder setVerify(boolean verify) {
            this.verify = verify;
            return this;
        }

        public Builder setOutDir(String outDir) {
            this.outDir = outDir;
            return this;
        }

        public Builder setOutFile(String outFile) {
            this.outFile = outFile;
            return this;
        }

        public JNIProcessorConfig build() {
            return new JNIProcessorConfig(messager, verbose, force, verify, outDir, outFile);
        }
    }
}
