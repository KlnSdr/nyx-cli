package nyx.goals;

import common.logger.Logger;
import nyx.config.Dependency;
import nyx.config.ProjectConfig;
import nyx.util.ProjectHelper;
import nyx.util.RepoHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final Long startTime = System.currentTimeMillis();
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

        LOGGER.info("Building project: " + config.getProjectName() + ":" + config.getProjectVersion());
        final boolean didCopySrc = copySrcToBuildAndReplaceVariables(config);
        if (!didCopySrc) {
            LOGGER.error("Failed to copy source files to build directory");
            return GoalResult.FAILURE;
        }

        final String classPath = buildClassPath(config, projectDir + "/build/src");

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

        final String buildCommand = String.format(
                "javac -cp %s -d %s --release %s @sources.list",
                classPath,
                buildDir + "/classes",
                config.getCompilerVersion());

        LOGGER.info("compiling class files...");

        LOGGER.debug("Finding source files...");

        final List<String> sourceFiles = findSourceFiles();

        if (sourceFiles == null) {
            LOGGER.error("Failed to find source files");
            return GoalResult.FAILURE;
        }

        for (String sourceFile : sourceFiles) {
            LOGGER.debug("Compiling: " + sourceFile);
        }

        try {
            Files.write(Paths.get("sources.list"), sourceFiles);
        } catch (IOException e) {
            LOGGER.error("Failed to write source files to sources.list");
            LOGGER.trace(e);
            return GoalResult.FAILURE;
        }

        LOGGER.debug("Executing command: " + buildCommand);
        if (!executeCommand(buildCommand)) {
            LOGGER.error("Failed to compile class files");
            return GoalResult.FAILURE;
        }

        final boolean didDeleteSourcesList = new File("sources.list").delete();

        if (!didDeleteSourcesList) {
            LOGGER.error("Failed to delete sources.list");
            return GoalResult.FAILURE;
        }

        LOGGER.info("copying resources...");

        if (!copyResources(buildDir + "/src", buildDir + "/classes", config.getExclude())) {
            LOGGER.error("Failed to copy resources");
            return GoalResult.FAILURE;
        }

        final String jarCommand = config.getEntryPoint().isEmpty()
                ? String.format(
                    "jar --create --file %s/%s-%s.jar -C %s/classes .",
                    buildDir,
                    config.getProjectName(),
                    config.getProjectVersion(),
                    buildDir)
                : String.format(
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

        final Long endTime = System.currentTimeMillis();
        final long duration = endTime - startTime;
        LOGGER.info("Build completed in " + duration + " ms");
        return GoalResult.SUCCESS;
    }

    private List<String> findSourceFiles() {
        String srcDir = "build/src";
        List<String> javaFiles;
        try {
            javaFiles = Files.walk(Paths.get(srcDir))
                    .map(Path::toString)
                    .filter(string -> string.endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Failed to find source files");
            LOGGER.trace(e);
            return null;
        }

        return javaFiles;
    }

    private boolean copyResources(String projectDir, String buildDir, List<String> exclude) {
        LOGGER.debug("Copying resources from " + projectDir + " to " + buildDir);
        final File projectDirFile = new File(projectDir);
        final File buildDirFile = new File(buildDir);

        if (!projectDirFile.exists()) {
            LOGGER.error("Project directory does not exist: " + projectDir);
            return false;
        }

        if (!buildDirFile.exists()) {
            LOGGER.error("Build directory does not exist: " + buildDir);
            return false;
        }

        final File[] files = projectDirFile.listFiles();

        if (files == null) {
            LOGGER.error("Failed to list files in project directory: " + projectDir);
            return false;
        }

        for (File file : files) {
            final String fileName = file.getName();
            final String destPath = buildDir + "/" + fileName;

            if (exclude.contains(fileName) || fileName.endsWith(".java")) {
                LOGGER.debug("Excluding file: " + fileName);
                continue;
            }

            if (file.isDirectory()) {
                createDirIfNotExists(destPath);
                if (!copyResources(file.getAbsolutePath(), destPath, exclude)) {
                    return false;
                }
            } else {
                try {
                    Files.copy(file.toPath(), new File(destPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.error("Failed to copy file: " + file.getAbsolutePath());
                    LOGGER.trace(e);
                    return false;
                }
            }
        }

        return true;
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

    private boolean copySrcToBuildAndReplaceVariables(ProjectConfig config) {
        final String projectDir = getProjectDir();
        final String srcDir = projectDir + "/src";
        final String buildSrcDir = projectDir + "/build/src";

        createDirIfNotExists(buildSrcDir);

        LOGGER.info("Copying source files from " + srcDir + " to " + buildSrcDir);

        final File srcFile = new File(srcDir);
        final File buildSrcFile = new File(buildSrcDir);

        if (!srcFile.exists()) {
            LOGGER.error("Source directory does not exist: " + srcDir);
            return false;
        }

        if (!buildSrcFile.exists()) {
            LOGGER.error("Build source directory does not exist: " + buildSrcDir);
            return false;
        }

        try (final Stream<Path> paths = Files.walk(srcFile.toPath())) {
            paths
                .forEach(source -> {
                    final Path destination = buildSrcFile.toPath().resolve(srcFile.toPath().relativize(source));
                    try {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        if (Files.isDirectory(destination)) {
                            return;
                        }
                        final boolean didReplaceVariables = replaceVariablesInFile(destination, config);
                        if (!didReplaceVariables) {
                            LOGGER.error("Failed to replace variables in file: " + destination);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to copy source file: " + source);
                        LOGGER.trace(e);
                    }
                });
        } catch (IOException e) {
            LOGGER.error("Failed to copy source files");
            LOGGER.trace(e);
            return false;
        }

        return true;
    }

    private boolean replaceVariablesInFile(Path filePath, ProjectConfig config) {
        try {
            String content = new String(Files.readAllBytes(filePath));
            content = content.replace("${PROJECT_VERSION}", config.getProjectVersion());
            Files.write(filePath, content.getBytes());
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to replace variables in file: " + filePath);
            LOGGER.trace(e);
            return false;
        }
    }
}
