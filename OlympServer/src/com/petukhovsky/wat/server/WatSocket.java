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
        Log.d("Socket connected");
        try {
            int g = readByte();
            String login = read();
            String pass = read();
            switch (g) {
                case 0:
                    account = Auth.auth(login, pass);
                    break;
                case 1:
                    String fName = read();
                    String sName = read();
                    account = Auth.register(login, pass, fName, sName);
                    break;
                default:
                    Log.d("Look just like incorrect request. IP: " + getIp());
                    destroy();
                    return;
            }
            if (account == null) {
                writeByte(0);
                destroy();
                return;
            }
            writeByte(1);
            WatOlympiad.firstMsg(account, this);
            while (true) {
                int b = readByte();
                WatOlympiad.msgReceived(b, account, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Socket disconnected: " + getIp());
        }
    }

    private String getIp() {
        return ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();
    }

    public String read() throws IOException {
        try {
            String res = dis.readUTF();
            Log.c("R: " + res);
            return res;
        } catch (IOException e) {
            Log.e("Error while reading string.");
            destroy();
            throw new IOException();
        }
    }

    public int readByte() throws IOException {
        try {
            int i = dis.readByte();
            Log.c("R: " + i);
            return i;
        } catch (IOException e) {
            Log.e("Error while reading byte.");
            destroy();
            throw new IOException();
        }
    }

    public int readInt() throws IOException {
        try {
            int i = dis.readInt();
            Log.c("R: " + i);
            return i;
        } catch (IOException e) {
            Log.e("Error while reading int.");
            destroy();
            throw new IOException();
        }
    }

    public long readLong() throws IOException {
        try {
            long i = dis.readLong();
            Log.c("R: " + i);
            return i;
        } catch (IOException e) {
            Log.e("Error while reading int.");
            destroy();
            throw new IOException();
        }
    }

    public byte[] readByteArray(int length) throws IOException{
        byte[] arr = new byte[length];
        try {
            dis.read(arr);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error while reading array of bytes.");
            destroy();
            throw new IOException();
        }
        return arr;
    }

    public void write(String x) throws IOException {
        try {
            dos.writeUTF(x);
            Log.c("W: " + x);
        } catch (IOException e) {
            Log.e("Error while writing msg");
            destroy();
            throw new IOException();
        }
    }

    public void writeByte(int b) throws IOException {
        try {
            dos.writeByte(b);
            Log.c("W: " + b);
        } catch (IOException e) {
            Log.e("Error while writing byte");
            destroy();
            throw new IOException();
        }
    }

    public void writeInt(int b) throws IOException {
        try {
            dos.writeInt(b);
            Log.c("W: " + b);
        } catch (IOException e) {
            Log.e("Error while writing int");
            destroy();
            throw new IOException();

        }
    }

    public void writeLong(long b) throws IOException {
        try {
            dos.writeLong(b);
            Log.c("W: " + b);
        } catch (IOException e) {
            Log.e("Error while writing int");
            destroy();
            throw new IOException();
        }
    }

    public void flush() throws IOException {
        try {
            dos.flush();
        } catch (IOException e) {
            Log.e("Error while flush");
            destroy();
            throw new IOException();
        }
    }

    public synchronized void writeSyncData (SocketConnection socketConnection) throws IOException {
        socketConnection.run(this);
        flush();
    }

    public void destroy() {
        if (account != null) WatOlympiad.disconnect(this);
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

    public Account getAccount() {
        return this.account;
    }
}