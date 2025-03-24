package nyx.util;

import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessHelper {
    private static final Logger LOGGER = new Logger(ProcessHelper.class, true);

    public static boolean executeCommand(String command) {
        return executeCommand(command, false);
    }

    public static boolean executeCommand(String command, boolean writeToStdout) {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.redirectErrorStream(true);
            final Process process = processBuilder.start();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            final List<String> outBuffer = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (writeToStdout) {
                    System.out.println(line);
                } else {
                    LOGGER.debug(line);
                    outBuffer.add(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.error("Failed to execute command \"" + command + "\". Exit code: " + exitCode);
                for (String outLine : outBuffer) {
                    LOGGER.error(outLine);
                }
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error executing command: " + command);
            LOGGER.trace(e);
            return false;
        }

        return true;
    }
}
