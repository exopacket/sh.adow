package com.inteliense.shadow.shell;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public abstract class InteractiveCommand {

    private ProcessBuilder builder;
    private BufferedWriter writer;
    private BufferedReader stdout;
    private BufferedReader stderr;
    private boolean closed = false;
    private Scanner scnr;
    private String dirId;


    public InteractiveCommand(String dirId, String cmd) {
        try {

            this.scnr = new Scanner(System.in);
            this.dirId = dirId;

            builder = new ProcessBuilder();
            builder.command("/bin/sh", "-c", cmd + " ; echo $(pwd) > /tmp/" + dirId);
            Process process = builder.start();

            stdout = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            stderr = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            watch();
            process.waitFor(1000, TimeUnit.MILLISECONDS);
            writer.close();
            stdout.close();
            stderr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPwd() {
        try {
            File file = new File("/tmp/" + dirId);
            if(!file.exists()) return null;
            Scanner reader = new Scanner(file);
            String res = (reader.hasNextLine()) ? reader.nextLine() : null;
            file.delete();
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void inputReceived(String input);

    private Thread print(BufferedReader reader, boolean isError) {
        return new Thread(() -> {
            try {
                int c;
                while ((c = reader.read()) != -1) {
                    if ((isError)) {
                        System.err.print((char) c);
                    } else {
                        System.out.print((char) c);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            closed = true;
        });
    }

    private void watch() throws InterruptedException {

        Thread inputThr = new Thread(() -> {
            try {
                while(!closed) {
                    while (System.in.available() == 0 && !closed) Thread.sleep(250);
                    if (closed) break;
                    while (scnr.hasNextLine()) {
                        String input = scnr.nextLine();
                        if(input.trim().toLowerCase().equals("exit")) {
                            closed = true;
                            break;
                        } else {
                            writer.write(input);
                            writer.newLine();
                            writer.flush();
                            inputReceived(input);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread watchThr = new Thread(() -> {
            try {
                while(!closed) {
                    Thread.sleep(250);
                }
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread outputThr = print(stdout, false);
        Thread errThr = print(stderr, true);

        outputThr.start();
        inputThr.start();
        watchThr.start();
        errThr.start();
        watchThr.join();
        inputThr.stop();
        outputThr.stop();
        errThr.stop();

    }

}
