package com.test.server;

import java.nio.channels.SocketChannel;

class ChangeRequest {


    private static final int REGISTER = 1;
    static final int CHANGEOPS = 2;

    private SocketChannel socket;
    private int type;
    private int ops;

    ChangeRequest(SocketChannel socket, int type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;

    }
}
