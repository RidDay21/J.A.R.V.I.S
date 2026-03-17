package jarvis.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jarvis.model.ApiMetadata;
import java.io.File;
import java.io.IOException;

public class JsonWriter {
    public void writeJson(ApiMetadata metadata, String outputDirPath) {
        File outputDir = new File(outputDirPath);

        // Создаем папку, если её нет
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(outputDir, "api-data.json");

        // Настраиваем Jackson для красивого форматирования JSON (с отступами)
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            mapper.writeValue(outputFile, metadata);
            System.out.println("JSON успешно сгенерирован: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при записи JSON: " + e.getMessage());
        }
    }
}
