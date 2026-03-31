package jarvis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiMetadata {
    public String apiName;
    public String version;
    public String baseUrl = "http://localhost:8080";
    public List<Endpoint> endpoints = new ArrayList<>();
    public Map<String, Schema> schemas = new HashMap<>();

    public ApiMetadata(String apiName, String version) {
        this.apiName = apiName;
        this.version = version;
    }

    public ApiMetadata(String apiName, String version, String baseUrl) {
        this.apiName = apiName;
        this.version = version;
        this.baseUrl = baseUrl;
    }
}