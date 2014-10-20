package com.petukhovsky.wat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Arthur on 31.08.2014.
 * <p/>
 */
public class WatSocket implements Runnable {

    private Socket socket = null;
    private DataOutputStream dos = null;
    private DataInputStream dis = null;
    private Account account = null;
    private boolean interrupted = false;
    private boolean initialized = false;

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
            default:
                Log.d("Look just like incorrect request. IP: " + getIp());
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
        initialized = true;
        WatOlympiad.firstMsg(account, this);
        while (!interrupted) {
            int b = readByte();
            if (interrupted) return;
            msgReceived(b);
        }
    }

    private String getIp() {
        return ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();
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
        WatOlympiad.msgReceived(b, account, this);
    }

    private void addConnection() {
        //TODO: delete this
    }

    private void deleteConnection() {
        WatOlympiad.setState(this, -1);
    }

    public boolean isDead() {
        return interrupted;
    }

    public Account getAccount() {
        return this.account;
    }
}