package com.inteliense.shadow.models;

import java.util.ArrayList;

public class Branch {
    
    private String name;
    private String notes;
    private String createdTimestamp;

    private ArrayList<Command> commands = new ArrayList<Command>();

    public Branch(String name, String notes) {
        this.name = name;
        this.notes = notes;
        this.createdTimestamp = "" + System.currentTimeMillis();
    }

    public Branch(String name) {
        this.name = name;
        this.createdTimestamp = "" + System.currentTimeMillis();
    }

    public void addNotes(String notes) {
        this.notes = notes;
    }

    public void add(Command command) {
        commands.add(command);
    }

    public int size() {
        return commands.size();
    }

}