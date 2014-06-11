package jniHelper.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jason on 6/10/14.
 */
public enum JNIProcessorOption {


    VERBOSE("v", "verbose") {
        @Override
        public void handle(JNIProcessorConfig ctx, String optionStr) {
            ctx.setVerbose(booleanVal(optionStr));

        }
    },
    FORCE("f", "force") {
        @Override
        public void handle(JNIProcessorConfig ctx, String optionStr) {
            ctx.setForce(booleanVal(optionStr));
        }
    },
    OUT_DIR("o", "outDir") {
        @Override
        public void handle(JNIProcessorConfig ctx, String optionStr) {
            ctx.setOutDir(optionStr);
        }
    },
    OUT_FILE("of", "outFile") {
        @Override
        public void handle(JNIProcessorConfig ctx, String optionStr) {
            ctx.setOutFile(optionStr);
        }
    };

    static boolean booleanVal(String str) throws IllegalArgumentException {
        str = str.trim().toLowerCase();
        if ("true".equals(str)) return true;
        if ("false".equals(str)) return false;
        throw new IllegalArgumentException("expected one of [true,false] but got " + str);
    }

    public abstract void handle(JNIProcessorConfig ctx, String optionStr) throws IllegalArgumentException;

    JNIProcessorOption(String... names) {
        this.names = names;
    }

    public void addOptionsTo(Set<String> opts) {
        for (String s : names) {
            opts.add("javah." + s);
        }
    }

    public String[] getOptionNames() {
        String[] oNames = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            oNames[i] = "javah." + names[i];
        }
        return oNames;
    }

    public static JNIProcessorConfig createConfig(Map<String, String> map) {
        JNIProcessorConfig config = new JNIProcessorConfig();
        for (String key : map.keySet()) {
            opts.get(key).handle(config, map.remove(key));
        }
        assert map.isEmpty();
        return config;
    }

    static final Map<String, JNIProcessorOption> opts;

    static {
        opts = new HashMap<String, JNIProcessorOption>(values().length * 2);
        for (JNIProcessorOption option : values()) {
            for (String name : option.getOptionNames()) {
                opts.put(name, option);
            }
        }


    }

    public static Set<String> getSupportedOptions() {
        return opts.keySet();
    }

    private final String[] names;
}
