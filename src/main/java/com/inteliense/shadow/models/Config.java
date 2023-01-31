package com.inteliense.shadow.models;

import com.inteliense.shadow.utils.RunCommand;

import java.util.ArrayList;

public class Config {

    public static String textEditor = "vi";
    public static String projectName = "";
    private static int currentBranch = 0;
    public static ArrayList<Branch> branches;
    public static String flavor = "debian";

    public static ArrayList<String> installed = new ArrayList<String>();

    public static void loadConfig() {

    }

    public static void saveConfig() {

    }

    public static Branch getCurrent() {

        return null;

    }

    public static ArrayList<String> getInstalled() {
        return null;
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
