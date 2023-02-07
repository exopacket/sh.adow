package com.inteliense.shadow.classes;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

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

            Scanner scnr = new Scanner(System.in);

            String outPath = "";
            if(args.length == 4) {
                outPath = args[3];
            } else {
                System.out.print("Enter the directory to place the new files: ");
                outPath = scnr.nextLine();
            }

            outPath = Config.fixPath(outPath) + Config.projectName;
            ArrayList<Branch> branches = Config.branches;

            for(int i=0; i<branches.size(); i++) {
                createShellFile(outPath, branches.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void createShellFile(String outPath, Branch branch) throws Exception {

        File dir = new File(Config.fixPath(outPath));
        if(!dir.exists()) dir.mkdirs();

        File file = new File(Config.fixPath(outPath) + branch.getName() + ".sh");
        System.out.println(Config.fixPath(outPath) + branch.getName() + ".sh");
        PrintWriter pw = new PrintWriter(file);

        pw.println("#!/bin/bash");
        pw.println();
        pw.println("DIR=${1:-$(pwd)}");
        pw.println("INSTALL_USER=${SUDO_USER:-$USER}");
        //pw.println("ls store && tar -xzf store.tar.gz");

        ArrayList<Event> events = branch.getEventList();
        for(int x=0; x<events.size(); x++) {
            Event event = events.get(x);
            String[] code = event.getShellCode(new String[]{});
            for(int y=0; y<code.length; y++) {
                pw.println(code[y]);
            }
        }

        pw.flush();
        pw.close();

    }

    private static void createStore(String outPath, Branch branch) {



    }

    private static void printHelp() {

    }

}
