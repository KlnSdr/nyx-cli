package nyx.goals;

import dobby.util.logging.Logger;
import nyx.config.Dependency;
import nyx.config.ProjectConfig;
import nyx.util.ProjectHelper;
import nyx.util.RepoHelper;

import java.util.List;

import static nyx.util.FileHelper.createDirIfNotExists;
import static nyx.util.ProcessHelper.executeCommand;
import static nyx.util.ProjectHelper.getProjectDir;

public class BuildGoal implements Goal {
    private static final Logger LOGGER = new Logger(BuildGoal.class, true);

    @Override
    public String getHelp() {
        return "Compiles the project and creates a JAR file. Runs sync and clean goals before building.";
    }

    @Override
    public String getName() {
        return "build";
    }

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

        final ProjectConfig config = ProjectHelper.getProjectConfig();

        if (config == null) {
            LOGGER.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        final String classPath = buildClassPath(config, projectDir + "/src");

        final String buildCommand = String.format(
                "javac -cp %s -d %s --release %s %s",
                classPath,
                buildDir + "/classes",
                config.getCompilerVersion(), projectDir + "/src/" + config.getEntryPoint().replace(".", "/") + ".java");

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
                config.getProjectName(),
                config.getProjectVersion(),
                config.getEntryPoint(),
                buildDir);

        LOGGER.info("creating jar file...");
        LOGGER.debug("Executing command: " + jarCommand);

        if (!executeCommand(jarCommand)) {
            LOGGER.error("Failed to create jar file");
            return GoalResult.FAILURE;
        }

        return GoalResult.SUCCESS;
    }

    private String buildClassPath(ProjectConfig config, String projectRoot) {
        final StringBuilder classPath = new StringBuilder();

        final String repoDir = RepoHelper.getRepoDir();
        final List<Dependency> dependencies = config.getDependencies();

        for (Dependency dependency : dependencies) {
            final String group = dependency.getGroup();
            final String name = dependency.getName();
            final String version = dependency.getVersion();
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
