package com.inteliense.shadow;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InteractiveShell {

    private static Scanner scnr = new Scanner(System.in);

    private static boolean isCollecting = true;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static ArrayList<String> history = new ArrayList<String>();

    public static void main() {

        String pwd = "";

        startup();

        boolean exit = false;
        while (!exit) {

            System.out.print("/bin/sh (" + ((isCollecting) ? "collecting" : "paused") + ") $ ");
            String input = scnr.nextLine().trim();
            if(input.equals("")) {
                showInfo();
                continue;
            }

            //parse syntax... more to do in the future
            String[] strings = matches(input, "(\"|'|`)(.*)(\\1)");
            String noStrings = macroize(input, "\34", strings);
            String[] commands = matches(noStrings, "[^\\];");

            int curr = 0;
            for(int i=0; i<commands.length; i++) {
                while(commands[i].indexOf(strings[curr]) >= 0) {
                    commands[i].replaceFirst("\34", strings[curr]);
                    curr++;
                }
            }

            for(int i=0; i<commands.length; i++) {

                String command = commands[i];
                String[] parts = command.split("\\s+");
                String first = parts[0];

                switch(first) {

                    case "edit":
                        break;
                    case "ignore":
                        break;
                    case "forget":
                        break;
                    case "pause":
                        setCollecting(false);
                        break;
                    case "last":
                        break;
                    case "continue":
                        setCollecting(true);
                        break;
                    case "var":
                        break;
                    case "exit":
                        break;
                    case "help":
                        printHelp();
                        break;

                    default:

                        String pwdCommand = "cd " + fixPath(pwd) + " && {" + command + "}; && echo \"#!\" && pwd";
                        try {
                            pwd = RunCommand.interactive(pwdCommand);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(isCollecting) history.add(command);

                        break;

                }

            }

        }

    }

    private static void setCollecting(boolean val) {
        isCollecting = val;
        if(val) {
            System.out.println("\n\n" + ANSI_RED + "You have resumed collecting command information.\nAll commands will be logged." + ANSI_RESET);
        } else {
            System.out.println("\n\n" + ANSI_CYAN + "You have paused collecting command information.\nAll commands will be ignored." + ANSI_RESET);
        }
    }

    private static String macroize(String in, String replacement, String[] matches) {
        String out = in;
        for(int i=0; i<matches.length; i++) {
            String match = matches[i];
            out.replaceFirst(match, replacement);
        }
        return out;
    }

    private static String[] matches(String in, String pattern) {

        ArrayList<String> matches = new ArrayList<String>();
        Matcher m = Pattern.compile(pattern).matcher(in);
        while(m.find()) matches.add(m.group());
        return matches.toArray(new String[0]);

    }

    private static String fixPath(String in) {
        if(!in.endsWith("/")) return in + "/";
        return in;
    }

    private static void showInfo() {

    }

    private static void startup() {
        System.out.println("\nYou are using the sh.adow interactive shell.\n\n");
    }

    private static void printHelp() {

    }

}
