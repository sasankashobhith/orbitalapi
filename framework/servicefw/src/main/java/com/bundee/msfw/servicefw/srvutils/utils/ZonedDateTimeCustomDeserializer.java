package com.bundee.msfw.servicefw.srvutils.utils;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ZonedDateTimeCustomDeserializer implements JsonDeserializer<ZonedDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    public ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)throws JsonParseException
    {
        try {
            LocalDateTime localDateTime= LocalDateTime.parse(json.getAsString(),
                    formatter.withLocale(Locale.ENGLISH));
           return ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        }catch(JsonParseException e){
            throw e;
        }
    }
}
