package jarvis;

import jarvis.analyzer.JarScanner;
import jarvis.analyzer.TypeAnalyzer;
import jarvis.model.ApiMetadata;
import jarvis.writer.HtmlExporter;
import jarvis.writer.JsonWriter;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Использование: java -jar jarvis.jar <путь_к_jar_сервиса> [выходная_папка]");
            System.exit(1);
        }

        String targetJarPath = args[0];
        // Если выходная папка не указана, создаем папку api-docs-output рядом с генератором
        String outputDir = args.length > 1 ? args[1] : "api-docs-output";

        System.out.println("=== JARVIS API GENERATOR ===");

        // 1. АНАЛИЗ
        ApiMetadata apiMetadata = new ApiMetadata("API Documentation", "1.0.0");
        TypeAnalyzer typeAnalyzer = new TypeAnalyzer(apiMetadata.schemas);
        JarScanner scanner = new JarScanner(typeAnalyzer);

        apiMetadata.endpoints = scanner.scanAndParse(targetJarPath);
        System.out.println("Найдено эндпоинтов: " + apiMetadata.endpoints.size());

        // 2. ГЕНЕРАЦИЯ
        System.out.println("\n=== ГЕНЕРАЦИЯ ДОКУМЕНТАЦИИ ===");

        JsonWriter jsonWriter = new JsonWriter();
        jsonWriter.writeJson(apiMetadata, outputDir);

        HtmlExporter htmlExporter = new HtmlExporter();
        htmlExporter.exportTemplates(outputDir);

        System.out.println("\nГотово! Откройте файл " + outputDir + "/index.html в браузере.");
    }
}