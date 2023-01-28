package com.inteliense.shadow;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSON {

    public static JSONObject getObject(String body) {

        try {

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(body);
            JSONObject jsonObj = (JSONObject) obj;

            return jsonObj;

        } catch (Exception ex) {

            ex.printStackTrace();

        }

        return new JSONObject();

    }

    public static JSONArray getArray(String body) {

        try {

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(body);
            JSONObject jsonObj = (JSONObject) obj;

            return (JSONArray) jsonObj.get("arr");

        } catch (Exception ex) {

            ex.printStackTrace();

        }

        return new JSONArray();

    }

    public static String getString(JSONObject obj) {

        try {

            StringWriter out = new StringWriter();
            obj.writeJSONString(out);
            return out.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "{}";

    }

}
