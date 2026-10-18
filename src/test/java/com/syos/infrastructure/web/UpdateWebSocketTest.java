package com.syos.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UpdateWebSocket Tests")
class UpdateWebSocketTest {

    @Test
    @DisplayName("Should add session on open")
    void onOpen_AddsSession() {
        UpdateWebSocket ws = new UpdateWebSocket();
        Session session = mock(Session.class);

        ws.onOpen(session);
        ws.onClose(session); // cleanup
    }

    @Test
    @DisplayName("Should remove session on close")
    void onClose_RemovesSession() {
        UpdateWebSocket ws = new UpdateWebSocket();
        Session session = mock(Session.class);

        ws.onOpen(session);
        ws.onClose(session);
        // no exception = success
    }

    @Test
    @DisplayName("Should remove session on error")
    void onError_RemovesSession() {
        UpdateWebSocket ws = new UpdateWebSocket();
        Session session = mock(Session.class);

        ws.onOpen(session);
        ws.onError(session, new RuntimeException("test"));
        // no exception = success
    }

    @Test
    @DisplayName("Should broadcast to open sessions")
    void broadcast_SendsToAllOpenSessions() throws Exception {
        UpdateWebSocket ws = new UpdateWebSocket();
        Session session = mock(Session.class);
        RemoteEndpoint.Basic remote = mock(RemoteEndpoint.Basic.class);

        when(session.isOpen()).thenReturn(true);
        when(session.getBasicRemote()).thenReturn(remote);

        ws.onOpen(session);
        UpdateWebSocket.broadcast("TEST_MESSAGE");

        verify(remote).sendText("TEST_MESSAGE");

        ws.onClose(session); // cleanup
    }

    @Test
    @DisplayName("Should skip closed sessions during broadcast")
    void broadcast_SkipsClosedSessions() throws Exception {
        UpdateWebSocket ws = new UpdateWebSocket();
        Session session = mock(Session.class);

        when(session.isOpen()).thenReturn(false);

        ws.onOpen(session);
        UpdateWebSocket.broadcast("TEST_MESSAGE");

        verify(session, never()).getBasicRemote();

        ws.onClose(session); // cleanup
    }
}