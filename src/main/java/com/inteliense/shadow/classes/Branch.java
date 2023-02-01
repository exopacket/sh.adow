package com.inteliense.shadow.classes;

import com.inteliense.shadow.utils.JSON;
import com.inteliense.shadow.utils.SHA;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.inteliense.shadow.classes.Config.*;

public class Branch {
    
    private String name;
    private String notes = "";
    private String createdTimestamp;
    private String id;
    private ArrayList<Event> events = new ArrayList<Event>();

    public Branch(String name, String notes) {
        this.name = name;
        this.notes = notes;
        this.createdTimestamp = "" + System.currentTimeMillis();
        this.id = SHA.getSha1("branch_" + this.createdTimestamp);
    }

    public Branch(String name) {
        this.name = name;
        this.createdTimestamp = "" + System.currentTimeMillis();
        this.id = SHA.getSha1("branch_" + this.createdTimestamp);
    }

    public Branch(JSONObject object) throws FileNotFoundException {

        String branchId = (String) object.get("branch_id");
        String branchName = (String) object.get("branch_name");
        String branchNotes = (String) object.get("branch_notes");
        String branchTimestamp = (String) object.get("branch_created");

        this.id = branchId;
        this.name = branchName;
        this.notes = branchNotes;
        this.createdTimestamp = branchTimestamp;

        String branchConfigPath = getConfigDir() + projectName + "/branches/" + branchId + "/branch.conf";

        File file = new File(branchConfigPath);
        Scanner scnr = new Scanner(file);

        String content = "";
        while(scnr.hasNextLine()) {
            content += scnr.nextLine();
        }

        JSONObject obj = JSON.getObject(content);
        JSONArray events = (JSONArray) obj.get("events");

        for(int i=0; i<events.size(); i++) {
            JSONObject command = (JSONObject) events.get(i);
            String type = (String) command.get("type");
            if(type.equals("var")) {
                this.events.add(new Variable(command));
            } else if(type.equals("pkg")) {
                this.events.add(new Package(command));
            } else if(type.equals("file")) {
                this.events.add(new EditFile(command));
            } else if(type.equals("shell")) {
                this.events.add(new ShellCommand(command));
            }
        }

    }
    
    public JSONObject getJson() {
        
        JSONObject obj = new JSONObject();
        
        obj.put("branch_id", id);
        obj.put("branch_name", name);
        obj.put("branch_notes", notes);
        obj.put("branch_created", createdTimestamp);
        
        return obj;
        
    }
    
    public JSONObject getEventsObj() {
        JSONObject object = new JSONObject();
        JSONArray arr = new JSONArray();
        for(int i=0; i<events.size(); i++) {
            arr.add(events.get(i).getObject());
        }
        object.put("events", arr);
        return object;
    }

    public void appendNotes(String val) {
        notes += "\n" + val;
    }

    public String getNotes() {
        return notes;
    }

    public String getName() {
        return name;
    }
    public void addNotes(String notes) {
        this.notes = notes;
    }

    public void add(Event command) {
        events.add(command);
    }

    public ArrayList<Event> history() {
        return this.events;
    }

    public int size() {
        return events.size();
    }
    
    public String getId() {
        return this.id;
    }

    public void clearEvents() {
        this.events.clear();
    }

}