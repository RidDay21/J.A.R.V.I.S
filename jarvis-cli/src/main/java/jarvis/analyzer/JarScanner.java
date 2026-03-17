package jarvis.analyzer;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import jarvis.model.Endpoint;

import java.util.ArrayList;
import java.util.List;

public class JarScanner {

    private final ControllerParser controllerParser;

    public JarScanner(TypeAnalyzer typeAnalyzer) {
        this.controllerParser = new ControllerParser(typeAnalyzer);
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