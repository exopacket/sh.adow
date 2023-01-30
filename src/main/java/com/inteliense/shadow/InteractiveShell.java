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

    private static String branchName;

    public static void capture() throws IOException {

        branchName = "branch_" + SHA.getSha1("" + System.currentTimeMillis());
        String pwd = RunCommand.getUserHome();

        startup();

        boolean exit = false;
        while (!exit) {

            System.out.print(ANSI_BLUE + "/bin/sh " + ((isCollecting) ? (ANSI_GREEN + "(collecting)") : (ANSI_RED + "(ignoring)")) + " " + ANSI_PURPLE + "$ " + ANSI_RESET);
            String input = scnr.nextLine().trim();
            if(input.equals("")) {
                showInfo();
                continue;
            }

            //parse syntax... more to do in the future
            String[] strings = matches(input, "/(\"|'|`)(.*)(\\1)/");
            String noStrings = macroize(input, "\34", strings);
            String[] commands = matches(noStrings, "/[^\\\\];/");
            ArrayList<String[]> list = new ArrayList<>();

            for(int i=0; i< commands.length; i++) {
                list.add(commands[i].split("\\s+"));
            }

            String[][] arr = list.toArray(String[][]::new);

            int curr = 0;
            for(int i=0; i< arr.length; i++) {
                for(int x=0; x<arr[i].length; x++) {
                    if(arr[i][x].equals("\34")) {
                        arr[i][x] = strings[curr];
                        curr++;
                    }
                }
            }

            for(int i=0; i<arr.length; i++) {

                String[] commandArr = arr[i];
                String first = commandArr[0];
                String command = "";
                for(int x=0; x< commandArr.length; x++) command += commandArr[x] + " ";

                switch(first) {

                    case "edit":
                        edit(pwd, commandArr);
                        break;
                    case "ignore":
                        setCollecting(false);
                        break;
                    case "forget":
                        forget(commandArr);
                        break;
                    case "last":
                        last(commandArr);
                        break;
                    case "continue":
                        setCollecting(true);
                        break;
//                    case "export":
//                    case "var":
//                        variable(commandArr);
//                        break;
                    case "branch":
                        branch();
                        break;
                    case "exit":
                        exit = true;
                        break;
                    case "help":
                        printHelp();
                        break;

                    default:

                        String pwdCommand = "cd " + fixPath(pwd) + " && " + command;
                        try {
                            InteractiveCommand cmd = new InteractiveCommand(SHA.getSha1("" + System.currentTimeMillis()), pwdCommand) {
                                @Override
                                public void inputReceived(String input) {
                                    //TODO add to config
                                }
                            };
                            pwd = cmd.getPwd();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(isCollecting) history.add(command);

                        break;

                }

            }

        }

        saveCapture(branchName);

    }

    private static void edit(String pwd, String[] command) {

    }
    private static void forget(String[] command) {
        System.out.println("Confirm. Are you sure you would like to forget every command executed from this branch?");
    }
    private static void last(String[] command) {
        if(history.size() == 0) return;
        if(command.length == 1) {
            System.out.println(" -- Last command -- ");
            System.out.println("       " + history.get(history.size() - 1));
            System.out.println(" Type remove to remove or leave blank to return to the shell >> ");
        } else if(command.length == 2) { //last [#]

        } else {
            printHelp();
        }
    }
    private static void branch() {
        System.out.print("\nYou've selected to start a new branch." +
                "\nEnter a name for the branch you're ending (leave blank to cancel): ");
        String prevBranch = scnr.nextLine().trim().toLowerCase().replaceAll("\\s+", "_");
        if(prevBranch.equals("")) {
            System.out.println("You have chosen to cancel switching to a new branch.");
            return;
        }
        System.out.print("Enter a name for the branch you're starting (leave blank to cancel): ");
        String currBranch = scnr.nextLine().trim().toLowerCase().replaceAll("\\s+", "_");
        if(currBranch.equals("")) {
            System.out.println("You have chosen to cancel switching to a new branch.");
            return;
        }
        saveCapture(prevBranch);
        System.out.println("\nThe previous branch has been saved and a new branch has started.\n");
        branchName = currBranch;
    }
    private static void variable(String[] command) {
        System.out.println("var");
    }

    private static void saveCapture(String branch) {
        System.out.println("saveCapture");
    }

    private static void setCollecting(boolean val) {
        isCollecting = val;
        if(val) {
            System.out.println("\n" + ANSI_GREEN + "You have resumed collecting command information.\nAll commands will be logged." + ANSI_RESET + "\n");
        } else {
            System.out.println("\n" + ANSI_CYAN + "You have paused collecting command information.\nAll commands will be ignored." + ANSI_RESET + "\n");
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
        if(matches.size() == 0) matches.add(in);
        return matches.toArray(new String[matches.size()]);

    }

    private static String fixPath(String in) {
        if(!in.endsWith("/")) return in + "/";
        return in;
    }

    private static void showInfo() {
        System.out.println("showInfo");
    }

    private static void startup() {
        System.out.println("\nYou are using the sh.adow interactive shell.\n\n");
    }

    private static void printHelp() {
        System.out.println("printHelp");
    }

}
