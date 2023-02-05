package com.inteliense.shadow.classes;

import org.json.simple.JSONObject;

public class Variable extends Event {

    private int index = -1;
    private String name;
    private String value;

    public Variable(String name, String value) {
        super();
        index = Config.getCurrent().size();
        this.name = name;
        this.value = value;
    }

    public Variable(JSONObject obj) {

        super();

        this.index = Integer.parseInt((String) obj.get("index"));
        this.name = (String) obj.get("name");
        this.value = (String) obj.get("value");
    }

    public String getHistoryString() {
        return "var " + name + " = " + value;
    }

    public String getType() {
        return "Variable";
    }

    @Override
    public String[] getShellCode(String[] args) {
        return new String[]{"export $" + this.name + "=" + this.value};
    }

    public JSONObject getObject() {
        JSONObject obj = new JSONObject();
        obj.put("index", "" + this.index);
        obj.put("type", "var");
        obj.put("name", this.name);
        obj.put("value", this.value);
        obj.put("key", getUniqueId());

        return obj;
    }

}
