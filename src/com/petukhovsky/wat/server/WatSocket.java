package com.petukhovsky.wat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Arthur on 31.08.2014.
 * <p/>
 * Вот это мясо. Тут тоже всё дописано, осталось только модулей прифигачить.
 */
   /*
    * Описание протокола:
    * Первое сообщение:
    * 0 - Авторизация
    * 1 - Регистрация
    * 2 - watRemote auth
    * Дальше идут логин и пароль. В случае успешного соединения сервер отвечает 1.
    *
    * Окно выбора:
    * 0 - Перейти в watConsole.
    * 1 - watChat
    *
    *
    * Единые команды в модулях:
    * 0 - Выход в окно выбора
    * 2 - chatMessage
    */
public class WatSocket implements Runnable {
    private static HashMap<Integer, WatSocket> connections = new HashMap<Integer, WatSocket>();

    private Socket socket = null;
    private DataOutputStream dos = null;
    private DataInputStream dis = null;
    private Account account = null;
    private boolean interrupted = false;
    private boolean initialized = false;
    private int mode = 0;

    WatSocket(Socket socket) {
        this.socket = socket;
    }

    public void init() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while initializing socket");
            destroy();
            return;
        }
        int g = readByte();
        if (interrupted) return;
        String login = read();
        if (interrupted) return;
        String pass = read();
        if (interrupted) return;
        switch (g) {
            case 0:
                account = Auth.auth(login, pass);
                break;
            case 1:
                account = Auth.register(login, pass);
                break;
            case 2:
                account = Auth.authWithReg(login, pass);
                this.mode = 1;
                break;
            default:
                destroy();
                return;
        }
        if (account == null) {
            writeByte(0);
            if (interrupted) return;
            destroy();
            return;
        }
        writeByte(1);
        if (interrupted) return;
        addConnection();
        initialized = true;
        while (!interrupted) {
            int b = readByte();
            if (interrupted) return;
            msgReceived(b);
        }
    }

    public String read() {
        if (dis == null) return null;
        try {
            String res = dis.readUTF();
            Log.c("R: " + res);
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while reading string.");
            destroy();
        }
        return null;
    }

    public int readByte() {
        if (dis == null) return -1;
        try {
            int i = dis.readByte();
            Log.c("R: " + i);
            return i;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while reading byte.");
            destroy();
        }
        return -1;
    }

    public int readInt() {
        if (dis == null) return -1;
        try {
            int i = dis.readInt();
            Log.c("R: " + i);
            return i;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while reading int.");
            destroy();
        }
        return -1;
    }

    public long readLong() {
        if (dis == null) return -1;
        try {
            long i = dis.readLong();
            Log.c("R: " + i);
            return i;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while reading int.");
            destroy();
        }
        return -1;
    }

    public void write(String x) {
        if (dos == null) return;
        try {
            dos.writeUTF(x);
            dos.flush();
            Log.c("W: " + x);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while writing msg");
            destroy();
        }
    }

    public void writeByte(int b) {
        if (dos == null) return;
        try {
            dos.writeByte(b);
            dos.flush();
            Log.c("W: " + b);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while writing byte");
            destroy();
        }
    }

    public void writeInt(int b) {
        if (dos == null) return;
        try {
            dos.writeInt(b);
            dos.flush();
            Log.c("W: " + b);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while writing int");
            destroy();
        }
    }

    public void writeLong(long b) {
        if (dos == null) return;
        try {
            dos.writeLong(b);
            dos.flush();
            Log.c("W: " + b);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while writing int");
            destroy();
        }
    }

    public void destroy() {
        if (interrupted) return;
        if (initialized) deleteConnection();
        interrupted = true;
        try {
            if (dos != null)
                dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dos = null;
        try {
            if (dis != null)
                dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dis = null;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }

    private void msgReceived(int b) {
        if (this.mode == 1) {
            write("Access denied");
            return;
        }
        if (this.mode == 0) {
            switch (b) {
                case 0:
                    if (this.account.getSuperuser() < 1) {
                        writeByte(0);
                        return;
                    }
                    this.mode = 2;
                    writeByte(1);
                    WatConsole.firstMsg(account, this);
                    return;
                case 1:
                    this.mode = 3;
                    WatChat.firstMsg(account, this);
                    return;
                case 2:
                    this.mode = 4;
                    WatOlympiad.firstMsg(account, this);
                    return;
                default:
                    writeByte(0);
                    return;
            }
        }
        if (b == 0) {
            switch (this.mode) {
                case 3:
                    WatChat.deleteUser(this);
                    break;
                case 4:
                    WatOlympiad.setState(this, -1);
                    break;
            }
            this.mode = 0;
            writeByte(1);
            return;
        }
        switch (this.mode) {
            case 2:
                WatConsole.msgReceived(b, account, this);
                return;
            case 3:
                WatChat.msgReceived(b, account, this);
                return;
            case 4:
                WatOlympiad.msgReceived(b, account, this);
                return;
            default:
                writeByte(0);
        }
    }

    private void addConnection() {
        connections.put(this.account.getId(), this);
        if (this.mode == 1) {
            WatConsole.addUser(account.getLogin().toLowerCase(), this);
        }
    }

    private void deleteConnection() {
        connections.remove(account.getId());
        switch (this.mode) {
            case 1:
                WatConsole.deleteUser(account.getLogin().toLowerCase());
                break;
            case 3:
                WatChat.deleteUser(this);
                break;
            case 4:
                WatOlympiad.setState(this, -1);
                break;
        }
    }

    public boolean isDead() {
        return interrupted;
    }

    public Account getAccount() {
        return this.account;
    }
}