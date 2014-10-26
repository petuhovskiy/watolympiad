package com.petukhovsky.wat.gui;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Arthur on 23.09.2014.
 */
public class WatNetwork implements Runnable {
    private final static String SERVER_IP = "localhost";
    private final static int PORT = 4898;

    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private static int connection = -1;
    private static byte[] sourceFile;

    @Override
    public void run() {
        while (true) {
            try {
                socket = new Socket(SERVER_IP, PORT);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            connection = 0;
            while (true) {
                int b = readByte();
                if (connection == -1) break;
                msgReceived(b);
            }
        }
    }

    public static String read() {
        if (dis == null) return null;
        try {
            String res = dis.readUTF();
            System.out.println("R: " + res);
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while reading string.");
            destroy();
        }
        return null;
    }

    private static void destroy() {
        dis = null;
        dos = null;
        socket = null;
        if (connection == 1 || connection == 2) WatAuth.getWatAuth().unlock();
        connection = -1;
        WatGUI.showAuthPanel();
    }

    public static int readByte() {
        if (dis == null) return -1;
        try {
            int i = dis.readByte();
            System.out.println("R: " + i);
            return i;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while reading byte.");
            destroy();
        }
        return -1;
    }

    public static int readInt() {
        if (dis == null) return -1;
        try {
            int i = dis.readInt();
            System.out.println("R: " + i);
            return i;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while reading byte.");
            destroy();
        }
        return -1;
    }

    public static long readLong() {
        if (dis == null) return -1;
        try {
            long i = dis.readLong();
            System.out.println("R: " + i);
            return i;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while reading byte.");
            destroy();
        }
        return -1;
    }

    public static void write(String x) {
        if (dos == null) return;
        try {
            dos.writeUTF(x);
            dos.flush();
            System.out.println("W: " + x);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while writing msg");
            destroy();
        }
    }

    public static void writeByte(int b) {
        if (dos == null) return;
        try {
            dos.writeByte(b);
            dos.flush();
            System.out.println("W: " + b);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while writing byte");
            destroy();
        }
    }

    private static void writeArrayByte(byte[] arr) {
        if (dos == null) return;
        try {
            dos.write(arr);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while writing array of bytes");
            destroy();
        }
    }

    public static void writeInt(int b) {
        if (dos == null) return;
        try {
            dos.writeInt(b);
            dos.flush();
            System.out.println("W: " + b);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while writing byte");
            destroy();
        }
    }

    public static void writeLong(long b) {
        if (dos == null) return;
        try {
            dos.writeLong(b);
            dos.flush();
            System.out.println("W: " + b);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while writing byte");
            destroy();
        }
    }

    private static void msgReceived(int b) {
        if (connection == 1 || connection == 2) {
            if (b == 1) {
                connection = 3;
                readByte();
                int count = readInt();
                String[] arr = new String[count];
                for (int i = 0; i < count; i++) {
                    arr[i] = read();
                }
                WatOlympiad.getWatOlympiad().showChoosePanel(arr);
                WatGUI.showOlympiadPanel();
                WatAuth.getWatAuth().unlock();
            } else {
                if (connection == 1) {
                    JOptionPane.showMessageDialog(WatGUI.getGui(), "Неправильный логин или пароль", "WatOlympiad", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(WatGUI.getGui(), "Пароль не может состоять из символов пробела и :\\/` &^-=+)(*{}[]", "WatOlympiad", JOptionPane.WARNING_MESSAGE);
                }
                destroy();
            }
            return;
        }
        switch (connection) {
            case 3:
                if (b == 0) {
                    JOptionPane.showMessageDialog(WatGUI.getGui(), "Вы не зарегистрированы на соревнование", "WatOlympiad", JOptionPane.WARNING_MESSAGE);
                    WatOlympiad.getWatOlympiad().unlock();
                } else {
                    if (b == 2) {
                        JOptionPane.showMessageDialog(WatGUI.getGui(), "Вы участвуете в соревновании вне конкурса", "WatOlympiad", JOptionPane.WARNING_MESSAGE);
                    }
                    int count = readInt();
                    String[] arr = new String[count];
                    for (int i = 0; i < count; i++) arr[i] = read();
                    long duration = readLong();
                    count = readInt();
                    WatOlympiad.getWatOlympiad().clearStates();
                    for (int i = 0; i < count; i++) {
                        int id = readInt();
                        String time = read();
                        int task = readInt();
                        int status = readInt();
                        int language = readInt();
                        String msg = read();
                        String source = read();
                        WatOlympiad.getWatOlympiad().addState(id, time, task, status, language, msg, source);
                    }
                    int messages = readInt();
                    ArrayList<Pair<Integer, String>> m = new ArrayList<>();
                    for (int i = 0; i < messages; i++) {
                        int id = readInt();
                        String msg = read();
                        m.add(new Pair<>(id, msg));
                    }
                    WatOlympiad.getWatOlympiad().showOlympPanel(arr, duration, m);
                    connection = 4;
                }
                return;
            case 4:
                if (b == 2) {
                    long timePassed = readLong();
                    WatOlympiad.getWatOlympiad().startOlymp(timePassed);
                } else if (b == 3) {
                    WatOlympiad.getWatOlympiad().stopOlymp();
                } else if (b == 4) {
                    int id = readInt();
                    String time = read();
                    int task = readInt();
                    int status = readInt();
                    int language = readInt();
                    String msg = read();
                    String source = read();
                    WatOlympiad.getWatOlympiad().addState(id, time, task, status, language, msg, source);
                } else if (b == 5) {
                    int id = readInt();
                    int status = readInt();
                    String msg = read();
                    WatOlympiad.getWatOlympiad().changeState(id, status, msg);
                } else if (b == 6) {
                    int id = readInt();
                    String msg = read();
                    WatOlympiad.getWatOlympiad().messageUpdate(id, msg);
                }
                return;
            case 5:
                if (b == 0) {
                    connection = 4;
                    WatOlympiad.getWatOlympiad().unlock();
                    return;
                }
                writeArrayByte(sourceFile);
                connection = 4;
                WatOlympiad.getWatOlympiad().unlock();
                return;
        }
        System.out.println("lol wtf: " + connection + " " + b);
    }

    public static void register(String login, String pass, String fName, String sName) {
        if (connection != 0) {
            WatAuth.getWatAuth().unlock();
            return;
        }
        connection = 2;
        writeByte(1);
        write(login);
        write(pass);
        write(fName);
        write(sName);
    }

    public static void auth(String login, String pass) {
        if (connection != 0) {
            WatAuth.getWatAuth().unlock();
            return;
        }
        connection = 1;
        writeByte(0);
        write(login);
        write(pass);
    }

    public static void selectOlympiad(int index) {
        writeByte(1);
        writeInt(index);
    }

    public static void writeSource(File file, int task, int language) {
        connection = 5;
        InputStream is = null;
        sourceFile = new byte[(int) file.length()];
        try {
            is = new FileInputStream(file);
            is.read(sourceFile);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                assert is != null;
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        writeByte(2);
        writeInt(task);
        writeByte(language);
        writeInt(sourceFile.length);
    }

    public static void sendMessage(String text) {
        writeByte(1);
        write(text);
    }
}
