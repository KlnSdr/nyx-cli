package nyx.config;

import dobby.util.json.NewJson;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectConfig {
    private final String compilerVersion;
    private final String remoteRepoUrl;
    private final String projectGroup;
    private final String projectName;
    private String projectVersion;
    private final String entryPoint;
    private final List<Dependency> dependencies;
    private final List<String> exclude;

    public ProjectConfig(String compilerVersion, String remoteRepoUrl, String projectGroup, String projectName, String projectVersion, String entryPoint, List<Dependency> dependencies, List<String> exclude) {
        this.compilerVersion = compilerVersion;
        this.remoteRepoUrl = remoteRepoUrl;
        this.projectGroup = projectGroup;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.entryPoint = entryPoint;
        this.dependencies = dependencies;
        this.exclude = exclude;
    }

    public static ProjectConfig fromJson(NewJson json) {
        return new ProjectConfig(
                json.getString("compiler.version"),
                json.getString("remoteRepoUrl"),
                json.getString("project.group"),
                json.getString("project.name"),
                json.getString("project.version"),
                json.getString("project.entry"),
                json.getList("project.dependencies").stream().map(o -> (NewJson) o).map(Dependency::fromJson).collect(Collectors.toList()),
                json.getList("exclude").stream().map(Object::toString).collect(Collectors.toList())
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

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public List<String> getExclude() {
        return exclude;
    }

    public NewJson toJson() {
        final NewJson config = new NewJson();

        final NewJson compiler = new NewJson();
        compiler.setString("version", compilerVersion);

        final NewJson project = new NewJson();
        project.setString("group", projectGroup);
        project.setString("name", projectName);
        project.setString("version", projectVersion);
        project.setString("entry", entryPoint);
        project.setList("dependencies", dependencies.stream().map(Dependency::toJson).collect(Collectors.toList()));

        config.setJson("compiler", compiler);
        config.setString("remoteRepoUrl", remoteRepoUrl);
        config.setJson("project", project);
        config.setList("exclude", exclude.stream().map(o -> (Object) o).collect(Collectors.toList()));
        return config;
    }
}
