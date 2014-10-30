package com.petukhovsky.wat.server;

import java.io.IOException;

/**
 * Created by Arthur on 10/29/2014.
 */
public abstract class SocketConnection {

    public abstract void run(WatSocket ws) throws IOException;
}
