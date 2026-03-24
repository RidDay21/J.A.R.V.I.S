package jarvis.analyzer;

import jarvis.model.Endpoint;
import jarvis.model.Schema;
import jarvis.model.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link ControllerParser}.
 * Проверяют извлечение данных из аннотаций Spring Web.
 */
class ControllerParserTest {

    private ControllerParser parser;
    private TypeAnalyzer mockAnalyzer;

    @RequestMapping("/api/v1")
    static class MockController {
        @PostMapping("/test/{id}")
        public void testMethod(
                @PathVariable("id") String id,
                @RequestParam("query") int q,
                @RequestBody String body) {}
    }

    @BeforeEach
    void setUp() {
        mockAnalyzer = Mockito.mock(TypeAnalyzer.class);
        when(mockAnalyzer.analyze(any())).thenReturn(new Schema("String"));
        parser = new ControllerParser(mockAnalyzer);
    }

    /**
     * Проверка путей и параметров.
     */
    @Test
    @DisplayName("Должен собирать путь и находить Path, Query параметры и Body")
    void shouldParseEndpointDetails() {
        List<Endpoint> result = parser.parse(MockController.class);

        assertThat(result).hasSize(1);
        Endpoint e = result.get(0);

        assertThat(e.path).isEqualTo("/api/v1/test/{id}");
        assertThat(e.method).isEqualTo("POST");

        // Проверка параметров (path и query)
        assertThat(e.parameters).hasSize(2);
        assertThat(e.parameters).anyMatch(p -> p.in.equals("path") && p.name.equals("id"));
        assertThat(e.parameters).anyMatch(p -> p.in.equals("query") && p.name.equals("query"));

        // Проверка Body
        assertThat(e.requestBody).isNotNull();
    }
}