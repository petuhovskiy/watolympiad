package com.petukhovsky.wat.server;

import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 31.08.2014.
 * <p/>
 * Тут лежит авторизация. Вот и всё
 */
public class Auth {

    private static final String BANNED_PASSWORD_CHARACTERS = ":\\/` &^-=+)(*{}[] ";
    private static final int MINIMAL_PASSWORD_LENGTH = 4;

    private static HashMap<String, Account> accounts = new HashMap<String, Account>();

    @SuppressWarnings("unchecked")
    public static void init() {
        accounts = SQLite.getAccounts();
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(s.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return bytesToHexString(digest);
        } catch (Exception e) {
            e.printStackTrace();
            assert true;
            return null;
        }
    }

    public static Account register(String login, String password) {
        if (!checkPass(password)) return null;
        if (accounts.containsKey(login.toLowerCase())) return null;
        Account account = Account.create(accounts.size(), login, sha256(password), 0, 0, "black");
        accounts.put(login.toLowerCase(), account);
        return account;
    }

    public static Account auth(String login, String password) {
        if (!accounts.containsKey(login.toLowerCase())) return null;
        if (!accounts.get(login.toLowerCase()).getPass().equals(sha256(password))) return null;
        return accounts.get(login.toLowerCase());
    }

    public static Account authWithReg(String login, String password) {
        if (accounts.containsKey(login.toLowerCase()))
            return auth(login, password);
        else return register(login, password);
    }

    private static boolean checkPass(String password) {
        if (password.length() < MINIMAL_PASSWORD_LENGTH) return false;
        for (int i = 0; i < BANNED_PASSWORD_CHARACTERS.length(); i++)
            if (password.contains(BANNED_PASSWORD_CHARACTERS.substring(i, i + 1))) return false;
        return true;
    }

    public static Account getAccount(String username) {
        return accounts.get(username);
    }

    public static Account getAccount(int id) {
        for (Account i : accounts.values()) {
            if (i.getId() == id) return i;
        }
        return null;
    }

}
