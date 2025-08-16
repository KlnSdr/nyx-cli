package nyx.goals;

import common.logger.Logger;
import nyx.config.ProjectConfig;
import nyx.settings.Settings;
import nyx.util.ProjectHelper;
import nyx.util.SettingsHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static nyx.util.ProjectHelper.getProjectDir;

public class PushGoal implements Goal {
    private static final Logger LOGGER = new Logger(PushGoal.class, true);
    private static final String LINE_FEED = "\r\n";

    @Override
    public String getHelp() {
        return "Pushes the artifact to the remote repository";
    }

    @Override
    public String getName() {
        return "push";
    }

    @Override
    public GoalResult execute() {
        final ProjectConfig config = ProjectHelper.getProjectConfig();

        if (config == null) {
            LOGGER.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        final Settings settings = SettingsHelper.getSettings();
        if (settings == null) {
            LOGGER.error("Failed to read settings");
            return GoalResult.FAILURE;
        }

        final String repoUrl = config.getRemoteRepoUrl();
        final String token = settings.getToken(repoUrl);

        final String projectDir = getProjectDir();
        final File artifactFile = new File(projectDir + "/build/" + config.getProjectName() + "-" + config.getProjectVersion() + ".jar");
        final File nyxJsonFile = new File(projectDir + "/nyx.json");

        if (!artifactFile.exists()) {
            LOGGER.warn(artifactFile.getAbsolutePath() + " does not exist. Attempting to build the project first.");
            final GoalResult buildResult = new BuildGoal().execute();
            if (buildResult == GoalResult.FAILURE) {
                LOGGER.error("Failed to build the project. Please run 'build' goal first.");
                return GoalResult.FAILURE;
            }
        }

        final String boundary = Long.toHexString(System.currentTimeMillis()) + UUID.randomUUID().toString().replace("-", "");

        final String targetUrl = repoUrl + "/rest/upload";

        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        PrintWriter writer = null;
        try {
            connection = (HttpURLConnection) new URL(targetUrl).openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            if (token != null) {
                connection.setRequestProperty("Hades-Login-Token", token);
            }

            outputStream = connection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            addFormField(writer, "group", config.getProjectGroup(), boundary);
            addFormField(writer, "name", config.getProjectName(), boundary);
            addFormField(writer, "version", config.getProjectVersion(), boundary);

            attachFile(writer, outputStream, "artifact", artifactFile, boundary);
            attachFile(writer, outputStream, "nyxjson", nyxJsonFile, boundary);

            writer.append("--").append(boundary).append("--").append(LINE_FEED);
            writer.flush();
            writer.close();

            final int responseCode = connection.getResponseCode();
            LOGGER.debug("Response Code: " + responseCode);

            if (responseCode == 200) {
                LOGGER.info("Artifact pushed successfully");
            } else {
                LOGGER.error("Failed to push artifact: " + responseCode);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.error(line);
                    }
                }
                return GoalResult.FAILURE;
            }

            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close output stream");
                    LOGGER.trace(e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return GoalResult.SUCCESS;
    }

    private void attachFile(PrintWriter writer, OutputStream outputStream, String fieldName, File file, String boundary) throws IOException {
        final String fileName = file.getName();
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
        writer.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        writer.append(LINE_FEED).flush();

        final FileInputStream inputStream = new FileInputStream(file);
        final byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append(LINE_FEED).flush();
    }

    private void addFormField(PrintWriter writer, String name, String value, String boundary) {
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
}
