package com.inteliense.shadow;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static ArrayList<String> installed = new ArrayList<String>();
    private static String flavor = "debian";
    private static String projectName = "";
    public static void main(String[] args) {

        if(args.length >= 1) {
            if(args[0].equals("cli")) {
                switch(args[1]) {
                    case "list":
                        listProjects();
                        break;
                }
                System.exit(0);
            }
        }

        if(args.length >= 2) {
            if(fileCheck(args[0])) {
                switch(args[1]) {
                    // TODO possibly add support for editing, copying, and removing files
                    case "install":
                        superuser();
                        downloadPackageWithDependencies(args);
                        break;
                    case "exec":
                        executeCommand(args);
                        break;
                    default:
                        printHelp();
                        break;
                }
            } else projectNotFound(args[0], args);
        } else printHelp();

    }

    private static void downloadPackageWithDependencies(String[] args) {

        JSONObject config = getConfig(args[0]);
        projectName = args[0];

        for(int i=2; i<args.length; i++) {
            String packageName = args[i];
            if(checkInstalled(packageName)) {
                if (!installed.contains(packageName)) installed.add(packageName);
            } else {
                ArrayList<String> allDependencies = new ArrayList<String>();
                allDependencies = parseDependencies(listDependencies(packageName), allDependencies);
                allDependencies.add(packageName);
                for(int x=0; x<allDependencies.size(); x++) {
                    String dependency = allDependencies.get(x);
                    String[] filenames = downloadPackage(dependency);
                    boolean toAdd = true;
                    if(filenames.length == 1)
                        if(filenames[0].trim().equals("")) continue;
                    for(int k=0; k<filenames.length; k++) {
                        JSONArray configArr = (JSONArray) config.get("entries");
                        JSONArray installedArr = (JSONArray) config.get("installed");
                        JSONObject commandObj = new JSONObject();
                        commandObj.put("index", configArr.size());
                        commandObj.put("type", "pkg");
                        commandObj.put("value", filenames[k]);
                        configArr.add(commandObj);
                        if(toAdd) {
                            toAdd = false;
                            if(!installed.contains(dependency)) {
                                installedArr.add(dependency);
                                installed.add(dependency);
                            }
                        }
                        copyPackage(filenames[k]);
                        saveConfig(config);
                    }
                }
            }

        }

    }

    private static String[] downloadPackage(String packageName) {

        try {
            if(flavor.equals("debian")) {
                RunCommand.runAndWait("rm /var/cache/apt/archives/*.deb");
                RunCommand.runAndWait("apt install --download-only " + packageName);
                //RunCommand.runAndWait("og-apt install --download-only " + packageName);
                return RunCommand.withOut("ls /var/cache/apt/archives | grep .deb");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private static void copyPackage(String filename) {
        try {
            if(flavor.equals("debian")) {
                String path = "/var/cache/apt/archives/" + filename;
                String toPath = getConfigDir() + projectName + "/packages/";
                File dir = new File(toPath);
                if(!dir.exists()) dir.mkdir();
                RunCommand.runAndWait("dpkg -i " + path);
                RunCommand.runAndWait("mv " + path + " " + toPath + filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] listDependencies(String packageName) {
        try {
            if(flavor.equals("debian"))
                return RunCommand.withOut("apt-cache depends " + packageName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private static boolean checkInstalled(String packageName) {

        try {

            if (flavor.equals("debian")) {
                return RunCommand.withOut("dpkg -s " + packageName).length > 2;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return false;

    }

    private static ArrayList<String> parseDependencies(String[] cmdOutput, ArrayList<String> dependencies) {

        for(int i=1; i<cmdOutput.length; i++) {
            String line = cmdOutput[i];
            if(line.contains("Depends:")) {
                String stripped = line.replaceAll("(Depends\\:)", "").replaceAll("[\\s\\|\\<\\>]", "");
                if(!checkInstalled(stripped)) {
                    dependencies.add(stripped);
                    dependencies = parseDependencies(listDependencies(stripped), dependencies);
                }
            }
        }

        return dependencies;

    }

    private static void executeCommand(String[] args) {

        JSONObject config = getConfig(args[0]);
        String fullCommand = "";
        for(int i=2; i<args.length; i++) {
            String arg = args[i];
            if(!arg.contains("\\ ") && arg.contains(" "))
                arg = "\"" + arg + "\"";
            fullCommand += ((i > 2) ? " " : "") + arg;
        }
        try {
            RunCommand.streamOut(fullCommand);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JSONArray configArr = (JSONArray) config.get("entries");
        JSONObject commandObj = new JSONObject();
        commandObj.put("index", configArr.size());
        commandObj.put("type", "shell");
        commandObj.put("value", fullCommand);
        configArr.add(commandObj);
        saveConfig(config);

    }

    private static void saveConfig(JSONObject object) {
        String projectName = (String) object.get("project_name");
        String content = JSON.getString(object);
        try {
            File file = new File(getConfigDir() + projectName + "/" + projectName + ".conf");
            PrintWriter pw = new PrintWriter(file);
            pw.println(content);
            pw.flush();
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static JSONObject getConfig(String projectName) {

        try {
            File file = new File(getConfigDir() + projectName + "/" + projectName + ".conf");
            Scanner scnr = new Scanner(file);
            String content = "";
            while (scnr.hasNextLine()) {
                content += scnr.nextLine();
            }
            JSONObject obj = JSON.getObject(content);
            JSONArray installedArr = (JSONArray) obj.get("installed");
            for(int i=0; i<installedArr.size(); i++) {
                installed.add((String) installedArr.get(i));
            }
            return obj;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return null;
        }

    }

    private static boolean fileCheck(String projectName) {
        return new File(getConfigDir() + projectName + "/" + projectName + ".conf").exists();
    }

    private static String getConfigDir() {
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

    private static String fixPath(String in) {
        if(!in.endsWith("/")) return in + "/";
        return in;
    }

    private static void projectNotFound(String projectName, String[] args) {

        Scanner scnr = new Scanner(System.in);
        System.out.println("The sh.adow project '" + projectName + "' was not found.");
        System.out.print("Would you like to create it? [Y/N]: ");
        String input = scnr.nextLine().replaceAll("\\s", "").toUpperCase();
        if(input.equals("Y") || input.equals("YES")) {
            try {
                File dir = new File(getConfigDir() + projectName);
                if (!dir.exists()) dir.mkdirs();
                File config = new File(getConfigDir() + projectName + "/" + projectName + ".conf");
                if (!config.exists()) {
                    config.createNewFile();
                    PrintWriter pw = new PrintWriter(config);
                    pw.println("{\"project_name\": \"" + projectName + "\", \"installed\": [], \"entries\": []}");
                    pw.flush();
                    pw.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            main(args);
        } else System.exit(0);

    }

    private static void printHelp() {

        System.err.println("Invalid usage");
        System.exit(1);

    }

    private static void listProjects() {
        try {
            RunCommand.streamOut("ls " + getConfigDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void superuser() {

        if(RunCommand.getUID() != 0) {
            System.err.println("You must run this command as the root user.");
            System.exit(2);
        }

    }

}
