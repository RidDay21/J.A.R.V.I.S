package ru.nsu;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Main {
    public static void main(String[] args) {
        // Проверка короче есть агрумент или нет, понял?
        if (args.length == 0) {
            System.err.println("Ошибка: не указан путь к JAR-файлу");
            System.err.println("Использование: java -jar JARvis.jar <путь-к-jar-файлу>");
            System.exit(1);
        }

        // Первый аргумент — путь к JAR-файлу
        String jarPath = args[0];

        // Вызываем метод анализа, йоу.
        analyzeJar(jarPath);
    }

    public static void analyzeJar(String jarPath) {
        File jarFile = new File(jarPath);

        // Проверяем, существует ли файл
        if (!jarFile.exists()) {
            System.err.println("Ошибка: файл не существует - " + jarPath);
            System.exit(1);
        }

        if (!jarFile.isFile()) {
            System.err.println("Ошибка: указанный путь не является файлом - " + jarPath);
            System.exit(1);
        }

        try (JarFile jar = new JarFile(jarFile)) {
            System.out.println("Анализируем JAR-файл: " + jarPath);

            Enumeration<JarEntry> entries = jar.entries();
            int classCount = 0;

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Обрабатываем только class-файлы из папки с приложением
                if (entryName.endsWith(".class") && entryName.startsWith("BOOT-INF/classes/")) {
                    classCount++;
                    String shortName = entryName.replace("BOOT-INF/classes/", "");
                    System.out.println("Найден class-файл: " + shortName);
                    // TODO: здесь будем анализировать класс через ASM
                    // InputStream is = jar.getInputStream(entry);
                    // ClassReader cr = new ClassReader(is);
                }
            }

            System.out.println("Всего найдено class-файлов: " + classCount);

        } catch (IOException e) {
            System.err.println("Ошибка при чтении JAR-файла: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

