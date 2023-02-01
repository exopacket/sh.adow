package com.inteliense.shadow.shell;

import com.inteliense.shadow.models.*;
import com.inteliense.shadow.models.Package;
import com.inteliense.shadow.utils.RunCommand;
import com.inteliense.shadow.utils.SHA;

import java.io.File;
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

    public static void capture() {

        String pwd = RunCommand.getUserHome();

        startup();

        boolean exit = false;
        while (!exit) {

            System.out.print(ANSI_BLUE + getUsername() + ANSI_RESET + " : " + ANSI_PURPLE  + getDirFromPath(pwd) + " " + ((isCollecting) ? (ANSI_GREEN + "(collecting)") : (ANSI_RED + "(ignoring)")) + " " + ANSI_PURPLE + "$ " + ANSI_RESET);
            String input = scnr.nextLine().trim();
            if(input.equals("")) {
                showInfo();
                continue;
            }

            //parse syntax... more to do in the future
            String[] strings;
            String noStrings;
            String[] commands;
            try {
                strings = matches(input, "/(\"|'|`)(.*)(\\1)/");
                noStrings = macroize(input, "\34", strings);
                commands = matches(noStrings, "/[^\\\\];/");
            } catch (Exception e) {
                System.err.println("Command syntax error.");
                continue;
            }
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
                    case "install":
                        install(commandArr);
                        break;
                    case "edit":
                        edit(pwd, commandArr);
                        break;
                    case "ignore":
                        setCollecting(false);
                        break;
                    case "forget":
                        forget();
                        break;
                    case "last":
                        last(commandArr);
                        break;
                    case "continue":
                        setCollecting(true);
                        break;
                    case "export":
                    case "var":
                        variable(commandArr);
                        break;
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
                        ShellCommand commandObj = new ShellCommand(command, fixPath(pwd));
                        try {
                            InteractiveCommand cmd = new InteractiveCommand(SHA.getSha1("" + System.currentTimeMillis()), pwdCommand) {
                                @Override
                                public void inputReceived(String input) {
                                    commandObj.addInputValue(input);
                                }
                            };
                            String pwdOnSuccess = cmd.getPwd();
                            if(pwdOnSuccess != null) pwd = pwdOnSuccess;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(isCollecting) Config.getCurrent().add(commandObj);

                        break;

                }

                Config.saveConfig();

            }

        }

    }

    private static void edit(String pwd, String[] command) {
        String filepath = fixPath(pwd);
        if(command.length == 2) {
            try {
                String inputPath = command[1];
                if (!inputPath.startsWith("/")) filepath += inputPath;
                else filepath = inputPath;
                File file = new File(filepath);
                if (!file.exists()) file.createNewFile();
                RunCommand.editor(Config.textEditor, filepath);
                if(!isCollecting) return;
                Scanner reader = new Scanner(file);
                String content = "";
                while(reader.hasNextLine()) {
                    content += reader.nextLine() + "\n";
                }
                EditFile editedFile = new EditFile(filepath, content);
                Config.getCurrent().add(editedFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void forget() {
        System.out.println("Confirm. Are you sure you would like to forget every event executed in this branch?");
        System.out.print("Enter yes or no: ");
        String input = scnr.nextLine().trim().toLowerCase();
        if(input.equals("yes")) {
            System.out.println("\nYou have selected to forget every event in this branch. Confirm a second time to continue.");
            System.out.print("Type forget to confirm or press enter to cancel: ");
            input = scnr.nextLine().trim().toLowerCase();
            if(input.equals("forget")) {
                Config.getCurrent().clearEvents();
                System.out.println("\nAll events in the current branch have been deleted.");
                return;
            }
        }
        System.out.println("You have canceled the forget process and the events remain in storage.");
    }

    private static void last(String[] command) {
        if(Config.getCurrent().history().size() == 0) return;
        if(command.length == 1) {
            System.out.println(ANSI_GREEN + "LAST COMMAND" + ANSI_RESET);
            System.out.println("Type 'remove'/'rm' or 'delete'/'del' to ignore a command.");
            System.out.println("To show the next previous command, leave blank and press enter.");
            System.out.println("When finished, type 'exit'.");
            System.out.println();
            int curr = 1;
            while(true) {
                int index = Config.getCurrent().history().size() - curr;
                System.out.print(ANSI_BLUE + "[" + (index + 1) + "] " + ANSI_GREEN + Config.getCurrent().history().get(index).getHistoryString());
                System.out.print(ANSI_PURPLE + " $ " + ANSI_BLUE + ">> " + ANSI_RESET);
                String input = scnr.nextLine().trim();
                if(input.equals("")) {
                    if(index > 0) curr++;
                } else if(input.equals("exit")) {
                    break;
                } else if(input.equals("rm") || input.equals("remove") || input.equals("del") || input.equals("delete")) {
                    Config.getCurrent().history().remove(index);
                    if(Config.getCurrent().history().size() == 0) break;
                    if(curr > 1) curr--;
                }
            }
            System.out.println();
        } else if(command.length == 2) { //last [#]

        } else {
            printHelp();
        }
    }

    private static void install(String[] packages) {
        for(int i=1; i<packages.length; i++) {

            String packageName = packages[i];
            if(isCollecting) {
                Package pkg = new Package(packageName);
                Config.getCurrent().add(pkg);
            }

        }
    }

    private static void branch() {

        System.out.println(ANSI_GREEN + "CREATE NEW BRANCH" + ANSI_RESET);

        System.out.print("Would you like to continue creating a new branch? [yes|no]: ");
        String input = scnr.nextLine().trim().toLowerCase();
        if(input.equals("no")) return;

        System.out.println("\nYour current branch is " + ANSI_CYAN + Config.getCurrent().getName() + ANSI_RESET + " (" + Config.getCurrent().getId() + ")");
        System.out.println(ANSI_RESET + "" + Config.getCurrent().history().size() + " events");
        System.out.println(ANSI_YELLOW + "Notes: " + ANSI_RESET + Config.getCurrent().getNotes());
        System.out.println();

        System.out.print("Enter more notes or leave blank to continue: ");
        String notes = scnr.nextLine().trim();
        if(!notes.equals("")) Config.getCurrent().appendNotes(notes);

        String branchName = "";
        while(true) {
            System.out.print("Enter the new branch name: ");
            branchName = scnr.nextLine().trim();
            if(!branchName.equals("")) break;
        }

        String newNotes = "";
        System.out.print("Enter notes for the new branch: ");
        newNotes = scnr.nextLine();

        Config.createBranch(branchName, newNotes);

        System.out.println(ANSI_GREEN + "\nYou're now on the new branch '" + Config.getCurrent().getName() + "' (" + Config.getCurrent().getId() + ")" + ANSI_RESET + "\n");

    }

    private static void variable(String[] command) {
        String[] variable = null;
        if(command.length == 4) {
            variable = command;
        } else if(command.length < 4) {
            String commandStr = "";
            for(int i=0; i<command.length; i++) {
                commandStr += command[i];
            }
            commandStr.replace("=", " = ");
            variable = commandStr.split("\\s+");
        }
        if(variable.length == 4) {
            if(isCollecting) {
                Variable var = new Variable(variable[1], variable[3]);
                Config.getCurrent().add(var);
            }
        } else {
            System.err.println("Invalid variable usage.");
            System.err.println("example:");
            System.err.println("var name = 'value'");
            System.err.println("export name = 'value'");
        }
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
        System.out.println("\n" + ANSI_PURPLE + "You are using the " + ANSI_RED + "sh.adow" + ANSI_PURPLE + " interactive shell.\n" + ANSI_RESET);
    }

    private static void printHelp() {
        System.out.println("printHelp");
    }

    private static String getUsername() {
        return System.getProperty("user.name");
    }

    private static String getDirFromPath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

}
