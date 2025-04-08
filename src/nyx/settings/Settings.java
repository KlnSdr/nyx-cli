package nyx.settings;

import dobby.util.json.NewJson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Settings {
    private final Map<String, String> repos = new HashMap<>();

    public static Settings fromJson(NewJson json) {
        final Settings settings = new Settings();
        final List<NewJson> repos = json.getList("repos").stream().map(o -> (NewJson) o).collect(Collectors.toList());
        for (NewJson repo : repos) {
            final String url = repo.getString("url");
            final String token = repo.getString("token");
            settings.addRepo(url, token);
        }

        return settings;
    }

    public NewJson toJson() {
        final NewJson json = new NewJson();

        final List<NewJson> repos = this.repos.entrySet().stream().map(e -> {
            final NewJson repo = new NewJson();
            repo.setString("url", e.getKey());
            repo.setString("token", e.getValue());
            return repo;
        }).collect(Collectors.toList());

        json.setList("repos", repos.stream().map(o -> (Object) o).collect(Collectors.toList()));

        return json;
    }

    public void addRepo(String url, String token) {
        repos.put(url, token);
    }

    public String getToken(String url) {
        return repos.get(url);
    }
}
