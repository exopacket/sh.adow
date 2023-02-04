package com.inteliense.shadow.classes;

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

    public static String username = "";
    public static String textEditor = "vi";
    public static String projectName = "default";
    private static int currentBranch = 0;
    public static ArrayList<Branch> branches = new ArrayList<Branch>();
    public static String flavor = "debian";
    public static ArrayList<String> installed = new ArrayList<String>();
    public static boolean isOfflineInstallation = true;
    public static boolean dirtyDownload = false;

    public static boolean loadConfig() {

        try {

            String globalConfigPath = getConfigDir() + "global.conf";
            File file = new File(globalConfigPath);
            if (file.exists()) {
                Scanner reader = new Scanner(file);
                String content = "";
                while (reader.hasNextLine()) {
                    content += reader.nextLine();
                }
                JSONObject object = JSON.getObject(content);
                textEditor = (String) object.get("text_editor");
                projectName = (String) object.get("project_name");
                flavor = (String) object.get("os_type");
                isOfflineInstallation = (boolean) object.get("offline_install");
                dirtyDownload = (boolean) object.get("dirty_download");

                JSONArray installedArr = (JSONArray) object.get("installed_packages");
                for (int i = 0; i < installedArr.size(); i++) {
                    installed.add((String) installedArr.get(i));
                }

            }

            String projectConfigPath = getConfigDir() + projectName + "/" + "project.conf";

            File projectConfig = new File(projectConfigPath);
            if (file.exists()) {
                Scanner reader = new Scanner(projectConfig);
                String content = "";
                while (reader.hasNextLine()) {
                    content += reader.nextLine();
                }
                JSONObject project = JSON.getObject(content);
                String currentBranchId = (String) project.get("current_branch");
                JSONArray branchesArr = (JSONArray) project.get("branches");
                for (int i = 0; i < branchesArr.size(); i++) {
                    JSONObject branch = (JSONObject) branchesArr.get(i);
                    Branch branchData = new Branch(branch);
                    if (branchData.getId().equals(currentBranchId)) currentBranch = i;
                    branches.add(branchData);
                }
            }

            setUsername();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

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
            if(RunCommand.getUID() == 0) {
                String sudoUser = RunCommand.withOut("echo $SUDO_USER")[0];
                RunCommand.runAndWait("chown -R " + sudoUser + ":" + sudoUser + " " + getConfigDir());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void createBranch(String branchName, String notes) {

        branchName = branchName.replaceAll("\\s+", "_").toLowerCase();
        Branch branch = new Branch(branchName, notes);
        currentBranch = branches.size();
        branches.add(branch);

    }

    public static void initConfig(String textEditor, String projectName, String firstBranchName, String flavor, boolean isOfflineInstallation, boolean dirtyDownload, boolean editSudoers) {

        Config.textEditor = textEditor;
        Config.projectName = projectName;
        Branch defaultBranch = new Branch(firstBranchName);
        branches.add(defaultBranch);
        Config.flavor = flavor;
        Config.isOfflineInstallation = isOfflineInstallation;
        setUsername();
        saveConfig();
        if(editSudoers) editSudoers();

    }

    public static void initConfig(boolean editSudoers) {
        Branch defaultBranch = new Branch("default");
        branches.add(defaultBranch);
        setUsername();
        saveConfig();
        if(editSudoers) editSudoers();
    }

    public static void setProjectName(String name) {
        projectName = name.replaceAll("\\s+", "_").toLowerCase();
        saveConfig();
    }
    private static void setUsername() {
        try {
            if (RunCommand.getUID() == 0) username = RunCommand.withOut("echo $SUDO_USER")[0];
            else username = RunCommand.withOut("echo $USER")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static void editSudoers() {

        try {

            File sudoers = new File("/etc/sudoers");
            Scanner reader = new Scanner(sudoers);

            String content = "";
            while (reader.hasNextLine()) {
                content += reader.nextLine() + "\n";
            }

            if (content.contains(username)) {
                content.replaceAll("/^" + username + "/", "#" + username);
            }

            if(content.contains("Defaults:" + username)) {
                content.replaceAll("Defaults:" + username, "#Defaults:" + username);
            }

            content += "Defaults:" + username + " !requiretty\n";
            content += username + "\tALL=(ALL:ALL) NOPASSWD: " + RunCommand.getJarPath();

            PrintWriter pw = new PrintWriter(sudoers);
            pw.println(content);
            pw.flush();
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static JSONObject getGlobalConfigObj() {

        JSONObject obj = new JSONObject();
        obj.put("text_editor", textEditor);
        obj.put("project_name", projectName);
        obj.put("os_type", flavor);
        obj.put("offline_install", isOfflineInstallation);
        obj.put("dirty_download", dirtyDownload);

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
            String home = "";
            if(RunCommand.getUID() == 0) {
                home = RunCommand.withOut("su -c 'echo $HOME' " + username)[0];
            } else {
                home = RunCommand.withOut("echo $HOME")[0];
            }
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
