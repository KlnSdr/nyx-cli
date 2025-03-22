package nyx.goals;

import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import nyx.util.ProjectHelper;
import nyx.util.RepoHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static nyx.util.FileHelper.createDirIfNotExists;
import static nyx.util.ProjectHelper.getProjectDir;

public class BuildGoal implements Goal {
    private static final Logger LOGGER = new Logger(BuildGoal.class, true);

    @Override
    public GoalResult execute() {
        LOGGER.info("Building project...");

        if (new SyncGoal().execute() == GoalResult.FAILURE) {
            LOGGER.error("Failed to sync dependencies");
            return GoalResult.FAILURE;
        }

        if (new CleanGoal().execute() == GoalResult.FAILURE) {
            LOGGER.error("Failed to clean project");
            return GoalResult.FAILURE;
        }

        final String projectDir = getProjectDir();
        final String buildDir = projectDir + "/build";

        createDirIfNotExists(buildDir);

        final NewJson config = ProjectHelper.getProjectConfig();

        if (config == null) {
            LOGGER.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        final String classPath = buildClassPath(config, projectDir + "/src");

        final String buildCommand = String.format(
                "javac -cp %s -d %s --release %s %s",
                classPath,
                buildDir + "/classes",
                config.getString("compiler.version"), projectDir + "/src/" + config.getString("project.entry").replace(".", "/") + ".java");

        LOGGER.info("compiling class files...");
        LOGGER.debug("Executing command: " + buildCommand);

        if (!executeCommand(buildCommand)) {
            LOGGER.error("Failed to compile class files");
            return GoalResult.FAILURE;
        }

        final String copyCommand = String.format(
                "cd %s && find . -type f ! -name '*.java' -exec cp --parents {} %s \\;",
                projectDir + "/src", buildDir + "/classes"
        );

        LOGGER.info("copying resources...");
        LOGGER.debug("Executing command: " + copyCommand);

        if (!executeCommand(copyCommand)) {
            LOGGER.error("Failed to copy resources");
            return GoalResult.FAILURE;
        }

        LOGGER.info("Extracting JAR dependencies...");
        final String[] jarDependencies = classPath.split(":");
        for (String jar : jarDependencies) {
            if (jar.endsWith(".jar")) {
                LOGGER.info("Extracting JAR: " + jar);
                String extractCommand = String.format("unzip -o %s -d %s", jar, buildDir + "/classes");
                LOGGER.debug("Executing command: " + extractCommand);
                if (!executeCommand(extractCommand)) {
                    LOGGER.error("Failed to extract JAR: " + jar);
                    return GoalResult.FAILURE;
                }
            }
        }

        final String jarCommand = String.format(
                "jar --create --file %s/%s-%s.jar --main-class=%s -C %s/classes .",
                buildDir,
                config.getString("project.name"),
                config.getString("project.version"),
                config.getString("project.entry"),
                buildDir);

        LOGGER.info("creating jar file...");
        LOGGER.debug("Executing command: " + jarCommand);

        if (!executeCommand(jarCommand)) {
            LOGGER.error("Failed to create jar file");
            return GoalResult.FAILURE;
        }

        return GoalResult.SUCCESS;
    }

    private boolean executeCommand(String command) {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.redirectErrorStream(true);
            final Process process = processBuilder.start();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.error("Failed to execute command \"" + command + "\". Exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error executing command: " + command);
            LOGGER.trace(e);
            return false;
        }

        return true;
    }

    private String buildClassPath(NewJson config, String projectRoot) {
        final StringBuilder classPath = new StringBuilder();

        final String repoDir = RepoHelper.getRepoDir();
        final List<NewJson> dependencies = config.getList("project.dependencies").stream().map(o -> (NewJson) o).collect(Collectors.toList());

        for (NewJson dependency : dependencies) {
            final String group = dependency.getString("group");
            final String name = dependency.getString("name");
            final String version = dependency.getString("version");
            classPath
                    .append(repoDir)
                    .append("/")
                    .append(group)
                    .append("/")
                    .append(name)
                    .append("/")
                    .append(version)
                    .append("/")
                    .append(name)
                    .append("-")
                    .append(version)
                    .append(".jar")
                    .append(":");
        }

        classPath.append(projectRoot);

        return classPath.toString();
    }
}
