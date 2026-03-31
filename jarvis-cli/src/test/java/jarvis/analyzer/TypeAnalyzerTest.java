package jarvis.analyzer;

import jarvis.model.Schema;
import jarvis.model.FieldInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Спецификация для {@link TypeAnalyzer}.
 * Описывает правила трансформации Java-классов в JSON-схемы для J.A.R.V.I.S.
 */
@DisplayName("Спецификация анализа типов (TypeAnalyzer)")
class TypeAnalyzerTest {

    private TypeAnalyzer typeAnalyzer;
    private Map<String, Schema> registry;

    @BeforeEach
    void setUp() {
        registry = new HashMap<>();
        typeAnalyzer = new TypeAnalyzer(registry);
    }

    // --- Тестовые DTO ---
    static class SimpleDto {
        private String name;
        private int age;
        private boolean active;
        public static final String SECRET = "42";
    }

    static class NestedDto {
        private SimpleDto data;
    }

    static class RecursiveDto {
        private RecursiveDto parent;
    }

    @Test
    @DisplayName("Должен конвертировать примитивы и базовые типы в JSON-совместимые названия")
    void shouldMapBasicTypesToWebStandard() {
        Schema schema = typeAnalyzer.analyze(SimpleDto.class);

        // Проверяем String -> string
        assertThat(schema.fields)
                .anyMatch(f -> f.name.equals("name") && f.type.equals("string"));

        // Проверяем int -> number
        assertThat(schema.fields)
                .anyMatch(f -> f.name.equals("age") && f.type.equals("number"));

        // Проверяем boolean -> boolean
        assertThat(schema.fields)
                .anyMatch(f -> f.name.equals("active") && f.type.equals("boolean"));
    }

    @Test
    @DisplayName("Должен игнорировать статические поля (константы)")
    void shouldIgnoreStaticFields() {
        Schema schema = typeAnalyzer.analyze(SimpleDto.class);

        // Поле SECRET не должно попасть в схему
        assertThat(schema.fields)
                .noneMatch(f -> f.name.equals("SECRET"));

        // Всего должно быть 3 поля (name, age, active)
        assertThat(schema.fields).hasSize(3);
    }

    @Test
    @DisplayName("Должен корректно определять вложенные объекты и регистрировать их")
    void shouldHandleNestedObjectsWithRegistry() {
        Schema schema = typeAnalyzer.analyze(NestedDto.class);

        // Проверяем поле 'data'
        assertThat(schema.fields).hasSize(1);
        FieldInfo field = schema.fields.get(0);

        assertThat(field.name).isEqualTo("data");
        assertThat(field.type).isEqualTo("object");
        assertThat(field.ref).isEqualTo("SimpleDto");

        // Проверяем, что вложенный класс тоже проанализирован и лежит в реестре
        assertThat(registry).containsKey("SimpleDto");
        assertThat(registry.get("SimpleDto").fields).isNotEmpty();
    }

    @Test
    @DisplayName("Должен предотвращать бесконечную рекурсию при циклической зависимости")
    void shouldPreventStackOverflowOnCycles() {
        Schema schema = typeAnalyzer.analyze(RecursiveDto.class);

        assertThat(schema.type).isEqualTo("RecursiveDto");
        assertThat(schema.fields).hasSize(1);
        assertThat(schema.fields.get(0).ref).isEqualTo("RecursiveDto");

        // В реестре должен быть только один экземпляр схемы
        assertThat(registry).containsKey("RecursiveDto");
    }
}