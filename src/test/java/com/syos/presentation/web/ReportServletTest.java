package com.syos.presentation.web;

import com.syos.application.service.StockService;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.TransactionRepository;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServlet Tests")
class ReportServletTest {

    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private StockService stockService;
    @Mock private ItemRepository itemRepo;
    @Mock private StockRepository stockRepo;
    @Mock private TransactionRepository transRepo;
    @Mock private ServletContext servletContext;

    private ReportServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(servletContext.getAttribute("stockService")).thenReturn(stockService);
        when(servletContext.getAttribute("itemRepo")).thenReturn(itemRepo);
        when(servletContext.getAttribute("stockRepo")).thenReturn(stockRepo);
        when(servletContext.getAttribute("transRepo")).thenReturn(transRepo);

        servlet = new ReportServlet() {
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };
        servlet.init();
    }

    @Test
    @DisplayName("Should return sales report")
    void doGet_SalesReport_ReturnsData() throws Exception {
        when(req.getPathInfo()).thenReturn("/sales");
        when(req.getParameter("type")).thenReturn(null);
        when(transRepo.getItemSalesByDate(any(), any())).thenReturn(Collections.emptyList());

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("grandTotal"));
        assertTrue(json.contains("items"));
    }

    @Test
    @DisplayName("Should return reorder report")
    void doGet_ReorderReport_ReturnsData() throws Exception {
        when(req.getPathInfo()).thenReturn("/reorder");
        Item item = new Item("DRY-001", "Rice", new BigDecimal("650"), "DRY", 50);
        when(itemRepo.findAll()).thenReturn(Arrays.asList(item));
        when(stockService.getTotalStockCount("DRY-001")).thenReturn(10);

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("DRY-001"));
        assertTrue(json.contains("count"));
    }

    @Test
    @DisplayName("Should return inventory report")
    void doGet_InventoryReport_ReturnsData() throws Exception {
        when(req.getPathInfo()).thenReturn("/inventory");
        Item item = new Item("DRY-001", "Rice", new BigDecimal("650"), "DRY", 50);
        when(itemRepo.findAll()).thenReturn(Arrays.asList(item));
        when(stockService.getStockByLocation("DRY-001", "SHELF")).thenReturn(20);
        when(stockService.getStockByLocation("DRY-001", "WEB")).thenReturn(15);
        when(stockService.getStockByLocation("DRY-001", "STORE")).thenReturn(30);
        when(stockService.getTotalStockCount("DRY-001")).thenReturn(65);

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("DRY-001"));
        assertTrue(json.contains("shelf"));
    }

    @Test
    @DisplayName("Should return bill report")
    void doGet_BillReport_ReturnsData() throws Exception {
        when(req.getPathInfo()).thenReturn("/bills");
        when(req.getParameter("type")).thenReturn(null);
        when(transRepo.getAllTransactions(null)).thenReturn(Collections.emptyList());

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertNotNull(json);
    }

    @Test
    @DisplayName("Should return reshelving report")
    void doGet_ReshelvingReport_ReturnsData() throws Exception {
        when(req.getPathInfo()).thenReturn("/reshelving");
        Item item = new Item("DRY-001", "Rice", new BigDecimal("650"), "DRY", 50);
        when(itemRepo.findAll()).thenReturn(Arrays.asList(item));
        when(stockService.getStockByLocation("DRY-001", "SHELF")).thenReturn(5);
        when(stockService.getStockByLocation("DRY-001", "STORE")).thenReturn(30);

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("DRY-001"));
    }

    @Test
    @DisplayName("Should return batches report")
    void doGet_BatchesReport_ReturnsData() throws Exception {
        when(req.getPathInfo()).thenReturn("/batches");
        when(itemRepo.findAll()).thenReturn(Collections.emptyList());

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertNotNull(json);
    }

    @Test
    @DisplayName("Should return 404 for unknown report")
    void doGet_UnknownReport_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/unknown");

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 for unknown POST endpoint")
    void doPost_UnknownEndpoint_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/unknown");

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}