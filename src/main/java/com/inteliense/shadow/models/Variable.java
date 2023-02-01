package com.inteliense.shadow.models;

import org.json.simple.JSONObject;

public class Variable extends Command {

    private int index = -1;
    private String name;
    private String value;

    public Variable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Variable(JSONObject obj) {
        this.index = (int) obj.get("index");
        this.name = (String) obj.get("name");
        this.value = (String) obj.get("value");
    }

    public String getType() {
        return "Variable";
    }

    public JSONObject getObject() {
        JSONObject obj = new JSONObject();
        obj.put("index", this.index);
        obj.put("type", "var");
        obj.put("name", this.name);
        obj.put("value", this.value);

        return obj;
    }

}
