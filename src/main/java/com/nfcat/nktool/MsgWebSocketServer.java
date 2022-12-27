package com.nfcat.nktool;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;


public class MsgWebSocketServer extends WebSocketServer {

    public MsgWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onClose(WebSocket ws, int arg1, String arg2, boolean arg3) {

    }

    @Override
    public void onError(WebSocket ws, Exception e) {

    }

    @Override
    public void onMessage(WebSocket ws, String msg) {

    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake shake) {

    }

    @Override
    public void onStart() {

    }

}