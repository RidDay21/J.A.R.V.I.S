package jarvis.analyzer;

import jarvis.model.Endpoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест для класса {@link JarScanner}.
 * Проверяет способность утилиты находить Spring-контроллеры внутри скомпилированного JAR-файла.
 */
class JarScannerTest {

    /**
     * Проверяет сканирование тестового JAR-файла.
     * ВНИМАНИЕ: Тест ожидает, что проект spring-test-service собран
     * и JAR-файл находится в папке build/libs/.
     */
    @Test
    @DisplayName("Должен найти контроллеры в тестовом JAR-файле")
    void shouldScanAndFindControllersInJar() {
        String jarPath = "../spring-test-service/build/libs/spring-test-service-0.0.1-SNAPSHOT.jar";

        // Проверяем, существует ли файл, чтобы тест не упал с непонятной ошибкой
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            System.out.println("Пропуск теста: JAR-файл не найден по пути " + jarPath);
            return;
        }

        TypeAnalyzer analyzer = new TypeAnalyzer(new HashMap<>());
        JarScanner scanner = new JarScanner(analyzer);

        List<Endpoint> endpoints = scanner.scanAndParse(jarPath);

        // Проверки
        assertThat(endpoints)
                .withFailMessage("Сканер не нашел ни одного эндпоинта в " + jarPath)
                .isNotEmpty();

        boolean hasOrderPath = endpoints.stream()
                .anyMatch(e -> e.path.contains("/orders"));

        assertThat(hasOrderPath).isTrue();
    }
}