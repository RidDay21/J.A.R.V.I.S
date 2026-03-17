package jarvis.analyzer;

import jarvis.model.Endpoint;
import jarvis.model.Schema;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;



public class ControllerParser {

    private final TypeAnalyzer typeAnalyzer;

    public ControllerParser(TypeAnalyzer typeAnalyzer) {
        this.typeAnalyzer = typeAnalyzer;
    }

    public List<Endpoint> parse(Class<?> controllerClass) {
        List<Endpoint> endpoints = new ArrayList<>();

        String basePath = "";
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping reqMapping = controllerClass.getAnnotation(RequestMapping.class);
            if (reqMapping.value().length > 0) {
                basePath = reqMapping.value()[0];

            }
        }

        for (Method method : controllerClass.getDeclaredMethods()) {
            Endpoint endpoint = extractEndpoint(method, basePath);
            if (endpoint != null) {
                // Если метод оказался REST-эндпоинтом, парсим его параметры
                extractParameters(method, endpoint);
                endpoints.add(endpoint);
            }
        }

        return endpoints;
    }

    private Endpoint extractEndpoint(Method method, String basePath) {
        String path = "";
        String httpMethod = "";

        if (method.isAnnotationPresent(GetMapping.class)) {
            path = getPath(method.getAnnotation(GetMapping.class).value());
            httpMethod = "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            path = getPath(method.getAnnotation(PostMapping.class).value());
            httpMethod = "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            path = getPath(method.getAnnotation(PutMapping.class).value());
            httpMethod = "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            path = getPath(method.getAnnotation(DeleteMapping.class).value());
            httpMethod = "DELETE";
        } else {
            return null;
        }

        String fullPath = normalizePath(basePath + path);
        return new Endpoint(fullPath, httpMethod);
    }

    // Вспомогательный метод для безопасного извлечения пути из массива
    private String getPath(String[] values) {
        return values.length > 0 ? values[0] : "";
    }

    private String normalizePath(String path) {
        return path.replaceAll("//+", "/");
    }

    // НОВАЯ ЛОГИКА: Парсинг параметров
    private void extractParameters(Method method, Endpoint endpoint) {
        for (Parameter param : method.getParameters()) {

            // 1. Ищем @PathVariable (например, /users/{id})
            if (param.isAnnotationPresent(PathVariable.class)) {
                PathVariable ann = param.getAnnotation(PathVariable.class);
                String name = resolveParamName(ann.value(), ann.name(), param.getName());
                endpoint.parameters.add(new jarvis.model.Parameter(name, "path", param.getType().getSimpleName(), ann.required()));
            }

            // 2. Ищем @RequestParam (например, ?query=test)
            else if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam ann = param.getAnnotation(RequestParam.class);
                String name = resolveParamName(ann.value(), ann.name(), param.getName());
                endpoint.parameters.add(new jarvis.model.Parameter(name, "query", param.getType().getSimpleName(), ann.required()));
            }

            // 3. Ищем @RequestHeader (заголовки)
            else if (param.isAnnotationPresent(RequestHeader.class)) {
                RequestHeader ann = param.getAnnotation(RequestHeader.class);
                String name = resolveParamName(ann.value(), ann.name(), param.getName());
                endpoint.parameters.add(new jarvis.model.Parameter(name, "header", param.getType().getSimpleName(), ann.required()));
            }

            // 4. Ищем @RequestBody (JSON, который шлют в теле запроса)
            else if (param.isAnnotationPresent(RequestBody.class)) {
                Schema bodySchema = typeAnalyzer.analyze(param.getType());
                // Сохраняем ссылку на схему в эндпоинт
                endpoint.requestBody = new Schema(bodySchema.type);
            }
        }
    }

    // Вспомогательный метод для определения имени параметра
    private String resolveParamName(String value, String name, String fallback) {
        if (!value.isEmpty()) return value;
        if (!name.isEmpty()) return name;
        return fallback;
    }
}