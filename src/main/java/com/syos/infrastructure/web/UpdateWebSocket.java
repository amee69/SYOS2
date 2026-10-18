package com.syos.infrastructure.web;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/ws/updates")
public class UpdateWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    //wraps the hasheset in a synchronized set to make it thread safe
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("SYOS WS: Client connected. Total: " + sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("SYOS WS: Client disconnected. Total: " + sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        sessions.remove(session);
    }

    public static void broadcast(String message) {
        System.out.println("SYOS WS: Broadcasting '" + message + "' to " + sessions.size() + " clients");
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        System.err.println("SYOS WS: Failed to send to client: " + e.getMessage());
                    }
                }
            }
        }
    }
}