package search.condition.query.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.elasticsearch.index.query.BoolQueryBuilder;
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
    @DisplayName("Generating should succeed when the parameter is a valid JSON element")
    public void testGenerate_whenParamIsValidLongJsonElement_thenSuccess() throws Exception {

        String searchRequest = "{\"sample0\":{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},\"sample1\":[{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},{\"field\":\"age\",\"operator\":\">\",\"value\":10}],\"sample2\":{\"group0\":{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},\"group1\":{\"field\":\"age\",\"operator\":\">\",\"value\":10}},\"sample4\":{\"field\":\"city\",\"operator\":\"in\",\"value\":\"dea\"},\"sample5\":{\"field\":\"age\",\"operator\":\"range\",\"begin\":20,\"end\":30},\"sample3\":{\"field\":\"city\",\"operator\":\"range\",\"value\":[\"seoul\",\"daejeon\",\"daegu\",\"busan\"]}}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        BoolQueryBuilder boolQueryBuilder = generator.generate(json);

        assertThat(boolQueryBuilder).isNotNull();
    }

    @Test
    @DisplayName("Generating should throw InvalidFormatException when the parameter is not a valid JSON")
    public void testGenerate_whenParamIsNotJson_thenInvalidFormatException() {

        String searchRequest = "aaa";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        assertThatThrownBy(() -> {
            generator.generate(json);
        }).isInstanceOf(InvalidFormatException.class);
    }

}
