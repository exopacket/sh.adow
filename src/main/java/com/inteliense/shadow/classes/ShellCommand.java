package com.inteliense.shadow.classes;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ShellCommand extends Event {

    private String command;
    private String dir;
    private ArrayList<String> inputValues = new ArrayList<String>();
    private int index = -1;

    public ShellCommand(String command, String dir) {
        super();
        index = Config.getCurrent().size();
        this.command = command;
        this.dir = dir;
    }

    public ShellCommand(JSONObject obj) {

        super();

        this.index = Integer.parseInt((String) obj.get("index"));
        this.command = (String) obj.get("value");
        this.dir = (String) obj.get("dir");

        JSONArray inputArr = (JSONArray) obj.get("input");
        for(int i=0; i< inputArr.size(); i++) {
            JSONObject input = (JSONObject) inputArr.get(i);
            this.inputValues.add((String) input.get("value"));
        }

    }

    public String getHistoryString() {
        return command;
    }

    public String getType() {
        return "ShellCommand";
    }

    public String[] getShellCode(String[] args) {

        String userHome = "/home/" + Config.username;
        String fixDir = dir.replaceAll(userHome, "/home/\\$\\{INSTALL_USER\\}");

        if(inputValues.size() == 0) {
            return new String[]{"cd \"" + fixDir + "\" && " + command};
        } else {
            String val = "{ ";
            for(int i=0; i<inputValues.size(); i++) {
                val += "echo \"" + inputValues.get(i) + "\"; ";
            }
            val += "} | " + command;
            return new String[]{"cd \"" + fixDir + "\" && " + val};
        }

    }

    public void addInputValue(String value) {
        this.inputValues.add(value);
    }

    public JSONObject getObject() {

        JSONObject obj = new JSONObject();
        obj.put("index", "" + this.index);
        obj.put("type", "shell");
        obj.put("value", command);
        obj.put("key", getUniqueId());
        obj.put("dir", dir);

        JSONArray inputArr = new JSONArray();
        for(int i=0; i< inputValues.size(); i++) {
            JSONObject input = new JSONObject();
            input.put("index", "" + i);
            input.put("value", inputValues.get(i));
            inputArr.add(input);
        }

        obj.put("input", inputArr);

        return obj;
    }

}
