package com.inteliense.shadow.models;

import com.inteliense.shadow.utils.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.inteliense.shadow.models.Config.*;

public class Branch {
    
    private String name;
    private String notes;
    private String createdTimestamp;
    private String id;

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
        JSONArray commands = (JSONArray) obj.get("events");

        for(int i=0; i<commands.size(); i++) {
            JSONObject command = (JSONObject) commands.get(i);
            String type = (String) command.get("type");
            if(type.equals("var")) {
                this.commands.add(new Variable(command));
            } else if(type.equals("pkg")) {
                this.commands.add(new Package(command));
            } else if(type.equals("file")) {
                this.commands.add(new EditFile(command));
            } else if(type.equals("shell")) {
                this.commands.add(new TerminalCommand(command));
            }
        }

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