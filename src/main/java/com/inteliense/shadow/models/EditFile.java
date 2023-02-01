package com.inteliense.shadow.models;

import com.inteliense.shadow.utils.SHA;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class EditFile extends Command {

    private String filepath;
    private String savedName;
    private int index = -1;

    public EditFile(String filepath, String content) {
        this.filepath = filepath;
        this.index = Config.getCurrent().size();
        try {
            saveFile(content);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveFile(String content) throws FileNotFoundException {
        this.savedName = SHA.getSha1("file_" + System.currentTimeMillis());
        File file = new File("" + savedName);
        PrintWriter pw = new PrintWriter(file);
        pw.print(content);
        pw.flush();
        pw.close();
    }
    public String getType() {
        return "EditedFile";
    }

    public JSONObject getObject() {

        JSONObject obj = new JSONObject();
        obj.put("index", this.index);
        obj.put("type", "file");
        obj.put("path", this.filepath);
        obj.put("saved_name", this.savedName);

        return obj;

    }

}
