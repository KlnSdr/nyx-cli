package nyx.util;

import common.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class Git {
    private static final Logger LOGGER = new Logger(Git.class, true);
    private final List<String> commands = new ArrayList<>();

    public Git add(String filePath) {
        commands.add("git add " + filePath);
        return this;
    }

    public Git commit(String message) {
        commands.add("git commit -m \"" + message + "\"");
        return this;
    }

    public Git tag(String tagName) {
        commands.add("git tag " + tagName);
        return this;
    }

    public boolean execute() {
        for (String command : commands) {
            final boolean success = ProcessHelper.executeCommand(command, false);

            if (!success) {
                return false;
            }
        }
        return true;
    }
}
