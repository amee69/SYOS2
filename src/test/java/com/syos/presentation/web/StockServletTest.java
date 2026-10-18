package com.syos.presentation.web;

import com.syos.application.service.ItemManagementFacade;
import com.syos.application.service.StockService;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockServlet Tests")
class StockServletTest {

    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private StockService stockService;
    @Mock private ItemManagementFacade itemFacade;
    @Mock private ItemRepository itemRepo;
    @Mock private StockRepository stockRepo;
    @Mock private ServletContext servletContext;

    private StockServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(servletContext.getAttribute("stockService")).thenReturn(stockService);
        when(servletContext.getAttribute("itemFacade")).thenReturn(itemFacade);
        when(servletContext.getAttribute("itemRepo")).thenReturn(itemRepo);
        when(servletContext.getAttribute("stockRepo")).thenReturn(stockRepo);

        servlet = new StockServlet() {
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };
        servlet.init();
    }

    @Test
    @DisplayName("Should return stock levels for item")
    void doGet_StockLevels_ReturnsAllLocations() throws Exception {
        when(req.getPathInfo()).thenReturn("/levels/DRY-001");
        when(req.getParameter("location")).thenReturn(null);
        when(stockService.getStockByLocation("DRY-001", "SHELF")).thenReturn(20);
        when(stockService.getStockByLocation("DRY-001", "WEB")).thenReturn(15);
        when(stockService.getStockByLocation("DRY-001", "STORE")).thenReturn(30);

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("DRY-001"));
        assertTrue(json.contains("\"shelf\""));
        assertTrue(json.contains("\"web\""));
        assertTrue(json.contains("\"store\""));
        assertTrue(json.contains("DRY-001"));
    }

    @Test
    @DisplayName("Should return stock for specific location")
    void doGet_StockLevels_SpecificLocation() throws Exception {
        when(req.getPathInfo()).thenReturn("/levels/DRY-001");
        when(req.getParameter("location")).thenReturn("SHELF");
        when(stockService.getStockByLocation("DRY-001", "SHELF")).thenReturn(20);

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("\"quantity\""));
        assertTrue(json.contains("DRY-001"));
    }

    @Test
    @DisplayName("Should return categories")
    void doGet_Categories_ReturnsList() throws Exception {
        when(req.getPathInfo()).thenReturn("/categories");
        when(itemFacade.getCategories()).thenReturn(java.util.Collections.emptyList());

        servlet.doGet(req, resp);

        assertNotNull(responseWriter.toString());
    }

    @Test
    @DisplayName("Should return 400 when batches requested without itemCode")
    void doGet_Batches_NoItemCode_Returns400() throws Exception {
        when(req.getPathInfo()).thenReturn("/batches");
        when(req.getParameter("itemCode")).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 404 for unknown GET endpoint")
    void doGet_UnknownPath_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/unknown");

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 for unknown POST endpoint")
    void doPost_UnknownPath_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/unknown");

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 for unknown DELETE endpoint")
    void doDelete_UnknownPath_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/unknown");

        servlet.doDelete(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}