package nyx.util;

import dobby.exceptions.MalformedJsonException;
import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import nyx.config.ProjectConfig;
import nyx.exceptions.InvalidConfigException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ProjectHelper {
    private static final Logger LOGGER = new Logger(ProjectHelper.class, true);
    public static String getProjectDir() {
        return System.getProperty("user.dir");
    }

    public static ProjectConfig getProjectConfig() {
        final String projectDir = getProjectDir();
        final String content;

        try {
            final File file = new File(projectDir, "nyx.json");
            FileInputStream fileInputStream = new FileInputStream(file);

            // Read all bytes from the file into a byte array
            content = new String(fileInputStream.readAllBytes());

            // Close the resource
            fileInputStream.close();
            final NewJson projectConfig = NewJson.parse(content);
            if (validateProjectConfig(projectConfig)) {
                return ProjectConfig.fromJson(projectConfig);
            } else {
                throw new InvalidConfigException();
            }
        } catch (IOException | MalformedJsonException | InvalidConfigException e) {
            LOGGER.error("Failed to read nyx.json file");
            LOGGER.trace(e);
            return null;
        }
    }

    public static NewJson emptyConfig() {
        final NewJson projectConfig = new NewJson();

        final NewJson compiler = new NewJson();
        compiler.setString("version", "21");
        projectConfig.setJson("compiler", compiler);

        final NewJson project = new NewJson();
        project.setString("group", "com.example");
        project.setString("name", "project-name");
        project.setString("version", "1.0.0");
        project.setString("entry", "com.example.Main");
        project.setList("dependencies", List.of());
        projectConfig.setJson("project", project);

        projectConfig.setString("remoteRepoUrl", "https://repo.klnsdr.com");
        projectConfig.setList("exclude", List.of());

        return projectConfig;
    }

    private static boolean validateProjectConfig(NewJson projectConfig) {
        if (!projectConfig.hasKeys("compiler", "project")) {
            return false;
        }

        if (!projectConfig.hasKeys("remoteRepoUrl")) {
            return false;
        }

        if (!projectConfig.hasKey("compiler.version")) {
            return false;
        }

        if (!projectConfig.hasKey("exclude")) {
            return false;
        }

        if (!projectConfig.hasKeys("project.name", "project.dependencies", "project.version", "project.entry", "project.group")) {
            return false;
        }

        final List<Object> dependencies = projectConfig.getList("project.dependencies");

        if (dependencies == null) {
            return false;
        }

        for (Object dependency : dependencies) {
            if (!(dependency instanceof NewJson)) {
                return false;
            }

            if (!((NewJson) dependency).hasKeys("name", "version", "group")) {
                return false;
            }
        }

        return true;
    }
}
