package search.condition.query.generator;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import search.condition.query.generator.exception.InvalidFormatException;

public class RdbQueryGenerator {

    public String generate(JsonElement jsonElement) throws Exception {

        String whereCondition = "";

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
                        if ("BETWEEN".equalsIgnoreCase(operator) || "RANGE".equalsIgnoreCase(operator)) {
                            JsonElement elementBegin = jsonObject.get("begin");
                            JsonElement elementEnd = jsonObject.get("end");

                            if ((elementBegin != null && elementBegin.isJsonPrimitive())
                                    && (elementEnd != null && elementEnd.isJsonPrimitive())) {

                                whereCondition += field + " ";

                                if (isNot) {
                                    whereCondition += "NOT ";
                                }

                                whereCondition += "BETWEEN ";
                                whereCondition += elementBegin.getAsString();
                                whereCondition += " AND ";
                                whereCondition += elementEnd.getAsString();

                            }
                        } 
                    } else {
                        if (elementValue.isJsonPrimitive()) {
                            JsonPrimitive jsonPrimitive = elementValue.getAsJsonPrimitive();

                            if (jsonPrimitive.isString()) {
                                String value = toString(elementValue);

                                String covertedOperator = primitiveOperatorConversion(operator);

                                if (!covertedOperator.isEmpty()) {

                                    whereCondition += field;
                                    whereCondition += covertedOperator;
                                    whereCondition += "\'" + value + "\'"; 
                                } else {
                                    if ("IN".equalsIgnoreCase(operator) || "CTNS".equalsIgnoreCase(operator)
                                            || "CONTAINS".equalsIgnoreCase(operator)) {

                                        String tempQuery = "";
                                        tempQuery += field + " ";
                                        tempQuery += "LIKE ";
                                        tempQuery += "\'%" + value + "\'";
                                        tempQuery += " OR ";
                                        tempQuery += field + " ";
                                        tempQuery += "LIKE ";
                                        tempQuery += "\'%" + value + "%\'";
                                        tempQuery += " OR ";
                                        tempQuery += field + " ";
                                        tempQuery += "LIKE ";
                                        tempQuery += "\'" + value + "%\'";

                                        if (isNot) {
                                            whereCondition += "NOT (" + tempQuery + ") ";
                                        } else {
                                            whereCondition += tempQuery;
                                        }

                                    }
                                }
                            } else if (jsonPrimitive.isNumber()) {
                                long value = jsonPrimitive.getAsLong();

                                String covertedOperator = primitiveOperatorConversion(operator);

                                whereCondition += field;
                                whereCondition += covertedOperator;
                                whereCondition += value;
                            }
                        } else if ("BETWEEN".equalsIgnoreCase(operator) || "RANGE".equalsIgnoreCase(operator)) {

                            String values = "";
                            for (JsonElement element : elementValue.getAsJsonArray()) {

                                if (!values.isEmpty()) {
                                    values += ", " + "'" + element.getAsString() + "'";
                                } else {
                                    values += "'" + element.getAsString() + "'";
                                }
                            }
                            whereCondition += field;
                            whereCondition += " IN (" + values + ")";
                        }
                    }
                }
                return whereCondition;
            } else {
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {

                    if (!whereCondition.isEmpty()) {
                        whereCondition += " OR ";
                    }

                    whereCondition += "( " + generate(entry.getValue()) + " )";
                }
            }
        } else if (jsonElement != null && jsonElement.isJsonArray()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {

                if (!whereCondition.isEmpty()) {
                    whereCondition += " AND ";
                }

                whereCondition += "( " + generate(element) + " )";
            }
        } else {
            throw new InvalidFormatException(
                    "It appears to not be a valid JSON format consisting of key-value pairs.");
        }
        return whereCondition;
    }

    public String toString(JsonElement elementValue) {

        String str = "";

        if (elementValue.isJsonPrimitive()) {
            str = elementValue.getAsJsonPrimitive().getAsString();
        }

        return str;
    }

    public String primitiveOperatorConversion(String operator) {

        if ("eq".equalsIgnoreCase(operator) || "=".equalsIgnoreCase(operator)) {
            return "=";
        }

        if ("neq".equalsIgnoreCase(operator) || "!=".equalsIgnoreCase(operator)
                || "<>".equalsIgnoreCase(operator)) {

            return "!=";
        }

        if ("lt".equalsIgnoreCase(operator) || "<".equalsIgnoreCase(operator)) {
            return "<";
        }

        if ("le".equalsIgnoreCase(operator) || "<=".equalsIgnoreCase(operator)) {
            return "<=";
        }

        if ("gt".equalsIgnoreCase(operator) || ">".equalsIgnoreCase(operator)) {
            return ">";
        }

        if ("gte".equalsIgnoreCase(operator) || ">=".equalsIgnoreCase(operator)) {
            return ">=";
        }

        return "";
    }

}
