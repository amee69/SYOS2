package com.syos.presentation.web;

import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemServlet Tests")
class ItemServletTest {

    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private ItemRepository itemRepo;
    @Mock private ServletContext servletContext;

    private ItemServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(servletContext.getAttribute("itemRepo")).thenReturn(itemRepo);

        servlet = new ItemServlet() {
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };
        servlet.init();
    }

    @Test
    @DisplayName("Should return all items")
    void doGet_AllItems_ReturnsJsonArray() throws Exception {
        when(req.getPathInfo()).thenReturn("/");
        when(itemRepo.findAll()).thenReturn(Arrays.asList(
                new Item("DRY-001", "Rice", new BigDecimal("650"), "DRY", 50),
                new Item("BEV-001", "Tea", new BigDecimal("390"), "BEV", 50)
        ));

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("DRY-001"));
        assertTrue(json.contains("BEV-001"));
    }

    @Test
    @DisplayName("Should return single item by code")
    void doGet_SingleItem_ReturnsItem() throws Exception {
        when(req.getPathInfo()).thenReturn("/DRY-001");
        when(itemRepo.findByCode("DRY-001")).thenReturn(
                Optional.of(new Item("DRY-001", "Rice", new BigDecimal("650"), "DRY", 50)));

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("DRY-001"));
        assertTrue(json.contains("Rice"));
    }

    @Test
    @DisplayName("Should return 404 for unknown item")
    void doGet_UnknownItem_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/FAKE-001");
        when(itemRepo.findByCode("FAKE-001")).thenReturn(Optional.empty());

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Should return all items when pathInfo is null")
    void doGet_NullPath_ReturnsAllItems() throws Exception {
        when(req.getPathInfo()).thenReturn(null);
        when(itemRepo.findAll()).thenReturn(Arrays.asList(
                new Item("DRY-001", "Rice", new BigDecimal("650"), "DRY", 50)
        ));

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("DRY-001"));
    }
}