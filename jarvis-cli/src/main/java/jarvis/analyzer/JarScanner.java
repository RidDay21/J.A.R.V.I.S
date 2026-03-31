package jarvis.analyzer;

import io.github.classgraph.*;
import jarvis.model.Endpoint;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class JarScanner {

    private final ControllerParser controllerParser;

    public JarScanner(TypeAnalyzer typeAnalyzer) {
        this.controllerParser = new ControllerParser(typeAnalyzer);
    }

    public String extractBaseUrl(String jarPath) {
        String port = "8080";
        String contextPath = "";

        try (ScanResult scanResult = new ClassGraph()
                .overrideClasspath(jarPath)
                .acceptPathsNonRecursive("") // Ищем только в корне ресурсов
                .scan()) {

            // 1. Ищем application.properties
            ResourceList propertiesResources = scanResult.getResourcesWithLeafName("application.properties");
            if (!propertiesResources.isEmpty()) {
                try (InputStream is = propertiesResources.get(0).open()) {
                    Properties props = new Properties();
                    props.load(is);
                    port = props.getProperty("server.port", port);
                    contextPath = props.getProperty("server.servlet.context-path", contextPath);
                }
            }

            // 2. Ищем application.yml (упрощенный поиск регулярками)
            ResourceList ymlResources = scanResult.getResourcesWithLeafName("application.yml");
            if (!ymlResources.isEmpty()) {
                try (InputStream is = ymlResources.get(0).open();
                     Scanner scanner = new Scanner(is)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.startsWith("port:")) {
                            port = line.split(":")[1].trim();
                        } else if (line.contains("context-path:")) {
                            contextPath = line.split(":")[1].trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Предупреждение: Не удалось прочитать конфиги из JAR: " + e.getMessage());
        }

        // Чистим contextPath от лишних кавычек и слешей
        contextPath = contextPath.replace("\"", "").replace("'", "");
        if (!contextPath.isEmpty() && !contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        return "http://localhost:" + port + contextPath;
    }

    public List<Endpoint> scanAndParse(String jarPath) {
        List<Endpoint> allEndpoints = new ArrayList<>();
        System.out.println("Сканирование JAR: " + jarPath);

        try (ScanResult scanResult = new ClassGraph()
                .overrideClasspath(jarPath)
                .enableAllInfo() // Включаем сбор всей информации о классах
                .scan()) {

            // Ищем все REST-контроллеры
            ClassInfoList restControllers = scanResult.getClassesWithAnnotation("org.springframework.web.bind.annotation.RestController");

            for (ClassInfo classInfo : restControllers) {
                // Загружаем класс в память, чтобы работала Java Reflection
                Class<?> controllerClass = classInfo.loadClass();
                System.out.println("Анализ контроллера: " + controllerClass.getSimpleName());

                // Парсим класс
                List<Endpoint> classEndpoints = controllerParser.parse(controllerClass);
                allEndpoints.addAll(classEndpoints);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при сканировании: " + e.getMessage());
        }

        return allEndpoints;
    }
}