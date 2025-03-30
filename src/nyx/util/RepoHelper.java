package nyx.util;

import dobby.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static nyx.util.FileHelper.createDirIfNotExists;
import static nyx.util.FileHelper.getHome;

public class RepoHelper {
    private static final Logger LOGGER = new Logger(RepoHelper.class, true);
    private static final String repoDir = getHome() + "/.nyx";
    private final String remoteRepoUrl;

    public static RepoHelper getInstance(String remoteRepoUrl) {
        return new RepoHelper(remoteRepoUrl);
    }

    private RepoHelper(String remoteRepoUrl) {
        this.remoteRepoUrl = remoteRepoUrl;
    }

    public static String getRepoDir() {
        return repoDir;
    }

    public boolean existsInRepo(String group, String name, String version) {
        createRepoIfNotExists();
        final File file = new File(repoDir, group + "/" + name + "/" + version + "/" + name + "-" + version + ".jar");
        final File jsonFile = new File(repoDir, group + "/" + name + "/" + version + "/nyx.json");

        return file.exists() && jsonFile.exists();
    }

    public boolean downloadToRepo(String group, String name, String version) {
        createRepoIfNotExists();
        LOGGER.info("Downloading dependency: " + group + ":" + name + ":" + version);

        // create the group and name directories if they don't exist
        createDirIfNotExists(repoDir + "/" + group);
        createDirIfNotExists(repoDir + "/" + group + "/" + name);
        createDirIfNotExists(repoDir + "/" + group + "/" + name + "/" + version);

        return downloadFile(remoteRepoUrl + "/" + group + "/" + name + "/" + version + "/" + name + ".jar", repoDir + "/" + group + "/" + name + "/" + version + "/" + name + "-" + version + ".jar")
                && downloadFile(remoteRepoUrl + "/" + group + "/" + name + "/" + version + "/nyx.json", repoDir + "/" + group + "/" + name + "/" + version + "/nyx.json");
    }

    private boolean downloadFile(String fileURL, String savePath) {
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

    private void createRepoIfNotExists() {
        // create the dir ~/.nyx if it doesn't exist
        createDirIfNotExists(repoDir);
    }
}
