package com.inteliense.shadow.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ShellCommand extends Event {

    private String command;
    private ArrayList<String> inputValues = new ArrayList<String>();
    private int index = -1;

    public ShellCommand(String command) {
        index = Config.getCurrent().size();
        this.command = command;
    }

    public ShellCommand(JSONObject obj) {

        this.index = (int) obj.get("index");
        this.command = (String) obj.get("value");

        JSONArray inputArr = (JSONArray) obj.get("input");
        for(int i=0; i< inputArr.size(); i++) {
            JSONObject input = (JSONObject) inputArr.get(i);
            this.inputValues.add((String) input.get("value"));
        }

    }

    public String getType() {
        return "ShellCommand";
    }

    public void addInputValue(String value) {
        this.inputValues.add(value);
    }
    public JSONObject getObject() {

        JSONObject obj = new JSONObject();
        obj.put("index", this.index);
        obj.put("type", "shell");
        obj.put("value", command);

        JSONArray inputArr = new JSONArray();
        for(int i=0; i< inputValues.size(); i++) {
            JSONObject input = new JSONObject();
            input.put("index", i);
            input.put("value", inputValues.get(i));
            inputArr.add(input);
        }

        obj.put("input", inputArr);

        return obj;
    }

}
