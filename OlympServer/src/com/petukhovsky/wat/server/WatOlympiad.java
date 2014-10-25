package com.petukhovsky.wat.server;

import javafx.util.Pair;

import javax.swing.*;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 26.09.2014.
 */
public class WatOlympiad implements Runnable{
    private static final int MAX_FILE_LENGTH = 65536;
    private static final String CHECK_DIR = Checker.RES_DIR + "test";

    private static HashMap<WatSocket, Integer> states = new HashMap<WatSocket, Integer>();
    private static String location = Checker.RES_DIR + "olymp/";
    private static ArrayList<Olympiad> olympiads = new ArrayList<Olympiad>();
    private static ArrayDeque<Source> checkQueue = new ArrayDeque<Source>();
    private static boolean checkNow = true;

    public static void firstMsg(Account account, WatSocket ws) {
        System.out.println("First msg");
        ws.writeByte(1);
        setState(ws, 0);
        ws.writeInt(olympiads.size());
        for (Olympiad olympiad : olympiads) ws.write(olympiad.getName());
    }

    public static void init() {
        File file = new File(location);
        String list[] = file.list();
        for (String s : list) {
            if (new File(location + s).isDirectory()) olympiads.add(new Olympiad(location + s, s));
        }
        SQLite.getCheckQueue();
        SQLite.setGuiMessages();
        new Thread(new WatOlympiad()).start();
    }

    public static void msgReceived(int b, Account account, WatSocket ws) {
        int state = states.get(ws);
        if (state == 0) {
            if (b == 1) {
                int i = ws.readInt();
                if (i >= olympiads.size() || i < 0) ws.writeByte(0);
                else {
                    ws.writeByte(1);
                    sendInfo(ws, olympiads.get(i), account);
                    states.put(ws, i + 1);
                }
            }
            return;
        }
        if (b == 0) {
            ws.writeByte(1);
            states.put(ws, 0);
            ws.writeInt(olympiads.size());
            for (Olympiad olympiad : olympiads) ws.write(olympiad.getName());
            return;
        }
        if (b == 1) {
            String msg = ws.read();
            messageReceived(account.getId(), state - 1, msg);
        }
        if (b == 2) {
            int task = ws.readInt();
            int language = ws.readByte();
            int fileLength = ws.readInt();
            if (fileLength > MAX_FILE_LENGTH) {
                ws.writeByte(0);
                return;
            }
            ws.writeByte(1);
            addCheckTask(olympiads.get(state - 1).writeSourceFile(ws, fileLength, language, task, account));
            return;
        }
    }

    public static void messageReceived(int account, int olympiad, String msg) {
        int id = SQLite.writeMessage(account, msg, olympiads.get(olympiad).getId());
        for (Map.Entry<WatSocket, Integer> s : states.entrySet()) {
            if (s.getValue() == olympiad + 1 && (account == -1 || s.getKey().getAccount().getId() == account)) {
                s.getKey().writeByte(6);
                s.getKey().writeInt(id);
                s.getKey().write(msg);
            }
        }
        Gui.getGui().updateMessage(id, msg);
    }

    public static void addCheckTask(Source source) {
        checkQueue.addLast(source);
    }

    private static void sendInfo(WatSocket ws, Olympiad olympiad, Account account) {
        ws.writeInt(olympiad.tasksCount());
        for (Task t : olympiad.getTasks()) {
            ws.write(t.getName());
        }
        ws.writeLong(olympiad.getDuration());
        olympiad.sendStates(ws, account);
        ArrayList<Pair<Integer, String>> messages = SQLite.getMessages(account.getId(), olympiad.getId());
        ws.writeInt(messages.size());
        for (Pair<Integer, String> s : messages) {
            ws.writeInt(s.getKey());
            ws.write(s.getValue());
        }
        if (olympiad.isRunning()) {
            ws.writeByte(2);
            ws.writeLong(olympiad.getTimeFromStart());
        }
    }

    public static void setState(WatSocket ws, int state) {
        if (state == -1) states.remove(ws); else states.put(ws, state);
    }

