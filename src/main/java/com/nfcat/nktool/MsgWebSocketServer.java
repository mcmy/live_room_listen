package com.nfcat.nktool;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MsgWebSocketServer extends WebSocketServer{

    List<WebSocket> webSocketList = new ArrayList<>();

    public void sendAll(String msg) {
        for (WebSocket webSocket : webSocketList) {
            try {
                webSocket.send(msg);
            } catch (Exception e) {
                webSocketList.remove(webSocket);
            }
        }
    }

    public static void main(String[] args) {
        new MsgWebSocketServer(8989).start();
    }

    public MsgWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onClose(WebSocket ws, int arg1, String arg2, boolean arg3) {
        webSocketList.remove(ws);
    }

    @Override
    public void onError(WebSocket ws, Exception e) {
        webSocketList.remove(ws);
        try {
            ws.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onMessage(WebSocket ws, String msg) {

    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake shake) {
        webSocketList.add(ws);
    }

    @Override
    public void onStart() {
    }

}