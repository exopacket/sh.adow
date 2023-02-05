package com.inteliense.shadow.classes;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Bootstrapper {


    public static void prepare(String[] args) {

        if(args[3].equals("cmd-list")) {

        } else if(args[3].equals("cmd-skip")) {

        } else printHelp();

    }

    public static void merge(String[] args) {



    }

    public static void export(String[] args) {

        try {

            String outPath = args[3];
            File file = new File(outPath);
            PrintWriter pw = new PrintWriter(file);

            ArrayList<Branch> branches = Config.branches;
            ArrayList<String> toOverlook = Config.overlookList;
            String[] _args = new String[10];

            pw.println("#!/bin/bash");

            for(int i=0; i<branches.size(); i++) {
                Branch branch = branches.get(i);
                ArrayList<Event> events = branch.getEventList();
                for(int x=0; x<events.size(); x++) {
                    Event event = events.get(x);
                    String[] code = event.getShellCode(_args);
                    for(int y=0; y<code.length; y++) {
                        pw.println(code[y]);
                    }
                }
            }

            pw.flush();
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void printHelp() {

    }

}
