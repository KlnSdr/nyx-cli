package nyx.util;

import dobby.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class RepoHelper {
    private static final Logger LOGGER = new Logger(RepoHelper.class, true);
    private static final String repoDir = System.getProperty("user.home") + "/.nyx";

    public static boolean existsInRepo(String group, String name, String version) {
        createRepoIfNotExists();
        final File file = new File(repoDir, group + "/" + name + "/" + version + "/" + name + "-" + version + ".jar");
        return file.exists();
    }

    public static boolean downloadToRepo(String group, String name, String version) {
        createRepoIfNotExists();
        LOGGER.info("Downloading dependency: " + group + ":" + name + ":" + version);

        // create the group and name directories if they don't exist
        createDirIfNotExists(repoDir + "/" + group);
        createDirIfNotExists(repoDir + "/" + group + "/" + name);
        createDirIfNotExists(repoDir + "/" + group + "/" + name + "/" + version);

        return downloadFile("http://localhost:3000/" + "/" + name + "/v" + version + "/" + name + ".jar", repoDir + "/" + group + "/" + name + "/" + version + "/" + name + "-" + version + ".jar");
    }

    private static boolean downloadFile(String fileURL, String savePath) {
        try (InputStream in = new URL(fileURL).openStream();
             OutputStream out = new FileOutputStream(savePath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to download file: " + fileURL);
            LOGGER.trace(e);
            return false;
        }
    }

    private static void createRepoIfNotExists() {
        // create the dir ~/.nyx if it doesn't exist
        createDirIfNotExists(repoDir);
    }

    private static void createDirIfNotExists(String path) {
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
