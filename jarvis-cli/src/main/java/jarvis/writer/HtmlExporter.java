package jarvis.writer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Экспортирует статические файлы UI (HTML, CSS, JS) в целевую директорию.
 */
public class HtmlExporter {
    /**
     * Копирует шаблоны интерфейса из ресурсов в указанный путь.
     * @param outputDirPath путь к папке назначения.
     */
    public void exportTemplates(String outputDirPath) {
        // Список файлов, которые нужно скопировать из ресурсов
        String[] templateFiles = {"index.html", "app.js", "style.css"};

        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (String fileName : templateFiles) {
            // Читаем файл из папки src/main/resources/template/
            String resourcePath = "/template/" + fileName;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("Предупреждение: Файл шаблона не найден в ресурсах: " + resourcePath);
                    // Создадим пустой файл, чтобы не ломать логику, если файла пока нет
                    new File(outputDir, fileName).createNewFile();
                    continue;
                }

                Path targetPath = new File(outputDir, fileName).toPath();
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);

            } catch (IOException e) {
                System.err.println("Ошибка при копировании файла " + fileName + ": " + e.getMessage());
            }
        }
        System.out.println("Файлы UI успешно скопированы в: " + outputDir.getAbsolutePath());
    }
}
