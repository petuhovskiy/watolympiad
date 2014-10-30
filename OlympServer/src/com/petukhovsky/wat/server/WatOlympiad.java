package com.petukhovsky.wat.server;

import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
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

    private static HashMap<WatSocket, Olympiad> states = new HashMap<WatSocket, Olympiad>();
    private static String location = Checker.RES_DIR + "olymp/";
    private static ArrayList<Olympiad> olympiads = new ArrayList<Olympiad>();
    private static ArrayDeque<Source> checkQueue = new ArrayDeque<Source>();

    public static void firstMsg(Account account, WatSocket ws) throws IOException{
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

    public static void msgReceived(final int b, final Account account, WatSocket ws) throws IOException {
        ws.writeSyncData(new SocketConnection() {
            public void run(WatSocket ws) throws IOException {
                if (states.get(ws) == null) {
                    if (b == 1) {
                        int i = ws.readInt();
                        if (i >= olympiads.size() || i < 0) {
                            ws.writeByte(0);
                            return;
                        }
                        int type = SQLite.getType(olympiads.get(i).getId(), account.getId());
                        if (type == 1) {
                            ws.writeByte(0);
                            return;
                        }
                        if (type == 2) {
                            ws.writeByte(2);
                        } else {
                            ws.writeByte(1);
                        }
                        sendInfo(ws, olympiads.get(i), account);
                        states.put(ws, olympiads.get(i));
                    }
                    return;
                }
                final Olympiad olymp = states.get(ws);
                if (b == 1) {
                    String msg = ws.read();
                    msg = msg.replaceAll("\"", "'");
                    msg = Olympiad.formatTime(olymp.getTimeFromStart()) + " \n*___*\n \n" + msg;
                    final String finalMsg = msg;
                    new Thread(){
                        @Override
                        public void run() {
                            userMessageReceived(account.getId(), olymp, finalMsg);
                        }
                    }.start();
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
                    addCheckTask(olymp.writeSourceFile(ws, fileLength, language, task, account));
                }
            }
        });
    }

    public static void userMessageReceived(int account, Olympiad olympiad, final String msg) {
        final int id = SQLite.writeMessage(account, msg, olympiad.getId());
        for (Map.Entry<WatSocket, Olympiad> s : states.entrySet()) {
            if (s.getValue() == olympiad && (account == -1 || s.getKey().getAccount().getId() == account)) {
                try {
                    s.getKey().writeSyncData(new SocketConnection() {
                        @Override
                        public void run(WatSocket ws) throws IOException {
                            ws.writeByte(6);
                            ws.writeInt(id);
                            ws.write(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Gui.getGui().updateMessage(id, msg);
    }

    public static void addCheckTask(Source source) {
        checkQueue.addLast(source);
    }

    private static void sendInfo(WatSocket ws, final Olympiad olympiad, final Account account) throws IOException {
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

    public static void stopOlympiad(Olympiad olympiad) {
        for (Map.Entry<WatSocket, Olympiad> entry : states.entrySet()) {
            if (entry.getValue() == olympiad) {
                try {
                    entry.getKey().writeSyncData(new SocketConnection() {
                        public void run(WatSocket ws) throws IOException {
                            ws.writeByte(3);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void startOlympiad(final Olympiad olympiad) {
        for (Map.Entry<WatSocket, Olympiad> entry : states.entrySet()) {
            if (entry.getValue() == olympiad) {
                try{
                    entry.getKey().writeSyncData(new SocketConnection() {
                        public void run(WatSocket ws) throws IOException {
                            ws.writeByte(2);
                            ws.writeLong(olympiad.getTimeFromStart());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            while (!checkQueue.isEmpty()) {
                Source source = checkQueue.removeFirst();
                Checker.clearDir(CHECK_DIR);
                String s = Checker.compile(source, CHECK_DIR + "/solve.exe");
                if (!new File(CHECK_DIR + "/solve.exe").exists()) {
                    source.setStatus(1);
                    source.setMsg("<font color=red>Ошибка компиляции</font><br>" + s);
                    SQLite.updateSource(source);
                    WatOlympiad.updateSourceState(source);
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

    public static void sendSourceState(Olympiad olympiad, Account account, final Source source) {
        for (Map.Entry<WatSocket, Olympiad> s : states.entrySet()) {
            if (s.getValue() == olympiad && s.getKey().getAccount() == account) {
                WatSocket ws = s.getKey();
                try {
                    ws.writeSyncData(new SocketConnection() {
                        @Override
                        public void run(WatSocket ws) throws IOException {
                            ws.writeByte(4);
                            sendState(ws, source);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void updateSourceState(final Source source) {
        for (Map.Entry<WatSocket, Olympiad> s : states.entrySet()) {
            if (s.getValue() == source.getOlympiad() && s.getKey().getAccount() == source.getAccount()) {
                WatSocket ws = s.getKey();
                try {
                    ws.writeSyncData(new SocketConnection() {
                        @Override
                        public void run(WatSocket ws) throws IOException {
                            ws.writeByte(5);
                            ws.writeInt(source.getId());
                            ws.writeInt(source.getStatus());
                            ws.write(source.getMsg());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void sendState(WatSocket ws, final Source source) throws IOException {
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

    public static Olympiad getOlympiad(int index) {
        return olympiads.get(index);
    }

    public static void answerQuestion(final int id, final String text) {
        Pair<Integer, String> q = SQLite.answerQuestion(id, text);
        for (final Map.Entry<WatSocket, Olympiad> s : states.entrySet()) {
            if (s.getValue().getId().equals(q.getValue()) && (q.getKey() == -1 || s.getKey().getAccount().getId() == q.getKey())) {
                try {
                    s.getKey().writeSyncData(new SocketConnection() {
                        @Override
                        public void run(WatSocket ws) throws IOException {
                            ws.writeByte(6);
                            ws.writeInt(id);
                            ws.write(text);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Gui.getGui().updateMessage(id, text);
    }

    public static String[] getOlympiadsTitles() {
        String[] arr = new String[olympiads.size()];
        for (int i = 0; i < olympiads.size(); i++) arr[i] = olympiads.get(i).getName();
        return arr;
    }

    public static void disconnect(WatSocket watSocket) {
        states.remove(watSocket);
    }
}
