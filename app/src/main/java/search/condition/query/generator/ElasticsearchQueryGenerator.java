package search.condition.query.generator;

import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import search.condition.query.generator.exception.InvalidFormatException;

public class ElasticsearchQueryGenerator {

    /**
     * return true sample
     * {
     * "field" : "~",
     * "operator" : "~",
     * "value" : "~"
     * }
     * 
     * return false sample
     * {
     * "name" : "woosub"
     * }
     * 
     * @param jsonElement
     * @return true = vaild, false = invaild
     */
    public boolean hasRequiredKey(JsonObject jsonObject) {
        return jsonObject.has("field") && jsonObject.has("operator") && jsonObject.has("value");
    }

    public BoolQueryBuilder generate(JsonElement jsonElement) throws Exception {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (!hasRequiredKey(jsonObject)) {
                throw new InvalidFormatException(
                        "it may be in the correct JSON format but may not contain the 'field,' 'operator,' and 'value' keys.");
            }

            JsonElement elementField = jsonObject.get("field");
            if (elementField != null && elementField.isJsonPrimitive()
                    && elementField.getAsJsonPrimitive().isString()) {
                String field = elementField.getAsString();
                JsonElement elementOperator = jsonObject.get("operator");
                if (elementOperator != null && elementOperator.isJsonPrimitive()
                        && elementOperator.getAsJsonPrimitive().isString()) {
                    String operator = elementOperator.getAsString().toUpperCase();

                    boolean isNot = operator.contains("NOT ") || operator.contains("not ");
                    if (isNot) {
                        operator = operator.substring(("NOT ").length(), operator.length());
                    }

                    JsonElement elementValue = jsonObject.get("value");
                    if (elementValue == null) {
                        if ("BETWEEN".equalsIgnoreCase(operator) || "RANGE".equalsIgnoreCase(operator)) {
                            JsonElement elementBegin = jsonObject.get("begin");
                            JsonElement elementEnd = jsonObject.get("end");

                            if ((elementBegin != null && elementBegin.isJsonPrimitive())
                                    && (elementEnd != null && elementEnd.isJsonPrimitive())) {

                                if (isNot) {
                                    BoolQueryBuilder tempBoolQueryBuilder = QueryBuilders.boolQuery();

                                    tempBoolQueryBuilder.filter(
                                            QueryBuilders.rangeQuery(field)
                                                    .gte(elementBegin.getAsString()));

                                    tempBoolQueryBuilder.filter(
                                            QueryBuilders.rangeQuery(field)
                                                    .lte(elementEnd.getAsString()));

                                    boolQueryBuilder.mustNot(tempBoolQueryBuilder);
                                } else {
                                    boolQueryBuilder.filter(
                                            QueryBuilders.rangeQuery(field)
                                                    .gte(elementBegin.getAsString()));

                                    boolQueryBuilder.filter(
                                            QueryBuilders.rangeQuery(field)
                                                    .lte(elementEnd.getAsString()));
                                }

                            }
                        } else if ("EXISTS".equalsIgnoreCase(operator)) {
                            if (isNot) {
                                boolQueryBuilder.mustNot(QueryBuilders.existsQuery(field));
                            } else {
                                boolQueryBuilder.must(QueryBuilders.existsQuery(field));
                            }
                        }
                    } else {
                        if (elementValue.isJsonPrimitive()) {
                            JsonPrimitive jsonPrimitive = elementValue.getAsJsonPrimitive();

                            if (jsonPrimitive.isString()) {
                                String value = toString(elementValue);

                                BoolQueryBuilder createdBoolQueryBuilder = primitiveOperatorConversion(boolQueryBuilder,
                                        field,
                                        operator,
                                        value);
                                if (createdBoolQueryBuilder != null) {
                                    boolQueryBuilder = createdBoolQueryBuilder;
                                } else {
                                    if ("IN".equalsIgnoreCase(operator) || "CTNS".equalsIgnoreCase(operator)
                                            || "CONTAINS".equalsIgnoreCase(operator)) {
                                        BoolQueryBuilder shouldQuery = new BoolQueryBuilder().minimumShouldMatch(1);
                                        shouldQuery.should(QueryBuilders.termQuery(field + ".ngram", value));
                                        shouldQuery.should(QueryBuilders.termQuery(field + ".raw", value));
                                        shouldQuery.should(QueryBuilders.termQuery(field, value));
                                        if (isNot) {
                                            BoolQueryBuilder tempBoolQueryBuilder = QueryBuilders.boolQuery();
                                            tempBoolQueryBuilder.filter(shouldQuery);
                                            boolQueryBuilder.mustNot(tempBoolQueryBuilder);
                                        } else {
                                            boolQueryBuilder.filter(shouldQuery);
                                        }
                                    }
                                }
                            } else if (jsonPrimitive.isNumber()) {

                                long value = jsonPrimitive.getAsLong();
                                BoolQueryBuilder createdBoolQueryBuilder = primitiveOperatorConversion(boolQueryBuilder,
                                        field,
                                        operator,
                                        value);
                                if (createdBoolQueryBuilder != null) {
                                    boolQueryBuilder = createdBoolQueryBuilder;
                                }
                            }
                        }
                    }
                }
                return boolQueryBuilder;
            } else {
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    boolQueryBuilder.should(generate(entry.getValue()));
                }
            }

        } else if (jsonElement != null && jsonElement.isJsonArray()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                boolQueryBuilder.filter(generate(element));
            }
        } else {
            throw new InvalidFormatException(
                    "It appears to not be a valid JSON format consisting of key-value pairs.");
        }

        return boolQueryBuilder;
    }

    public String toString(JsonElement elementValue) throws Exception {

        String str = new String();

        if (elementValue.isJsonPrimitive()) {
            str = elementValue.getAsJsonPrimitive().getAsString();
        }

        return str;
    }

    public <T> BoolQueryBuilder primitiveOperatorConversion(BoolQueryBuilder boolQueryBuilder, String field,
            String operator,
            T value) {

        Class<?> clazz = value.getClass();

        if (clazz == String.class) {
            if ("eq".equalsIgnoreCase(operator) || "=".equalsIgnoreCase(operator)) {

                BoolQueryBuilder shouldQuery = new BoolQueryBuilder().minimumShouldMatch(1);
                shouldQuery.should(QueryBuilders.termQuery(field + ".raw", value));
                shouldQuery.should(QueryBuilders.termQuery(field, value));
                boolQueryBuilder.filter(shouldQuery);
                return boolQueryBuilder;
            }

            if ("neq".equalsIgnoreCase(operator) || "!=".equalsIgnoreCase(operator)
                    || "<>".equalsIgnoreCase(operator)) {

                BoolQueryBuilder tempBoolQueryBuilder = QueryBuilders.boolQuery();
                tempBoolQueryBuilder.filter(QueryBuilders.termQuery(field, value));
                tempBoolQueryBuilder.filter(QueryBuilders.termQuery(field, value));

                boolQueryBuilder.mustNot(tempBoolQueryBuilder);
                return boolQueryBuilder;
            }

        } else if (clazz == long.class || clazz == Long.class || clazz == int.class || clazz == Integer.class
                || clazz == double.class || clazz == Double.class) {
            if ("lt".equalsIgnoreCase(operator) || "<".equalsIgnoreCase(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).lt(value));
                return boolQueryBuilder;
            }

            if ("le".equalsIgnoreCase(operator) || "<=".equalsIgnoreCase(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).lte(value));
                return boolQueryBuilder;
            }

            if ("gt".equalsIgnoreCase(operator) || ">".equalsIgnoreCase(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).gt(value));
                return boolQueryBuilder;
            }

            if ("gte".equalsIgnoreCase(operator) || ">=".equalsIgnoreCase(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).gte(value));
                return boolQueryBuilder;
            }
        }

        return null;
    }

}
