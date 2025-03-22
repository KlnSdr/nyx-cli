package nyx.goals;

import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import nyx.util.ProjectHelper;
import nyx.util.RepoHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static nyx.util.FileHelper.createDirIfNotExists;

public class InstallGoal implements Goal {
    private static final Logger LOGGER = new Logger(InstallGoal.class, true);

    @Override
    public String getHelp() {
        return "Installs the project JAR file to the local repository. Runs build goal before installing.";
    }

    @Override
    public String getName() {
        return "install";
    }

    @Override
    public GoalResult execute() {
        if (new BuildGoal().execute() == GoalResult.FAILURE) {
            return GoalResult.FAILURE;
        }

        final NewJson config = ProjectHelper.getProjectConfig();

        if (config == null) {
            LOGGER.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        final String jarName = config.getString("project.name") + "-" + config.getString("project.version") + ".jar";
        final File jarFile = new File(ProjectHelper.getProjectDir() + "/build/" + jarName);

        final String repoDir = RepoHelper.getRepoDir();

        createDirIfNotExists(repoDir + "/" + config.getString("project.group"));
        createDirIfNotExists(repoDir + "/" + config.getString("project.group") + "/" + config.getString("project.name"));
        createDirIfNotExists(repoDir + "/" + config.getString("project.group") + "/" + config.getString("project.name") + "/" + config.getString("project.version"));

        final File repoFile = new File(repoDir + "/" + config.getString("project.group") + "/" + config.getString("project.name") + "/" + config.getString("project.version") + "/" + jarName);

        try {
            Files.copy(
                    jarFile.toPath(),
                    repoFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to install jar file to local repository");
            LOGGER.trace(e);
            return GoalResult.FAILURE;
        }

        return GoalResult.SUCCESS;
    }
}
