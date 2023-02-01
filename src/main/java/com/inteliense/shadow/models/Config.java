package com.inteliense.shadow.models;

import com.inteliense.shadow.utils.JSON;
import com.inteliense.shadow.utils.RunCommand;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Config {

    public static String textEditor = "vi";
    public static String projectName = "default";
    private static int currentBranch = 0;
    public static ArrayList<Branch> branches = new ArrayList<Branch>();
    public static String flavor = "debian";
    public static ArrayList<String> installed = new ArrayList<String>();
    public static boolean isOfflineInstallation = true;

    public static void loadConfig() throws IOException {

        String globalConfigPath = getConfigDir() + "global.conf";
        File file = new File(globalConfigPath);
        if(file.exists()) {
            Scanner reader = new Scanner(file);
            String content = "";
            while(reader.hasNextLine()) {
                content += reader.nextLine();
            }
            JSONObject object = JSON.getObject(content);
            textEditor = (String) object.get("text_editor");
            projectName = (String) object.get("project_name");
            flavor = (String) object.get("os_type");
            isOfflineInstallation = (boolean) object.get("offline_install");

            JSONArray installedArr = (JSONArray) object.get("installed_packages");
            for(int i=0; i< installedArr.size(); i++) {
                installed.add((String) installedArr.get(i));
            }

        }

        String projectConfigPath = getConfigDir() + projectName + "/" + "project.conf";

        File projectConfig = new File(projectConfigPath);
        if(file.exists()) {
            Scanner reader = new Scanner(projectConfig);
            String content = "";
            while(reader.hasNextLine()) {
                content += reader.nextLine();
            }
            JSONObject project = JSON.getObject(content);
            String currentBranchId = (String) project.get("current_branch");
            JSONArray branchesArr = (JSONArray) project.get("branches");
            for(int i=0; i<branchesArr.size(); i++) {
                JSONObject branch = (JSONObject) branchesArr.get(i);
                Branch branchData = new Branch(branch);
                if(branchData.getId().equals(currentBranchId)) currentBranch = i;
                branches.add(branchData);
            }
        }

    }

    public static void saveConfig() {

        try {
            saveGlobalConfig(getConfigDir() + "global.conf");
            saveProjectConfig(getConfigDir() + projectName + "/project.conf");
            for (int i = 0; i < branches.size(); i++) {
                String branchId = branches.get(i).getId();
                saveBranchConfig(getConfigDir() + projectName + "/branches/" + branchId + "/branch.conf",
                        JSON.getString(branches.get(i).getEventsObj()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void initConfig(String textEditor, String projectName, String firstBranchName, String flavor, boolean isOfflineInstallation) {

    }

    public static void initConfig() {
        Branch defaultBranch = new Branch("default");
        branches.add(defaultBranch);
        saveConfig();
    }

    private static JSONObject getGlobalConfigObj() {

        JSONObject obj = new JSONObject();
        obj.put("text_editor", textEditor);
        obj.put("project_name", projectName);
        obj.put("os_type", flavor);
        obj.put("offline_install", isOfflineInstallation);

        JSONArray installedArr = new JSONArray();

        for(int i=0; i< installed.size(); i++) {
            installedArr.add(installed.get(i));
        }

        obj.put("installed_packages", installedArr);

        return obj;
    }

    private static JSONObject getProjectConfigObj() {

        JSONObject obj = new JSONObject();
        obj.put("name", projectName);

        JSONArray branchesArr = new JSONArray();

        for(int i=0; i< branches.size(); i++) {
            branchesArr.add(branches.get(i).getJson());
        }

        obj.put("branches", branchesArr);

        return obj;
    }

    private static void saveGlobalConfig(String path) throws IOException {
        File file = new File(path);
        if(!file.exists()) {
            makeDirectories(path);
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(file);
        pw.println(JSON.getString(getGlobalConfigObj()));
        pw.flush();
        pw.close();
    }

    private static void saveProjectConfig(String path) throws IOException {
        File file = new File(path);
        if(!file.exists()) {
            makeDirectories(path);
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(file);
        pw.println(JSON.getString(getProjectConfigObj()));
        pw.flush();
        pw.close();
    }

    private static void saveBranchConfig(String path, String content) throws IOException {
        File file = new File(path);
        if(!file.exists()) {
            makeDirectories(path);
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(file);
        pw.println(content);
        pw.flush();
        pw.close();
    }

    private static void makeDirectories(String input) {
        String[] parts = input.substring(1).split("/");
        String path = "";
        for(int i=0; i<parts.length - 1; i++) {
            path += "/" + parts[i];
        }
        File dirs = new File(path);
        if(!dirs.exists()) dirs.mkdirs();
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
