package com.petukhovsky.wat.server;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Arthur on 31.08.2014.
 * <p/>
 * Этот класс вообще бесполезен
 */
public class Server {

    private static final int DEFAULT_PORT = 4898;
    private static final boolean RETRY_ACCEPT_SOCKETS_ON_ERROR = true;

    private static ServerSocket serverSocket;
    private static boolean acceptSockets = true;

    public static void main(String[] args) {
        init();
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            fatalError("Error binding port.");
        }
        while (RETRY_ACCEPT_SOCKETS_ON_ERROR) {
            socketHandler();
        }
        //Log.d("Closing...");
    }

    private static void init() {
        new Gui();
        Auth.init();
        WatOlympiad.init();
        Gui.getGui().updateOlympiadTitles();
    }

    public static void fatalError(String s) {
        Log.e("FATAL ERROR: " + s);
        System.exit(0);
    }

    private static void socketHandler() {
        while (acceptSockets) {
            Socket s;
            try {
                s = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Error accepting socket.");
                break;
            }
            new WatSocket(s).init();
        }
    }
}
