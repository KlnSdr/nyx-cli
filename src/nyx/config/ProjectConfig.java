package nyx.config;

import dobby.util.json.NewJson;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectConfig {
    private final String compilerVersion;
    private final String remoteRepoUrl;
    private final String projectGroup;
    private final String projectName;
    private final String projectVersion;
    private final String entryPoint;
    private final List<Dependency> dependencies;

    public ProjectConfig(String compilerVersion, String remoteRepoUrl, String projectGroup, String projectName, String projectVersion, String entryPoint, List<Dependency> dependencies) {
        this.compilerVersion = compilerVersion;
        this.remoteRepoUrl = remoteRepoUrl;
        this.projectGroup = projectGroup;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.entryPoint = entryPoint;
        this.dependencies = dependencies;
    }

    public static ProjectConfig fromJson(NewJson json) {
        return new ProjectConfig(
                json.getString("compiler.version"),
                json.getString("remoteRepoUrl"),
                json.getString("project.group"),
                json.getString("project.name"),
                json.getString("project.version"),
                json.getString("project.entry"),
                json.getList("project.dependencies").stream().map(o -> (NewJson) o).map(Dependency::fromJson).collect(Collectors.toList())
        );
    }

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public String getRemoteRepoUrl() {
        return remoteRepoUrl;
    }

    public String getProjectGroup() {
        return projectGroup;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }
}
