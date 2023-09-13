package search.condition.query.generator;

import org.elasticsearch.index.query.BoolQueryBuilder;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import search.condition.query.generator.exception.InvalidFormatException;

public class ElasticsearchQueryGeneratorTest {

    ElasticsearchQueryGenerator generator = new ElasticsearchQueryGenerator();

    @Test
    @DisplayName("Generating should succeed when the parameter is a valid JSON element")
    public void testGenerate_whenParamIsValidJsonElement_thenSuccess() throws Exception {

        String searchRequest = "{\"field\":\"user.name\",\"operator\":\"eq\",\"value\":\"shinwoosub\"}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        BoolQueryBuilder boolQueryBuilder = generator.generate(json);

        assertThat(boolQueryBuilder).isNotNull();
    }

    @Test
    @DisplayName("Generating should throw InvalidFormatException when the parameter is not a valid JSON")
    public void testGenerate_whenParamIsNotJson_thenThrowInvalidFormatException() {

        String searchRequest = "aaa";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        assertThatThrownBy(() -> {
            generator.generate(json);
        }).isInstanceOf(InvalidFormatException.class);
    }

    @Test
    @DisplayName("Generating should throw InvalidFormatException when the parameter JSON is not in the search format")
    public void testGenerate_whenParamJsonIsNotSearchFormat_thenThrowInvalidFormatException() {

        String searchRequest = "{\"field\":\"user.name\",\"name\":\"eq\",\"desc\":\"shinwoosub\"}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        assertThatThrownBy(() -> {
            generator.generate(json);
        }).isInstanceOf(InvalidFormatException.class);
    }

}
