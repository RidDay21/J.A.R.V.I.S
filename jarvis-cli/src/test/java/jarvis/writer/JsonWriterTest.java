package jarvis.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jarvis.model.ApiMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Спецификация записи JSON (JsonWriter).
 * Проверяет не только наличие файла, но и корректность его структуры.
 */
@DisplayName("Спецификация записи JSON (JsonWriter)")
class JsonWriterTest {

    private JsonWriter jsonWriter;
    private ObjectMapper objectMapper;
    private ApiMetadata testMetadata;

    @BeforeEach
    void setUp() {
        jsonWriter = new JsonWriter();
        objectMapper = new ObjectMapper();
        testMetadata = new ApiMetadata("J.A.R.V.I.S. API", "1.0.0");
    }

    @Test
    @DisplayName("Должен записывать JSON с корректными данными (Title и Version)")
    void shouldWriteCorrectJsonStructure(@TempDir Path tempDir) throws IOException {
        String outputDir = tempDir.toString();
        File expectedFile = new File(outputDir, "api-data.json");

        jsonWriter.writeJson(testMetadata, outputDir);

        // Проверяем содержимое)
        assertThat(expectedFile).exists();

        // Читаем файл как JSON-дерево для проверки полей
        JsonNode rootNode = objectMapper.readTree(expectedFile);

        assertThat(rootNode.get("title").asText())
                .as("Поле 'title' в JSON должно совпадать с метаданными")
                .isEqualTo("J.A.R.V.I.S. API");

        assertThat(rootNode.get("version").asText())
                .as("Поле 'version' в JSON должно совпадать с метаданными")
                .isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Должен создавать подпапки, если указан глубокий путь")
    void shouldCreateNestedDirectories(@TempDir Path tempDir) {
        Path deepPath = tempDir.resolve("reports/api/v1");

        jsonWriter.writeJson(testMetadata, deepPath.toString());

        assertThat(deepPath.toFile()).exists().isDirectory();
        assertThat(new File(deepPath.toFile(), "api-data.json")).exists();
    }
}