package com.petukhovsky.wat.server;

import java.util.HashMap;

/**
 * Created by Arthur on 05.09.2014.
 * <p/>
 * Готовый модуль консоли. Всё.
 */
public class WatConsole {

    private static HashMap<String, WatSocket> remotes = new HashMap<String, WatSocket>();

    public static void firstMsg(Account account, WatSocket ws) {
    }

    public static String console(String msg, Account account) {
        msg = msg.trim();
        if (msg.isEmpty()) return "Wrong msg";
        String lowline = msg.toLowerCase();
        String arr[] = lowline.split("\\s+");
        if (arr[0].equals("setcolor")) {
            if (account.getSuperuser() >= 2) {
                Account acc = Auth.getAccount(arr[1]);
                if (arr.length < 3 || acc == null) return "Wrong msg";
                WatChat.setColor(acc, arr[2]);
                return "OK";
            } else return "Access denied";
        }
        if (arr[0].equals("startolymp")) {
            if (account.getSuperuser() >= 2) {
                if (arr.length < 3) return "Wrong msg";
                int num;
                try {
                    num = Integer.valueOf(arr[1]);
                } catch (Exception e) {
                    return "Wrong msg";
                }
                if (num >= WatOlympiad.getOlympiadCount() || num < 0) return "Wrong msg";
                long startAfter;
                try{
                    startAfter = Long.valueOf(arr[2]);
                } catch (Exception e) {
                    return "Wrong msg";
                }
                if (startAfter < 0) return "Wrong msg";
                WatOlympiad.consoleStart(num, startAfter);
                return "OK";
            } else return "Access denied";
        }
        if (arr[0].equals("startolympat")) {
            if (account.getSuperuser() >= 2) {
                if (arr.length < 3) return "Wrong msg";
                int num;
                try {
                    num = Integer.valueOf(arr[1]);
                } catch (Exception e) {
                    return "Wrong msg";
                }
                if (num >= WatOlympiad.getOlympiadCount() || num < 0) return "Wrong msg";
                long startTime;
                try{
                    startTime = Long.valueOf(arr[2]);
                } catch (Exception e) {
                    return "Wrong msg";
                }
                if (startTime < 0) return "Wrong msg";
                WatOlympiad.consoleStart(num, startTime - System.currentTimeMillis());
                return "OK";
            } else return "Access denied";
        }
        if (arr[0].equals("superuser")) {
            if (account.getSuperuser() >= 2) {
                if (arr.length == 1) return "Wrong msg";
                if (arr.length == 2) {
                    Account acc = Auth.getAccount(arr[1]);
                    if (acc == null) return "User not found";
                    return "Superuser status: " + acc.getSuperuser();
                }
                int superuser;
                try {
                    superuser = Integer.valueOf(arr[2]);
                } catch (Exception e) {
                    return "Wrong msg";
                }
                Account acc = Auth.getAccount(arr[1]);
                if (acc == null) return "User not found";
                acc.setSuperuser(superuser);
                return "OK";
            } else return "Access denied";
        }
        if (arr[0].equals("type")) {
            if (account.getSuperuser() >= 2) {
                if (arr.length == 1) return "Wrong msg";
                if (arr.length == 2) {
                    Account acc = Auth.getAccount(arr[1]);
                    if (acc == null) return "User not found";
                    return "Account type: " + acc.getType();
                }
                int type;
                try {
                    type = Integer.valueOf(arr[2]);
                } catch (Exception e) {
                    return "Wrong msg";
                }
                Account acc = Auth.getAccount(arr[1]);
                if (acc == null) return "User not found";
                acc.setType(type);
                return "OK";
            } else return "Access denied";
        }
        if (arr[0].equals("watremote")) {
            if (account.getSuperuser() < 1) {
                return "Access denied";
            }
            String[] arr2 = lowline.split("\\s+", 3);
            if (arr2.length == 1) {
                String ans = "";
                for (String s : remotes.keySet()) {
                    ans += s + " ";
                }
                if (ans.length() == 0) ans = "All users offline";
                return ans;
            }
            if (arr2.length == 2) {
                if (remotes.containsKey(arr2[1])) return "User online";
                else return "User offline";
            }
            if (!remotes.containsKey(arr2[1])) return "User offline";
            remotes.get(arr2[1]).write(arr2[2]);
            return "OK";
        }
        return "Wrong msg";
    }

    public static void msgReceived(int b, Account account, WatSocket ws) {
        ws.writeByte(1);
        if (ws.isDead()) return;
        if (b != 1) {
            ws.write("Wrong msg");
            return;
        }
        String msg = ws.read();
        if (ws.isDead()) return;
        ws.write(console(msg, account));
    }

    public static void addUser(String s, WatSocket watSocket) {
        remotes.put(s, watSocket);
    }

    public static void deleteUser(String s) {
        remotes.remove(s);
    }
}