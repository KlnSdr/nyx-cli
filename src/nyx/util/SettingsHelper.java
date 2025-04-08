package nyx.util;

import common.logger.Logger;
import dobby.exceptions.MalformedJsonException;
import dobby.util.json.NewJson;
import nyx.exceptions.InvalidConfigException;
import nyx.settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SettingsHelper {
    private static final Logger LOGGER = new Logger(SettingsHelper.class, true);

    public static Settings getSettings() {
        return readSettingsFile();
    }

    public static boolean writeSettings(Settings settings) {
        try {
            final File file = new File(RepoHelper.getRepoDir(), "settings.json");
            if (!file.exists()) {
                file.createNewFile();
            }
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(settings.toJson().toString().getBytes());
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to write settings.json file");
            LOGGER.trace(e);
            return false;
        }
    }

    private static Settings readSettingsFile() {
        try {
            final File file = new File(RepoHelper.getRepoDir(), "settings.json");
            final FileInputStream fileInputStream = new FileInputStream(file);

            final String content = new String(fileInputStream.readAllBytes());

            fileInputStream.close();
            final NewJson settings = NewJson.parse(content);
            if (validateSettingsFile(settings)) {
                return Settings.fromJson(settings);
            } else {
                throw new InvalidConfigException();
            }
        } catch (IOException | MalformedJsonException | InvalidConfigException e) {
            return null;
        }
    }

    private static boolean validateSettingsFile(NewJson settings) {
        if (settings == null) {
            return false;
        }

        if (!settings.hasKey("repos")) {
            return false;
        }

        final List<Object> repos = settings.getList("repos");
        if (repos == null) {
            return false;
        }

        for (Object repo : repos) {
            if (!(repo instanceof NewJson)) {
                return false;
            }

            if (!((NewJson) repo).hasKeys("url", "token")) {
                return false;
            }
        }

        return true;
    }
}
