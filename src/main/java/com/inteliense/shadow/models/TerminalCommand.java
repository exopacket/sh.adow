package com.inteliense.shadow.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class TerminalCommand extends Command {

    private String command;
    private ArrayList<String> inputValues = new ArrayList<String>();
    private int index = -1;

    public TerminalCommand(String command) {
        index = Config.getCurrent().size();
        this.command = command;
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
