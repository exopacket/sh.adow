package com.inteliense.shadow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class InteractiveShell {

    private static Scanner scnr = new Scanner(System.in);

    private static ArrayList<String> history = new ArrayList<String>();

    public static void main() {

        String pwd = "";

        startup();

        boolean exit = false;
        while(!exit) {

            //TODO change nextLine() to next() for grabbing arrows, delete, enter, etc.
            //TODO add custom functions (ex: 'edit' to open default text editor for saving the contents)

            //edit [filename]: opens the file in your editor & saves changes for use on another machine
            //ignore [command]: ignores all of a specific command (prompts for session or global use)
            //forget: forgets the current terminal session
            //pause: pauses collecting terminal commands
            //last: shows last command
            //last-[n]: shows last [n] commands
            //continue: continues collecting commands
            //exit: exit's the interactive shell

            System.out.print("/bin/sh (collecting) $ ");
            String input = scnr.nextLine();
            String command = "cd " + fixPath(pwd) + " && " + input + " && echo \"#!\" && pwd";
            try {
                pwd = RunCommand.interactive(command);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static String[] getCommand() {
        return null;
    }

    private static String fixPath(String in) {
        if(!in.endsWith("/")) return in + "/";
        return in;
    }

    private static void startup() {
        System.out.println("\nYou are using the sh.adow interactive shell.\n\n");
    }

}
