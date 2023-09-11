package search.condition.query.generator.impl;

import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.softcamp.jsc.Condition;

public class ElasticsearchQueryGenerator {

    public BoolQueryBuilder generate(Condition condition) throws Exception {
        return generate(condition.toJson());
    }

    public BoolQueryBuilder generate(JsonElement jsonElement) throws Exception {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
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
                        if ("BETWEEN".equals(operator) || "RANGE".equals(operator)) {
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
                        } else if ("EXISTS".equals(operator)) {
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
                                    if ("IN".equals(operator) || "CTNS".equals(operator)
                                            || "CONTAINS".equals(operator)) {
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
            if ("eq".equals(operator) || "=".equals(operator)) {

                BoolQueryBuilder shouldQuery = new BoolQueryBuilder().minimumShouldMatch(1);
                shouldQuery.should(QueryBuilders.termQuery(field + ".raw", value));
                shouldQuery.should(QueryBuilders.termQuery(field, value));
                boolQueryBuilder.filter(shouldQuery);
                return boolQueryBuilder;
            }

            if ("neq".equals(operator) || "!=".equals(operator) || "<>".equals(operator)) {

                BoolQueryBuilder tempBoolQueryBuilder = QueryBuilders.boolQuery();
                tempBoolQueryBuilder.filter(QueryBuilders.termQuery(field, value));
                tempBoolQueryBuilder.filter(QueryBuilders.termQuery(field, value));

                boolQueryBuilder.mustNot(tempBoolQueryBuilder);
                return boolQueryBuilder;
            }

        } else if (clazz == long.class || clazz == Long.class || clazz == int.class || clazz == Integer.class
                || clazz == double.class || clazz == Double.class) {
            if ("lt".equals(operator) || "<".equals(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).lt(value));
                return boolQueryBuilder;
            }

            if ("le".equals(operator) || "<=".equals(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).lte(value));
                return boolQueryBuilder;
            }

            if ("gt".equals(operator) || ">".equals(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).gt(value));
                return boolQueryBuilder;
            }

            if ("gte".equals(operator) || ">=".equals(operator)) {
                boolQueryBuilder.filter(
                        QueryBuilders.rangeQuery(field).gte(value));
                return boolQueryBuilder;
            }
        }

        return null;
    }

}
