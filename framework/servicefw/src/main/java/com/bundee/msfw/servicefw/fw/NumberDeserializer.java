package com.bundee.msfw.servicefw.fw;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class NumberDeserializer implements JsonDeserializer<Number> {

    @Override
    public Number deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive() && (json.isJsonNull() || json.getAsString().equals(""))) {
            return getDefaultPrimitiveValue(typeOfT);
        } else if (json.isJsonNull() || json.getAsString().equals("")) {
            return null;
        } else {
            return getNumberValue(json, typeOfT);
        }
    }

    private Number getNumberValue(JsonElement json, Type typeOfT) {
        switch (typeOfT.getTypeName()) {
            case "java.lang.Byte":
            case "byte":
                return Byte.parseByte(json.getAsString());
            case "java.lang.Short":
            case "short":
                return Short.parseShort(json.getAsString());
            case "java.lang.Integer":
            case "int":
                return Integer.parseInt(json.getAsString());
            case "java.lang.Long":
            case "long":
                return Long.parseLong(json.getAsString());
            case "java.lang.Float":
            case "float":
                return Float.parseFloat(json.getAsString());
            case "java.lang.Double":
            case "double":
                return Double.parseDouble(json.getAsString());
            case "java.math.BigDecimal":
                return BigDecimal.valueOf(Long.parseLong(json.getAsString()));
            case "java.math.BigInteger":
                return BigInteger.valueOf(Long.parseLong(json.getAsString()));
            default:
                return json.getAsNumber();
        }
    }


    private Number getDefaultPrimitiveValue(Type typeOfT) {
        switch (typeOfT.getTypeName()) {
            case "int":
            case "byte":
            case "short":
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
                return 0;
            case "float":
            case "java.lang.Float":
                return 0.0f;
            case "double":
            case "java.lang.Double":
                return 0.0d;
            case "long":
            case "java.lang.Long":
                return 0L;
            case "java.math.BigDecimal":
                return BigDecimal.ZERO;
            case "java.math.BigInteger":
                return BigInteger.ZERO;
        }
        return 0;
    }
}
