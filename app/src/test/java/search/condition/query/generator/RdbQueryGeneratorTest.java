package search.condition.query.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import search.condition.query.generator.exception.InvalidFormatException;

public class RdbQueryGeneratorTest {
    RdbQueryGenerator generator = new RdbQueryGenerator();

    @Test
    @DisplayName("Generating should succeed when the parameter is a valid JSON element")
    public void testGenerate_whenParamIsValidJsonElement_thenSuccess() throws Exception {

        String searchRequest = "{\"field\":\"user.name\",\"operator\":\"eq\",\"value\":\"shinwoosub\"}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        String where = generator.generate(json);

        assertThat(where).isNotNull();
    }

    @Test
    @DisplayName("Generating should succeed when the parameter is a valid JSON element")
    public void testGenerate_whenParamIsValidLongJsonElement_thenSuccess() throws Exception {

        String searchRequest = "{\"sample0\":{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},\"sample1\":[{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},{\"field\":\"age\",\"operator\":\">\",\"value\":10}],\"sample2\":{\"group0\":{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},\"group1\":{\"field\":\"age\",\"operator\":\">\",\"value\":10}},\"sample4\":{\"field\":\"city\",\"operator\":\"in\",\"value\":\"dea\"},\"sample5\":{\"field\":\"age\",\"operator\":\"range\",\"begin\":20,\"end\":30},\"sample3\":{\"field\":\"city\",\"operator\":\"range\",\"value\":[\"seoul\",\"daejeon\",\"daegu\",\"busan\"]}}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        String where = generator.generate(json);

        assertThat(where).isNotNull();
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

    // AND
    @Test
    @DisplayName("Generating should succeed when parameter is 'AND' condition then success")
    public void testGenerate_whenParamIsAndCondition_thenSuccess() throws Exception {

        String searchRequest = "{\"sample1\":[{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},{\"field\":\"age\",\"operator\":\">\",\"value\":10}]}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        String where = generator.generate(json);

        assertThat(where).isNotNull();
    }

    // OR
    @Test
    @DisplayName("Generating should succeed when parameter is 'OR' condition then success")
    public void testGenerate_whenParamIsOrCondition_thenSuccess() throws Exception {

        String searchRequest = "{\"sample2\":{\"group0\":{\"field\":\"name\",\"operator\":\"=\",\"value\":\"test\"},\"group1\":{\"field\":\"age\",\"operator\":\">\",\"value\":10}}}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        String where = generator.generate(json);

        assertThat(where).isNotNull();
    }

    // IN
    @Test
    @DisplayName("Generating should succeed when parameter is 'IN' condition then success")
    public void testGenerate_whenParamIsInCondition_thenSuccess() throws Exception {

        String searchRequest = "{\"sample4\":{\"field\":\"city\",\"operator\":\"in\",\"value\":\"dae\"}}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        String where = generator.generate(json);

        assertThat(where).isNotNull();
    }

    // RANGE
    @Test
    @DisplayName("Generating should succeed when parameter is 'RANGE' condition then success")
    public void testGenerate_whenParamIsRangeCondition_thenSuccess() throws Exception {

        String searchRequest = "{\"sample5\":{\"field\":\"age\",\"operator\":\"range\",\"begin\":20,\"end\":30}}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        String where = generator.generate(json);

        assertThat(where).isNotNull();
    }

    // TEXT RANGE
    @Test
    @DisplayName("Generating should succeed when parameter is 'Text RANGE' condition then success")
    public void testGenerate_whenParamIsTextRangeCondition_thenSuccess() throws Exception {

        String searchRequest = "{\"sample3\":{\"field\":\"city\",\"operator\":\"range\",\"value\":[\"seoul\",\"daejeon\",\"daegu\",\"busan\"]}}";
        JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

        String where = generator.generate(json);

        assertThat(where).isNotNull();
    }

}
