package com.inteliense.shadow.models;

import org.json.simple.JSONObject;

public abstract class Command {

    public abstract String getType();

    public abstract JSONObject getObject();

}
