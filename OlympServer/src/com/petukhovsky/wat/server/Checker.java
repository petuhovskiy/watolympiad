package com.petukhovsky.wat.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

/**
 * Created by petuh_000 on 02.10.2014.
 */
public class Checker {
    public static final String RES_DIR = "c:/111/";
    public static final String CHECK_DIR = RES_DIR + "test/";

    public static void clearDir(String path) {
        File dir = new File(path);
        dir.mkdir();
        for (String s : dir.list()) {
            File f = new File(path + "/" + s);
            if (f.isDirectory()) {
                clearDir(path + "/" + s);
            }
            f.delete();
        }
    }

    public static String compile(Source source, String outputFile) {
        Runtime r = Runtime.getRuntime();
        String command = "";
        if (source.getLanguage() == 0) {
            command = "c:/fpc/2.6.4/bin/i386-win32/fpc.exe -O2 -Xs -Sgic -viwn -Cs67107839 -Mdelphi -XS " + source.getLocation() + " -o" + outputFile;
        } else if (source.getLanguage() == 1) {
            command = "\"c:/Program Files (x86)/CodeBlocks/MinGW/bin/g++.exe\" -static -fno-optimize-sibling-calls -fno-strict-aliasing -lm -s -x c++ -Wl,--stack=268435456 -O2 -o " + outputFile + " " + source.getLocation();
        }
        String res = "";
        try {
            Process p = r.exec(command);
            Worker worker = new Worker(p);
            worker.start();
            try{
                worker.join(20000);
            } catch (Exception e) {
            }
            if (worker.getExit() == null) {
                p.destroy();
                return "Превышено время компиляции программы";
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = br.readLine();
            while (s != null) {
                res += s;
                s = br.readLine();
                if (s != null) res += "<br>";
            }
            br.close();
        } catch (Exception e) {
        }
        return res;
    }

    public static RunResult runexe(String timeLimit, String memoryLimit) {
        Runtime r = Runtime.getRuntime();
        try {
            Process p = r.exec(RES_DIR + "runexe.exe -t " + timeLimit + " -m " + memoryLimit + " -d " + CHECK_DIR + " -l test -p test " + CHECK_DIR + "solve.exe");
            p.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s[] = new String[5];
            for (int i = 0; i < 5; i++) {
                s[i] = br.readLine();
                Log.d(s[i]);
            }
            int exitcode = -1;
            String time = s[3].substring(14).trim();
            String memory = s[4].substring(14).trim();
            if (s[0].trim().equals("Program successfully terminated")) {
                if (s[1].substring(12).trim().equals("0")) exitcode = 0;
                else exitcode = 4;
            }
            else if (s[0].trim().equals("Idleness limit exceeded")) exitcode = 1;
            else if (s[0].trim().equals("Memory limit exceeded")) exitcode = 2;
            else if (s[0].trim().equals("Time limit exceeded")) exitcode = 3;
            else if (s[0].trim().equals("Security violation")) exitcode = 5;
            else Log.e("runexe result: " + s[0]);
            br.close();
            return new RunResult(exitcode, time, memory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("runexe fail");
        return new RunResult(-1, "", "");
    }

    public static boolean tokenCheck(String in, String out, String res) {
        try {
            StringTokenizer s1 = new StringTokenizer(readFile(out));
            StringTokenizer s2 = new StringTokenizer(readFile(res));
            while (s1.hasMoreTokens() && s2.hasMoreTokens()) {
                if (!s1.nextToken().equals(s2.nextToken())) return false;
            }
            return s1.hasMoreTokens() == s2.hasMoreTokens();
        } catch (Exception e) {
            return false;
        }
    }

    public static String readFile(String file) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded);
    }

    public static boolean customCheck(String exe, String in, String out, String res) {
        Runtime r = Runtime.getRuntime();
        try{
            Process p = r.exec(exe + " " + in + " " + out + " " + res);
            int cool = p.waitFor();
            Log.d("Custom check: " + cool);
            return cool == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("customCheck fail");
        return false;
    }

    public static void clearButSolve() {
        File dir = new File(CHECK_DIR);
        for (String s : dir.list()) {
            File f = new File(CHECK_DIR + s);
            if (f.isDirectory()) {
                clearDir(CHECK_DIR + s);
            }
            if (!s.equals("solve.exe")) f.delete();
        }
    }
}
class RunResult{
    private int exitcode;
    private String time;
    private String memory;
    private String info;
    private int verdict;

    RunResult(int exitcode, String time, String memory) {
        this.exitcode = exitcode;
        this.time = time;
        this.memory = memory;
    }

    public int getExitcode() {
        return exitcode;
    }

    public String getMemory() {
        return memory;
    }

    public String getTime() {
        return time;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getVerdict() {
        return verdict;
    }

    public void setVerdict(int verdict) {
        this.verdict = verdict;
    }
}

class Worker extends Thread {
    private final Process process;
    private Integer exit;
    public Worker(Process process) {
        this.process = process;
    }
    public void run() {
        try {
            exit = process.waitFor();
        } catch (InterruptedException ignore) {
        }
    }

    public Integer getExit() {
        return exit;
    }
}