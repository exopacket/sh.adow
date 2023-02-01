package com.inteliense.shadow;

import com.inteliense.shadow.classes.*;
import com.inteliense.shadow.classes.Package;
import com.inteliense.shadow.shell.InteractiveCommand;
import com.inteliense.shadow.shell.InteractiveShell;
import com.inteliense.shadow.utils.RunCommand;
import com.inteliense.shadow.utils.SHA;

import java.io.IOException;
import java.sql.SQLOutput;
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
            System.out.println();
            String currentProject = Config.projectName;
            System.out.println(ANSI_CYAN + "[*] " + currentProject + ANSI_RESET);
            try {
                String[] projects = RunCommand.withOut("ls " + Config.getConfigDir());
                for(int i=0; i<projects.length; i++) {
                    if(projects[i].equals("global.conf") || projects[i].equals(currentProject)) continue;
                    System.out.println(ANSI_PURPLE + "[-] " + projects[i] + ANSI_RESET);
                }
            } catch (Exception e) {
                System.err.println("Error retrieving projects...");
            }
        } else if(args[2].equals("branches")) {
            System.out.println(ANSI_GREEN + "ALL BRANCHES IN PROJECT '" + Config.projectName.toUpperCase() + "'" + ANSI_RESET);
            System.out.println();
            String currentBranch = Config.getCurrent().getId();
            for(int i=0; i<Config.branches.size(); i++) {
                Branch branch = Config.branches.get(i);
                if(branch.getId().equals(currentBranch)) {
                    System.out.println(ANSI_CYAN + "[*]\t" + branch.getName() + " (" + branch.getId() + ")");
                    System.out.println(ANSI_YELLOW + "\tNotes: " + branch.getNotes() + ANSI_RESET);
                } else {
                    System.out.println(ANSI_PURPLE + "[-]\t" + branch.getName() + " (" + branch.getId() + ")");
                    System.out.println(ANSI_YELLOW + "\tNotes: " + branch.getNotes() + ANSI_RESET);
                }
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
            boolean found = false;
            try {
                String[] projects = RunCommand.withOut("ls " + Config.getConfigDir());
                for(int i=0; i<projects.length; i++) {
                    if(projects[i].equals("global.conf")) continue;
                    if(projects[i].equals(projectName)) {
                        found = true;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error retrieving projects...");
            }
            Config.setProjectName(projectName);
            if(found) {
                System.out.println(ANSI_GREEN + "Switched to the project '" + projectName + "'" + ANSI_RESET);
            } else {
                System.out.println(ANSI_GREEN + "Created and switched to the project '" + projectName + "'" + ANSI_RESET);
            }
        } else {
            Scanner scnr = new Scanner(System.in);
            System.out.println("Current project: " + Config.projectName + "\n");
            System.out.print("Would you like to create a new project? [yes|no]: ");
            String answer = scnr.nextLine().trim().toLowerCase();
            if(answer.equals("no")) return;
            System.out.print("Enter a name for the new project: ");
            String projectName = scnr.nextLine().trim();
            Config.setProjectName(projectName);
            System.out.println(ANSI_GREEN + "Created and switched to the project '" + projectName + "'" + ANSI_RESET);
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
