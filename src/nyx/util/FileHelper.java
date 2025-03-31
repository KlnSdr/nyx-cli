package nyx.util;

import common.logger.Logger;

import java.io.File;

public class FileHelper {
    private static final Logger LOGGER = new Logger(FileHelper.class, true);

    public static String getHome() {
        return System.getProperty("user.home");
    }

    public static void createDirIfNotExists(String path) {
        final File file = new File(path);
        if (!file.exists()) {
            final boolean didCreate = file.mkdir();
            if (!didCreate) {
                LOGGER.error("Failed to create directory: " + path);
                System.exit(1);
            }
        }
    }
}
