package nyx.config;

import dobby.util.json.NewJson;

public class Dependency {
    private final String group;
    private final String name;
    private final String version;

    public Dependency(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    public static Dependency fromJson(NewJson json) {
        return new Dependency(
                json.getString("group"),
                json.getString("name"),
                json.getString("version"));
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public NewJson toJson() {
        NewJson json = new NewJson();
        json.setString("group", group);
        json.setString("name", name);
        json.setString("version", version);
        return json;
    }
}
