package jarvis.model;

import java.util.ArrayList;
import java.util.List;

public class Endpoint {
    public String path;
    public String method;
    public String summary;

    public List<Parameter> parameters = new ArrayList<>();

    // Сюда будем класть структуру @RequestBody, если она есть
    public Schema requestBody;
    public Schema responseBody;

    public Endpoint(String path, String method) {
        this.path = path;
        this.method = method;
        this.summary = "Описание метода отсутствует";
    }

    @Override
    public String toString() {
        return method + " " + path;
    }
}
