package jarvis.analyzer;
import jarvis.model.Endpoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест для класса {@link JarScanner}.
 * Проверяет способность утилиты находить Spring-контроллеры внутри скомпилированного JAR-файла.
 */
class JarScannerTest {

    @Test
    @DisplayName("Должен корректно извлекать Base URL из application.properties в JAR-файле")
    void shouldExtractBaseUrlFromJarConfig(@TempDir Path tempDir) throws Exception {
        // 1. Создаем временный JAR-файл с конфигом
        Path jarPath = tempDir.resolve("test-service.jar");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarPath.toFile()))) {
            ZipEntry entry = new ZipEntry("application.properties");
            zos.putNextEntry(entry);
            String content = "server.port=9090\nserver.servlet.context-path=/api/v2";
            zos.write(content.getBytes());
            zos.closeEntry();
        }

        // 2. Инициализируем сканер
        TypeAnalyzer analyzer = new TypeAnalyzer(new HashMap<>());
        JarScanner scanner = new JarScanner(analyzer);

        // 3. Вызываем метод
        String baseUrl = scanner.extractBaseUrl(jarPath.toString());

        // 4. Проверяем результат
        assertThat(baseUrl).isEqualTo("http://localhost:9090/api/v2");
    }

    @Test
    @DisplayName("Должен возвращать значение по умолчанию, если конфигов нет")
    void shouldReturnDefaultUrlIfNoConfigs(@TempDir Path tempDir) throws Exception {
        Path jarPath = tempDir.resolve("empty.jar");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarPath.toFile()))) {
            ZipEntry entry = new ZipEntry("dummy.txt");
            zos.putNextEntry(entry);
            zos.write("hello".getBytes());
            zos.closeEntry();
        }

        JarScanner scanner = new JarScanner(new TypeAnalyzer(new HashMap<>()));
        String baseUrl = scanner.extractBaseUrl(jarPath.toString());

        assertThat(baseUrl).isEqualTo("http://localhost:8080");
    }

    /**
     * Проверяет сканирование тестового JAR-файла.
...
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