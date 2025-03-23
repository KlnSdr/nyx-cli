package nyx.goals;

import dobby.util.logging.Logger;
import nyx.config.ProjectConfig;
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

        final ProjectConfig config = ProjectHelper.getProjectConfig();

        if (config == null) {
            LOGGER.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        final String jarName = config.getProjectName()+ "-" + config.getProjectVersion() + ".jar";
        final File jarFile = new File(ProjectHelper.getProjectDir() + "/build/" + jarName);

        final String repoDir = RepoHelper.getRepoDir();

        createDirIfNotExists(repoDir + "/" + config.getProjectGroup());
        createDirIfNotExists(repoDir + "/" + config.getProjectGroup() + "/" + config.getProjectName());
        createDirIfNotExists(repoDir + "/" + config.getProjectGroup() + "/" + config.getProjectName() + "/" + config.getProjectVersion());

        final File repoFile = new File(repoDir + "/" + config.getProjectGroup() + "/" + config.getProjectName() + "/" + config.getProjectVersion() + "/" + jarName);

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