    private static int getOlympiadIndex(Olympiad olympiad) {
        for (int i = 0; i < olympiads.size(); i++) if (olympiads.get(i) == olympiad) return i;
        return -1;
    }

    public static void stopOlympiad(Olympiad olympiad) {
        int num = getOlympiadIndex(olympiad);
        if (num == -1) return;
        num++;
        for (Map.Entry<WatSocket, Integer> entry : states.entrySet()) {
            if (entry.getValue() == num) {
                entry.getKey().writeByte(3);
            }
        }
    }

    public static void startOlympiad(Olympiad olympiad) {
        int num = getOlympiadIndex(olympiad);
        if (num == -1) return;
        num++;
        for (Map.Entry<WatSocket, Integer> entry : states.entrySet()) {
            if (entry.getValue() == num) {
                entry.getKey().writeByte(2);
                entry.getKey().writeLong(olympiad.getTimeFromStart());
            }
        }
    }

    public static int getOlympiadCount() {
        return olympiads.size();
    }

    @Override
    public void run() {
        while (checkNow) {
            while (!checkQueue.isEmpty()) {
                Source source = checkQueue.removeFirst();
                Checker.clearDir(CHECK_DIR);
                String s = Checker.compile(source, CHECK_DIR + "/solve.exe");
                if (!new File(CHECK_DIR + "/solve.exe").exists()) {
                    source.setStatus(1);
                    source.setMsg("<font color=red>Ошибка компиляции</font><br>" + s);
                    SQLite.updateSource(source);
                    WatOlympiad.updateStateWithoutConnection(source);
                    Log.d("compile fail");
                    continue;
                }
                source.getOlympiad().checkSolve(source);
            }
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendStateWithoutConnection(Olympiad olympiad, Account account, Source source) {
        int num = getOlympiadIndex(olympiad);
        if (num == -1) return;
        for (Map.Entry<WatSocket, Integer> s : states.entrySet()) {
            if (s.getValue() == num + 1 && s.getKey().getAccount() == account) {
                WatSocket ws = s.getKey();
                ws.writeByte(4);
                sendState(ws, source);
            }
        }
    }

    public static void updateStateWithoutConnection(Source source) {
        int num = getOlympiadIndex(source.getOlympiad());
        if (num == -1) return;
        for (Map.Entry<WatSocket, Integer> s : states.entrySet()) {
            if (s.getValue() == num + 1 && s.getKey().getAccount() == source.getAccount()) {
                WatSocket ws = s.getKey();
                ws.writeByte(5);
                ws.writeInt(source.getId());
                ws.writeInt(source.getStatus());
                ws.write(source.getMsg());
            }
        }
    }

    public static void sendState(WatSocket ws, Source source) {
        ws.writeInt(source.getId());
        ws.write(source.getTime());
        ws.writeInt(source.getTask());
        ws.writeInt(source.getStatus());
        ws.writeInt(source.getLanguage());
        ws.write(source.getMsg());
        ws.write(source.getSource());
    }

    public static Olympiad getOlympiad(String id) {
        for (Olympiad i : olympiads) {
            if (i.getId().equals(id)) return i;
        }
        return null;
    }

    public static void answerQuestion(int id, String text) {
        Pair<Integer, String> q = SQLite.answerQuestion(id, text);
        for (Map.Entry<WatSocket, Integer> s : states.entrySet()) {
            if (s.getValue() > 0 && olympiads.get(s.getValue()-1).getId().equals(q.getValue()) && (q.getKey() == -1 || s.getKey().getAccount().getId() == q.getKey())) {
                s.getKey().writeByte(6);
                s.getKey().writeInt(id);
                s.getKey().write(text);
            }
        }
        Gui.getGui().updateMessage(id, text);
    }

    public static String[] getOlympiadsTitles() {
        String[] arr = new String[olympiads.size()];
        for (int i = 0; i < olympiads.size(); i++) arr[i] = olympiads.get(i).getName();
        return arr;
    }
}
