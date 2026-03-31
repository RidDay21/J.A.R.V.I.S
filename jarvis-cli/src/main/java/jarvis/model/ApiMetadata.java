package jarvis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiMetadata {
    public String title;
    public String version;
    public String baseUrl = "http://localhost:8080";
    public List<Endpoint> endpoints = new ArrayList<>();
    public Map<String, Schema> schemas = new HashMap<>();

    public ApiMetadata(String title, String version) {
        this.title = title;
        this.version = version;
    }

    public ApiMetadata(String title, String version, String baseUrl) {
        this.title = title;
        this.version = version;
        this.baseUrl = baseUrl;
    }
}