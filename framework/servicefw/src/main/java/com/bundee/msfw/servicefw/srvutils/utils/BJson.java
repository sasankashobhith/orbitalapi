package com.bundee.msfw.servicefw.srvutils.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.fw.GSONExclusionStrategy;
import com.bundee.msfw.servicefw.fw.NumberDeserializer;
import com.bundee.msfw.servicefw.fw.UTF8StringSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class BJson {
	private static GSONExclusionStrategy exStrategy = new GSONExclusionStrategy();
    Gson gson;

    public BJson() {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting().registerTypeAdapter(UTF8String.class, new UTF8StringSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(ZonedDateTime.class, new ZonedDateTimeCustomDeserializer());
        gsonBuilder.registerTypeHierarchyAdapter(ZonedDateTime.class, new ZonedDateTimeCustomSerializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss");
        gsonBuilder.registerTypeAdapter(int.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(byte.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(short.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(float.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(double.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(long.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(Byte.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(Short.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(Integer.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(Long.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(Float.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(Double.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(BigDecimal.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(BigInteger.class, new NumberDeserializer());
        gsonBuilder.registerTypeAdapter(Number.class, new NumberDeserializer());

        gsonBuilder.addSerializationExclusionStrategy(exStrategy);
        
        gson = gsonBuilder.create();
    }

    public Object fromJson(String jsonString, Class<?> dtoClass) {
        Object obj = null;
        try {
            obj = gson.fromJson(jsonString, dtoClass);
        } catch (JsonSyntaxException jse) {
        }

        return obj;
    }

    public Collection<Map<String, String>> fromJSONAsCollMap(BLogger logger, String jsonString) {
        TypeToken<Collection<Map<String, String>>> collMapType = new TypeToken<Collection<Map<String, String>>>() {
        };
        return gson.fromJson(jsonString, collMapType);
    }

    public Map<String, String> fromJSONAsMap(BLogger logger, String jsonString) {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        return gson.fromJson(jsonString, mapType);
    }

    public UTF8String toJson(Object javaPOJO) {
        return new UTF8String(gson.toJson(javaPOJO));
    }
}
