package com.inteliense.shadow.models;

import com.inteliense.shadow.utils.SHA;
import org.json.simple.JSONObject;

public abstract class Event {

    private String uniqueId;

    public Event() {
        uniqueId = SHA.getSha1("event_" + System.currentTimeMillis());
    }

    public abstract String getType();

    public abstract JSONObject getObject();

    public abstract String getHistoryString();

    public String getUniqueId() {
        return uniqueId;
    }

}
