package com.vlad2305m;

import java.io.IOException;

public class PlatformSpecificStuff {

    public static boolean isLinux() {
        return System.getProperty("os.name").matches(".*[Ll]inux.*");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").matches(".*Windows.*");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").matches(".*[Mm]ac.*");
    }

    public static String qalcFile() {
        if (isLinux()) return qalcDir()+"qalc";
        if (isWindows()) return qalcDir()+"qalc.exe";
        return "qalc";
    }

    public static String qalculateFile() {
        if (isLinux()) return qalcDir()+"qalculate";
        if (isWindows()) return qalcDir()+"qalculate-gtk.exe";
        return "qalculate-gtk";
    }

    public static String qalcDir() {
        if (isLinux()) return "./config/chatqalc/qalculate/";
        if (isWindows()) return "./config/chatqalc/qalculate/";
        return "";
    }
}
