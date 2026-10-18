package com.syos.presentation.web;

import com.syos.application.service.CustomerService;
import com.syos.domain.entity.Customer;
import com.syos.domain.repository.CustomerRepository;
import com.syos.domain.repository.LoyaltyTierRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServlet Tests")
class CustomerServletTest {

    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private CustomerService customerService;
    @Mock private CustomerRepository customerRepo;
    @Mock private LoyaltyTierRepository loyaltyTierRepo;
    @Mock private ServletContext servletContext;

    private CustomerServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(servletContext.getAttribute("customerService")).thenReturn(customerService);
        when(servletContext.getAttribute("customerRepo")).thenReturn(customerRepo);
        lenient().when(servletContext.getAttribute("loyaltyTierRepo")).thenReturn(loyaltyTierRepo);

        servlet = new CustomerServlet() {
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };
        servlet.init();
    }

    @Test
    @DisplayName("Should return customer by email")
    void doGet_ValidEmail_ReturnsCustomer() throws Exception {
        when(req.getPathInfo()).thenReturn("/by-email");
        when(req.getParameter("email")).thenReturn("test@mail.com");

        Customer customer = new Customer(1, "test@mail.com", "Test User", 50, new BigDecimal("10000"), "SILVER");
        when(customerRepo.findByEmail("test@mail.com")).thenReturn(Optional.of(customer));

        servlet.doGet(req, resp);

        String json = responseWriter.toString();
        assertTrue(json.contains("Test User"));
        assertTrue(json.contains("test@mail.com"));
    }

    @Test
    @DisplayName("Should return 404 for unknown customer")
    void doGet_UnknownEmail_Returns404() throws Exception {
        when(req.getPathInfo()).thenReturn("/by-email");
        when(req.getParameter("email")).thenReturn("nobody@mail.com");
        when(customerRepo.findByEmail("nobody@mail.com")).thenReturn(Optional.empty());

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 400 when email parameter missing")
    void doGet_NoEmail_Returns400() throws Exception {
        when(req.getPathInfo()).thenReturn("/by-email");
        when(req.getParameter("email")).thenReturn(null);

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
}