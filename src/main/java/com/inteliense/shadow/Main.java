package com.inteliense.shadow;

import com.inteliense.shadow.classes.*;
import com.inteliense.shadow.classes.Package;
import com.inteliense.shadow.shell.InteractiveCommand;
import com.inteliense.shadow.shell.InteractiveShell;
import com.inteliense.shadow.utils.RunCommand;
import com.inteliense.shadow.utils.SHA;

import java.util.Scanner;

public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {

        if(args.length > 1) {

            if(args[0].equals("cli")) {

                switch(args[1]) {
                    case "capture":
                        loadConfig();
                        startInteractiveShell();
                        break;
                    case "list":
                        if(args.length < 3) {
                            printHelp();
                            System.exit(0);
                        }
                        loadConfig();
                        list(args);
                        break;
                    case "project":
                        loadConfig();
                        project(args);
                        break;
                    case "bootstrapper":
                        loadConfig();
                        bootstrapper();
                        break;
                    case "configure":
                        superuser();
                        interactiveConfigure();
                        break;
                    case "configure-defaults":
                        superuser();
                        if(args.length == 3) {
                            configure(args[2].equals("--with-sudo"));
                        } else configure(false);
                        break;
                    case "install":
                        if(args.length < 3) {
                            printHelp();
                            System.exit(0);
                        }
                        loadConfig();
                        install(args);
                        break;
                    case "exec":
                        if(args.length < 3) {
                            printHelp();
                            System.exit(0);
                        }
                        loadConfig();
                        exec(args);
                        break;
                    default:
                        printHelp();
                        break;
                }

                System.exit(0);

            }

        }

    }

    private static void exec(String[] args) {

        String command = "";
        for(int i=2; i<args.length; i++) {
            command += command + " ";
        }

        String pwd = RunCommand.getUserHome();

        String pwdCommand = "cd " + fixPath(pwd) + " && " + command;
        ShellCommand commandObj = new ShellCommand(command, fixPath(pwd));
        try {

            InteractiveCommand cmd = new InteractiveCommand(SHA.getSha1("" + System.currentTimeMillis()), pwdCommand) {
                @Override
                public void inputReceived(String input) {
                    commandObj.addInputValue(input);
                }
            };

        } catch (Exception e) {
            e.printStackTrace();
        }

        Config.getCurrent().add(commandObj);

    }

    private static void list(String[] args) {
        if(args[2].equals("projects")) {
            System.out.println(ANSI_GREEN + "ALL LOCAL PROJECTS" + ANSI_RESET);
        } else if(args[2].equals("branches")) {
            System.out.println(ANSI_GREEN + "ALL BRANCHES IN PROJECT '" + Config.projectName.toUpperCase() + "'" + ANSI_RESET);
            System.out.println();
            for(int i=0; i<Config.branches.size(); i++) {
                Branch branch = Config.branches.get(i);
                System.out.println(ANSI_PURPLE + "[" + (i + 1) + "]\t" + ANSI_CYAN + branch.getName() + " (" + branch.getId() + ")");
                System.out.println(ANSI_YELLOW + "\tNotes: " + branch.getNotes() + ANSI_RESET);
            }
        }
    }

    private static void bootstrapper() {
        Bootstrapper.configure();
    }

    private static void install(String[] args) {
        for(int i=2; i<args.length; i++) {
            Package pkg = new Package(args[i]);
            Config.getCurrent().add(pkg);
        }
    }

    private static void project(String[] args) {
        if(args.length == 3) {
            String projectName = args[2];
            //if exist, change to that project
            //else automatically create it
        } else {
            //prompt to create a new project
        }
    }

    private static void loadConfig() {
        if(!Config.loadConfig()) System.exit(3);
    }

    private static void configure(boolean editSudoers) {
        Config.initConfig(editSudoers);
    }

    private static void interactiveConfigure() {

        Scanner scnr = new Scanner(System.in);

        System.out.println("sh.adow | configuration");
        System.out.println();
        String flavor;
        String textEditor;
        String projectName;
        String branchName;
        String isOffline;

        while (true) {
            System.out.print("Enter your Linux base OS type [fedora|debian]: ");
            flavor = scnr.nextLine().toLowerCase();
            if(flavor.equals("debian") || flavor.equals("fedora")) break;
            else System.err.println("Invalid entry.");
        }

        System.out.print("Enter the name of your preferred text editor: ");
        textEditor = scnr.nextLine().toLowerCase();

        while (true) {
            System.out.print("Would you like to download packages for offline installations? [yes|no]: ");
            isOffline = scnr.nextLine().toLowerCase();
            if(isOffline.equals("yes") || isOffline.equals("no")) break;
            else System.err.println("Invalid entry.");
        }

        System.out.print("Enter the name of your initial project: ");
        projectName = scnr.nextLine().toLowerCase();

        System.out.print("Enter the name of your initial branch: ");
        branchName = scnr.nextLine().toLowerCase();

        System.out.print("Update sudoers file for sudo support? [yes/no]: ");
        String val = scnr.nextLine().toLowerCase().trim();

        Config.initConfig(textEditor, projectName, branchName, flavor, isOffline.equals("yes"), val.equals("yes"));

    }

    private static void printHelp() {

        System.err.println("Invalid usage");
        System.exit(1);

    }

    private static void superuser() {

        if(RunCommand.getUID() != 0) {
            System.err.println("You must run this command as the root user.");
            System.exit(2);
        }

    }

    private static void startInteractiveShell() {
        InteractiveShell.capture();
    }

    private static String fixPath(String in) {
        if(!in.endsWith("/")) return in + "/";
        return in;
    }

}
