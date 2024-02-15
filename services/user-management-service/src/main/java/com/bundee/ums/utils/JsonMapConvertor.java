package com.bundee.ums.utils;

import com.google.gson.*;
import org.json.*;

import java.lang.reflect.*;
import java.util.*;

public class JsonMapConvertor {
    public static Map convertJsonToMap(JSONObject jsonObject){
        Gson gson = new Gson();
        // Define the type for the map
        Type type = new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType();
        // Parse the JSON into a Map
        Map<String, Object> map = gson.fromJson(String.valueOf(jsonObject), type);
        // You can now work with the map
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            int value;
            if(entry.getValue() instanceof ArrayList<?>){
                ArrayList objects= (ArrayList) entry.getValue();
                ArrayList<Integer> list=new ArrayList<Integer>();
                for(int i=0;i<objects.size();i++){
                    try {
                        value = (int) Math.round((Double) objects.get(i));
                        list.add(value);
                    } catch (Exception e) {
                        System.out.println(objects.get(i));
                    }
                }
                entry.setValue(list);
            }else {
                try {
                    value = (int) Math.round((Double)entry.getValue());
                    entry.setValue(value);
                } catch (Exception e) {
                }
            }
            // Add more type checks as needed for other data types.
        }
        return map;
    }
}
