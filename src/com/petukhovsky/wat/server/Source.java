package com.petukhovsky.wat.server;

import java.io.File;

/**
 * Created by petuh_000 on 29.09.2014.
 */
public class Source {
    private int language;
    private String location;
    private Olympiad olympiad;
    private int task;
    private Account account;
    private int id;
    private String time;
    private int status;
    private String msg;
    private int score = 0;

    public Source(int language, String location, Olympiad olympiad, int task, Account account, int id, String time, int status, String msg) {
        this.language = language;
        this.location = location;
        this.olympiad = olympiad;
        this.task = task;
        this.account = account;
        this.id = id;
        this.time = time;
        this.status = status;
        this.msg = msg;
    }

    public File getFile() {
        return new File(location);
    }

    public Olympiad getOlympiad() {
        return olympiad;
    }

    public int getTask() {
        return task;
    }

    public int getLanguage() {
        return language;
    }

    public Account getAccount() {
        return account;
    }

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSource() {
        try{
            return Checker.readFile(location);
        } catch (Exception e) {
            return "";
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
