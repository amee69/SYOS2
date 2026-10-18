package com.syos.presentation.web;

import com.syos.application.factory.ValidationServiceFactory;
import com.syos.application.service.BillingService;
import com.syos.application.service.ValidationService;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.infrastructure.concurrency.CheckoutQueueService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingServlet Tests")
class BillingServletTest {

    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private BillingService billingService;
    @Mock private ValidationServiceFactory validationFactory;
    @Mock private ValidationService validationService;
    @Mock private CheckoutQueueService checkoutQueue;
    @Mock private HttpSession session;
    @Mock private ServletContext servletContext;

    private BillingServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(servletContext.getAttribute("billingService")).thenReturn(billingService);
        when(servletContext.getAttribute("validationFactory")).thenReturn(validationFactory);
        when(servletContext.getAttribute("checkoutQueue")).thenReturn(checkoutQueue);

        servlet = new BillingServlet() {
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };
        servlet.init();
    }

    private void setRequestBody(String json) throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes());
        when(req.getInputStream()).thenReturn(new jakarta.servlet.ServletInputStream() {
            public int read() { return stream.read(); }
            public boolean isFinished() { return stream.available() == 0; }
            public boolean isReady() { return true; }
            public void setReadListener(jakarta.servlet.ReadListener l) {}
        });
    }

    private Bill createMockBill() {
        return new Bill.Builder()
                .setSerial(1001)
                .setItems(Collections.singletonList(
                        new BillItem("DRY-001", "Rice", 1, new BigDecimal("650"))))
                .setTotal(new BigDecimal("650"))
                .setDiscount(BigDecimal.ZERO)
                .setPaymentType("CASH")
                .setTransactionType("POS")
                .setCashierUsername("admin")
                .setCashTendered(new BigDecimal("1000"))
                .setChangeAmount(new BigDecimal("350"))
                .build();
    }

    @Test
    @DisplayName("Should return 404 for unknown endpoint")
    void doPost_UnknownEndpoint_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/unknown");

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 400 when no items provided")
    void checkout_NoItems_Returns400() throws Exception {
        when(req.getPathInfo()).thenReturn("/checkout");
        setRequestBody("{\"transactionType\":\"POS\",\"cashTendered\":1000,\"items\":[]}");

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseWriter.toString().contains("No items provided"));
    }

    @Test
    @DisplayName("Should process checkout successfully")
    void checkout_ValidRequest_ReturnsSuccess() throws Exception {
        when(req.getPathInfo()).thenReturn("/checkout");
        setRequestBody("{\"transactionType\":\"POS\",\"cashTendered\":1000," +
                "\"items\":[{\"itemCode\":\"DRY-001\",\"quantity\":1}]}");

        when(validationFactory.create("SHELF")).thenReturn(validationService);
        when(validationService.validateItem(any())).thenReturn(null);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("staffUser")).thenReturn("admin");

        Bill mockBill = createMockBill();
        Future<Bill> future = CompletableFuture.completedFuture(mockBill);
        when(checkoutQueue.submit(any())).thenReturn(future);

        servlet.doPost(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("serialNumber"));
        assertTrue(json.contains("1001"));
    }

    @Test
    @DisplayName("Should return 503 on timeout")
    void checkout_Timeout_Returns503() throws Exception {
        when(req.getPathInfo()).thenReturn("/checkout");
        setRequestBody("{\"transactionType\":\"POS\",\"cashTendered\":1000," +
                "\"items\":[{\"itemCode\":\"DRY-001\",\"quantity\":1}]}");

        when(validationFactory.create("SHELF")).thenReturn(validationService);
        when(validationService.validateItem(any())).thenReturn(null);
        when(req.getSession(false)).thenReturn(null);

        Future<Bill> future = mock(Future.class);
        when(future.get(anyLong(), any(TimeUnit.class)))
                .thenThrow(new TimeoutException());
        when(checkoutQueue.submit(any())).thenReturn(future);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        assertTrue(responseWriter.toString().contains("Server is busy"));
    }

    @Test
    @DisplayName("Should return 400 when item validation fails")
    void checkout_ValidationFails_Returns400() throws Exception {
        when(req.getPathInfo()).thenReturn("/checkout");
        setRequestBody("{\"transactionType\":\"POS\",\"cashTendered\":1000," +
                "\"items\":[{\"itemCode\":\"FAKE-001\",\"quantity\":1}]}");

        when(validationFactory.create("SHELF")).thenReturn(validationService);
        when(validationService.validateItem(any())).thenReturn("Item not found: FAKE-001");

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseWriter.toString().contains("Item not found"));
    }

    @Test
    @DisplayName("Should use WEB-SYSTEM for web transactions")
    void checkout_WebTransaction_UsesWebSystem() throws Exception {
        when(req.getPathInfo()).thenReturn("/checkout");
        setRequestBody("{\"transactionType\":\"WEB\",\"cashTendered\":650," +
                "\"items\":[{\"itemCode\":\"DRY-001\",\"quantity\":1}]}");

        when(validationFactory.create("WEB")).thenReturn(validationService);
        when(validationService.validateItem(any())).thenReturn(null);
        when(req.getSession(false)).thenReturn(null);

        Bill mockBill = createMockBill();
        Future<Bill> future = CompletableFuture.completedFuture(mockBill);
        when(checkoutQueue.submit(any())).thenReturn(future);

        servlet.doPost(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("serialNumber"));
    }
}