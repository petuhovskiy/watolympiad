package com.petukhovsky.wat.server;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Arthur on 16.09.2014.
 * <p/>
 * Модуль чата.
 */
public class WatChat {

    private static final int MAX_MESSAGE_LENGTH = 300000;

    private static HashSet<WatSocket> clients = new HashSet<WatSocket>();
    private static HashMap<Account, ChatParams> params = new HashMap<Account, ChatParams>();
    private static ArrayList<String> messages = new ArrayList<String>();

    public static void init() {
    }

    public static void firstMsg(Account account, WatSocket ws) {
        if (!params.containsKey(account)) params.put(account, new ChatParams(messages.size()));
        sendChatMessage(account.getColoredUsername() + " connected");
        String ans = "";
        for (int i = params.get(account).getFirstShow(); i < messages.size(); i++) {
            ans = ans.concat(messages.get(i));
            if (i + 1 < messages.size()) ans = ans.concat("<br>");
        }
        ws.writeByte(1);
        ws.write(ans);
        if (!ws.isDead()) addUser(ws);
    }

    public static void msgReceived(int b, Account account, WatSocket ws) {
        if (b != 1) {
            ws.writeByte(0);
            return;
        }
        String s = ws.read();
        if (ws.isDead()) return;
        if (s.length() > MAX_MESSAGE_LENGTH || s.trim().length() == 0) {
            ws.writeByte(0);
            return;
        }
        ws.writeByte(1);
        if (s.startsWith("/")) {
            ws.writeByte(2);
            ws.write(WatConsole.console(s.substring(1), account));
            return;
        }
        if (s.startsWith("\\")) s = s.substring(1);
        sendChatMessage(formatString(account, s));
    }

    private static void sendChatMessage(String s) {
        Log.b(s);
        messages.add(s);
        for (WatSocket ws : clients) {
            ws.writeByte(2);
            ws.write(s);
        }
    }

    private static String formatString(Account account, String s) {
        return getCurrentTime() + " " + account.getColoredUsername() + ": " + s;
    }

    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    public static void addUser(WatSocket watSocket) {
        clients.add(watSocket);
    }

    public static void deleteUser(WatSocket s) {
        clients.remove(s);
        sendChatMessage(s.getAccount().getColoredUsername() + " disconnected");
    }

    public static void setColor(Account account, String color) {
        account.setColor(color);
    }
}

class ChatParams {

    private int firstShow;

    public ChatParams(int firstShow) {
        this.firstShow = firstShow;
    }

    public int getFirstShow() {
        return firstShow;
    }
}