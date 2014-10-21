package com.petukhovsky.wat.server;

/**
 * Created by Arthur on 31.08.2014.
 * <p/>
 * Класс инкапсулирующий параметры аккаутов.
 */
public class Account {
    private int id;
    private String login;
    private String pass;
    private int superuser;
    private int type;
    private String color;

    Account(int id, String login, String pass, int superuser, int type, String color) {
        this.id = id;
        this.login = login;
        this.pass = pass;
        this.superuser = superuser;
        this.type = type;
        this.color = color;
    }

    @Override
    public String toString() {
        return login + ":" + pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public int getSuperuser() {
        return superuser;
    }

    public void setSuperuser(int superuser) {
        this.superuser = superuser;
        SQLite.updateAccount(id, "superuser", superuser);
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        SQLite.updateAccount(id, "Type", type);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        SQLite.updateAccount(id, "Color", color);
    }

    public static Account create(int id, String login, String pass, int superuser, int type, String color) {
        Account acc = new Account(id, login, pass, superuser, type, color);
        SQLite.writeAccount(acc);
        return acc;
    }

    public String getColoredUsername() {
        return "<font color=\"" + getColor() + "\">" + getLogin() + "</font>";
    }
}
