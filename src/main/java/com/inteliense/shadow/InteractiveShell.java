package com.inteliense.shadow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class InteractiveShell {

    private static Scanner scnr = new Scanner(System.in);

    public static void main() {

        String pwd = "";

        startup();

        boolean exit = false;
        while(!exit) {

            //TODO change nextLine() to next() for grabbing arrows, delete, enter, etc.
            //TODO add custom functions

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


    private static String fixPath(String in) {
        if(!in.endsWith("/")) return in + "/";
        return in;
    }

    private static void startup() {
        System.out.println("\nYou are using the sh.adow interactive shell.\n\n");
    }

}
