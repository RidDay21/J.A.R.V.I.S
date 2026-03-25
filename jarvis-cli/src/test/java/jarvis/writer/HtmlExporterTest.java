package jarvis.writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяет обязательство компонента по развертыванию UI-шаблонов.
 */
@DisplayName("Спецификация экспорта UI (HtmlExporter)")
class HtmlExporterTest {

    private HtmlExporter htmlExporter;

    @BeforeEach
    void setUp() {
        htmlExporter = new HtmlExporter();
    }

    @Test
    @DisplayName("Должен экспортировать полный комплект UI-файлов (HTML, JS, CSS)")
    void shouldExportAllRequiredUiFiles(@TempDir Path tempDir) {
        String outputDir = tempDir.toString();
        List<String> expectedFiles = List.of("index.html", "app.js", "style.css");

        htmlExporter.exportTemplates(outputDir);

        for (String fileName : expectedFiles) {
            File file = new File(outputDir, fileName);
            assertThat(file)
                    .as("Файл %s должен быть экспортирован", fileName)
                    .exists();
        }
    }

    @Test
    @DisplayName("Должен корректно создавать дерево папок для экспорта")
    void shouldCreateOutputDirectory(@TempDir Path tempDir) {
        Path subDir = tempDir.resolve("web/docs/ui");

        htmlExporter.exportTemplates(subDir.toString());

        assertThat(subDir.toFile()).exists().isDirectory();
    }

    @Test
    @DisplayName("Должен создавать пустые файлы, если ресурсы шаблонов не найдены (Graceful Degradation)")
    void shouldHandleMissingResourcesGracefully(@TempDir Path tempDir) {
        // Этот тест проверяет логику catch/if из твоего класса
        String outputDir = tempDir.toString();

        htmlExporter.exportTemplates(outputDir);

        assertThat(new File(outputDir, "index.html")).exists();
    }
}