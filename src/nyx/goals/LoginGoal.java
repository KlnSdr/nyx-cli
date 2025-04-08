package nyx.goals;

import common.logger.Logger;
import nyx.settings.Settings;
import nyx.util.SettingsHelper;

import java.util.Scanner;

public class LoginGoal implements Goal {
    private static final Logger LOGGER = new Logger(LoginGoal.class, true);

    @Override
    public String getHelp() {
        return "Authenticates the user with the remote repository";
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public GoalResult execute() {
        final Scanner scanner = new Scanner(System.in);

        LOGGER.info("Enter the remote repository URL: ");
        final String repoUrl = scanner.nextLine();

        LOGGER.info("Enter your login token: ");
        final String token = scanner.nextLine();

        scanner.close();

        Settings settings = SettingsHelper.getSettings();
        if (settings == null) {
            settings = new Settings();
        }

        settings.addRepo(repoUrl, token);

        if (!SettingsHelper.writeSettings(settings)) {
            LOGGER.error("Failed to save settings");
            return GoalResult.FAILURE;
        }

        LOGGER.info("credentials saved to ~/.nyx/settings.json");
        return GoalResult.SUCCESS;
    }
}
