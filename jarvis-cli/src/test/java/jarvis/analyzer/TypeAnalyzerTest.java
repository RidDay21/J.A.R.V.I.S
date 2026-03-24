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
 * Тесты для {@link TypeAnalyzer}.
 * Проверяют, как парсер разбирает Java-классы в объекты Schema.
 */
class TypeAnalyzerTest {

    private TypeAnalyzer typeAnalyzer;
    private Map<String, Schema> registry;

    @BeforeEach
    void setUp() {
        registry = new HashMap<>();
        typeAnalyzer = new TypeAnalyzer(registry);
    }

    static class SimpleDto {
        private String name;
        private int age;
        public static final String SECRET = "42";
    }

    static class NestedDto {
        private SimpleDto data;
    }

    static class RecursiveDto {
        private RecursiveDto parent;
    }

    /**
     * Проверка базовых типов и игнорирования статических полей.
     */
    @Test
    @DisplayName("Должен распознавать String/int и игнорировать static")
    void shouldParseSimpleTypes() {
        Schema schema = typeAnalyzer.analyze(SimpleDto.class);

        assertThat(schema.fields).hasSize(2);
        assertThat(schema.fields).anyMatch(f -> f.name.equals("name") && f.type.equals("String"));
        assertThat(schema.fields).anyMatch(f -> f.name.equals("age") && f.type.equals("int"));
        // Проверяем, что статическое поле SECRET не попало в список
        assertThat(schema.fields).noneMatch(f -> f.name.equals("SECRET"));
    }

    /**
     * Проверка вложенных объектов (рекурсии).
     */
    @Test
    @DisplayName("Должен помечать вложенные объекты как type='object' и заполнять ref")
    void shouldParseNestedObjects() {
        Schema schema = typeAnalyzer.analyze(NestedDto.class);

        FieldInfo nestedField = schema.fields.get(0);
        assertThat(nestedField.name).isEqualTo("data");
        assertThat(nestedField.type).isEqualTo("object");
        assertThat(nestedField.ref).isEqualTo("SimpleDto");

        // Проверяем, что вложенный класс попал в общий реестр
        assertThat(registry).containsKey("SimpleDto");
    }

    /**
     * Проверка циклической зависимости (Node -> Node).
     */
    @Test
    @DisplayName("Должен обрабатывать циклы в DTO без StackOverflow")
    void shouldHandleRecursion() {
        Schema schema = typeAnalyzer.analyze(RecursiveDto.class);

        assertThat(schema.type).isEqualTo("RecursiveDto");
        assertThat(schema.fields.get(0).ref).isEqualTo("RecursiveDto");
        assertThat(registry).hasSize(1);
    }
}