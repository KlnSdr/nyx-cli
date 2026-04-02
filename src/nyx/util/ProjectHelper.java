package nyx.util;

import dobby.exceptions.MalformedJsonException;
import dobby.util.json.NewJson;
import common.logger.Logger;
import nyx.config.ProjectConfig;
import nyx.exceptions.InvalidConfigException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ProjectHelper {
    private static final Logger LOGGER = new Logger(ProjectHelper.class, true);
    private static ProjectConfig cachedConfig;

    public static String getProjectDir() {
        return System.getProperty("user.dir");
    }

    public static ProjectConfig getProjectConfig() {
        if (cachedConfig == null) {
            cachedConfig = readConfigFile(getProjectDir());
        }
        return cachedConfig;
    }

    public static ProjectConfig getConfigOfDependency(String group, String name, String version) {
        final String path = RepoHelper.getRepoDir() + "/" + group + "/" + name + "/" + version;
        return readConfigFile(path);
    }

    private static ProjectConfig readConfigFile(String path) {
        final String content;
        try {
            final File file = new File(path, "nyx.json");
            FileInputStream fileInputStream = new FileInputStream(file);
            content = new String(fileInputStream.readAllBytes());
            fileInputStream.close();
            final NewJson projectConfig = validateProjectConfig(NewJson.parse(content));
            return ProjectConfig.fromJson(projectConfig);
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
        projectConfig.setList("replaceVarsIn", List.of());

        return projectConfig;
    }

    private static NewJson validateProjectConfig(NewJson projectConfig) throws InvalidConfigException {
        if (!projectConfig.hasKeys("compiler", "project")) {
            throw new InvalidConfigException("Missing 'compiler' or 'project' section");
        }

        if (!projectConfig.hasKeys("remoteRepoUrl")) {
            throw new InvalidConfigException("Missing 'remoteRepoUrl' field");
        }

        if (!projectConfig.hasKey("compiler.version")) {
            LOGGER.warn("Missing 'compiler.version' field, defaulting to '21'");
            projectConfig.setString("compiler.version", "21");
        }

        if (!projectConfig.hasKey("exclude")) {
            LOGGER.warn("Missing 'exclude' field, defaulting to empty list");
            projectConfig.setList("exclude", List.of());
        }

        if (!projectConfig.hasKey("replaceVarsIn")) {
            LOGGER.warn("Missing 'replaceVarsIn' field, defaulting to empty list");
            projectConfig.setList("replaceVarsIn", List.of());
        }

        if (!projectConfig.hasKeys("project.name", "project.version", "project.group")) {
            throw new InvalidConfigException("Missing one of the required project fields: 'name', 'group' or 'version'");
        }

        if (!projectConfig.hasKey("project.entry")) {
            LOGGER.warn("Missing 'project.entry' field, defaulting to empty string (no entry point)");
            projectConfig.setString("project.entry", "");
        }

        if (!projectConfig.hasKey("project.dependencies")) {
            LOGGER.warn("Missing 'project.dependencies' field, defaulting to empty list");
            projectConfig.setList("project.dependencies", List.of());
        }

        final List<Object> dependencies = projectConfig.getList("project.dependencies");

        if (dependencies == null) {
            throw new InvalidConfigException("'project.dependencies' must be a list");
        }

        for (Object dependency : dependencies) {
            if (!(dependency instanceof NewJson)) {
                throw new InvalidConfigException("Each dependency must be a JSON object");
            }

            if (!((NewJson) dependency).hasKeys("name", "version", "group")) {
                throw new InvalidConfigException("Each dependency must have 'name', 'version', and 'group' fields");
            }
        }

        return projectConfig;
    }
}
