package com.inteliense.shadow.models;

import com.inteliense.shadow.utils.RunCommand;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.inteliense.shadow.models.Config.projectName;

public class Package extends Event {

    private String name;
    private int index = -1;
    private ArrayList<String> dependencies = new ArrayList<String>();

    public Package(String name) {
        super();
        this.name = name;
        this.index = Config.getCurrent().size();
        downloadWithDependencies(name);
    }

    public Package(JSONObject obj) {

        super();

        this.index = (int) obj.get("index");
        this.name = (String) obj.get("value");

        JSONArray filesArr = (JSONArray) obj.get("files");
        for(int i=0; i< filesArr.size(); i++) {
            JSONObject file = (JSONObject) filesArr.get(i);
            this.dependencies.add((String) file.get("value"));
        }

    }

    public String getHistoryString() {
        return "install " + name;
    }

    public String getType() {
        return "InstallPackage";
    }

    public JSONObject getObject() {

        JSONObject obj = new JSONObject();
        obj.put("index", this.index);
        obj.put("type", "pkg");
        obj.put("value", this.name);

        JSONArray filesArr = new JSONArray();
        for(int i=0; i< dependencies.size(); i++) {
            JSONObject file = new JSONObject();
            file.put("index", i);
            file.put("value", dependencies.get(i));
            filesArr.add(file);
        }

        obj.put("files", filesArr);

        return obj;

    }

    private void downloadWithDependencies(String name) {

        if(checkInstalled(name)) {
            if(!Config.getInstalled().contains(name)) {
                Config.getInstalled().add(name);
                return;
            }
        }

        ArrayList<String> allDependencies = new ArrayList<String>();
        allDependencies = parseDependencies(listDependencies(name), allDependencies);
        allDependencies.add(name);
        for(int x=0; x<allDependencies.size(); x++) {
            String dependency = allDependencies.get(x);
            if(checkInstalled(dependency)) continue;
            String[] filenames = downloadPackage(dependency);
            boolean toAdd = true;
            if(filenames.length == 1)
                if(filenames[0].trim().equals("")) continue;
            for(int k=0; k<filenames.length; k++) {
                dependencies.add(filenames[k]);
                if(toAdd) {
                    toAdd = false;
                    if(!Config.getInstalled().contains(dependency)) {
                        Config.getInstalled().add(dependency);
                    }
                }
                copyPackage(filenames[k]);
            }
        }
    }

    private static String[] downloadPackage(String packageName) {

        try {
            if(Config.flavor.equals("debian")) {
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
            if(Config.flavor.equals("debian")) {
                String path = "/var/cache/apt/archives/" + filename;
                String toPath = Config.getConfigDir() + projectName + "/packages/";
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
            if(Config.flavor.equals("debian"))
                return RunCommand.withOut("apt-cache depends " + packageName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private static boolean checkInstalled(String packageName) {

        try {

            if (Config.flavor.equals("debian")) {
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
    
}
