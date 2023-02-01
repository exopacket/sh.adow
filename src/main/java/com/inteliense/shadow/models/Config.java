package com.inteliense.shadow.models;

import com.inteliense.shadow.utils.JSON;
import com.inteliense.shadow.utils.RunCommand;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Config {

    public static String textEditor = "vi";
    public static String projectName = "default";
    private static int currentBranch = 0;
    public static ArrayList<Branch> branches = new ArrayList<Branch>();
    public static String flavor = "debian";
    public static ArrayList<String> installed = new ArrayList<String>();

    public static void loadConfig() throws IOException {

        String globalConfigPath = getConfigDir() + "global.conf";
        File file = new File(globalConfigPath);
        if(file.exists()) {
            Scanner reader = new Scanner(globalConfigPath);
            String content = "";
            while(reader.hasNextLine()) {
                content += reader.nextLine();
            }
            JSONObject object = JSON.getObject(content);
            textEditor = (String) object.get("text_editor");
            projectName = (String) object.get("project_name");
            flavor = (String) object.get("os_type");
        }

        String projectConfigPath = getConfigDir() + projectName + "/" + "project.conf";

        File projectConfig = new File(projectConfigPath);
        if(!file.exists()) {
            file.mkdirs();
            file.createNewFile();
        } else {
            Scanner reader = new Scanner(projectConfig);
            String content = "";
            while(reader.hasNextLine()) {
                content += reader.nextLine();
            }
            JSONObject project = JSON.getObject(content);
            JSONArray branchesArr = (JSONArray) project.get("branches");
            for(int i=0; i<branchesArr.size(); i++) {
                JSONObject branch = (JSONObject) branchesArr.get(i);
                branches.add(new Branch(branch));
            }
        }

    }

    public static void saveConfig() {

    }

    public static Branch getCurrent() {
        return branches.get(currentBranch);
    }

    public static ArrayList<String> getInstalled() {
        return installed;
    }

    public static String getConfigDir() {
        try {
            String home = RunCommand.withOut("echo $HOME")[0];
            if(!home.trim().equals("")) {
                return fixPath(home) + ".config/shadow/";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "/root/.config/shadow/";
    }

    public static String fixPath(String in) {
        if(!in.endsWith("/")) return in + "/";
        return in;
    }

}
