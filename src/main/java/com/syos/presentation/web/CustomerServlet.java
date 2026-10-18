package com.syos.presentation.web;

import com.google.gson.*;
import com.syos.application.dto.CustomerRegistrationRequest;
import com.syos.application.service.CustomerService;
import com.syos.domain.entity.Customer;
import com.syos.domain.repository.CustomerRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStreamReader;

@WebServlet("/api/customers/*")
public class CustomerServlet extends HttpServlet {

    private CustomerService customerService;
    private CustomerRepository customerRepo;
    private Gson gson;

    @Override
    public void init() {
        customerService = (CustomerService) getServletContext().getAttribute("customerService");
        customerRepo = (CustomerRepository) getServletContext().getAttribute("customerRepo");
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        if ("/register".equals(pathInfo)) {
            handleRegistration(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Unknown endpoint\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        // GET /api/customers/by-email?email=somehting used by pos to look up customer
        if ("/by-email".equals(pathInfo)) {
            String email = req.getParameter("email");
            if (email == null || email.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"email parameter required\"}");
                return;
            }

            var customerOpt = customerRepo.findByEmail(email.trim().toLowerCase());
            if (customerOpt.isPresent()) {
                Customer c = customerOpt.get();
                JsonObject response = new JsonObject();
                response.addProperty("customerId", c.getCustomerId());
                response.addProperty("fullName", c.getFullName());
                response.addProperty("email", c.getEmail());
                response.addProperty("loyaltyPoints", c.getLoyaltyPoints());
                response.addProperty("customerTier", c.getCustomerTier());

                var loyaltyTierRepo = (com.syos.domain.repository.LoyaltyTierRepository)
                        getServletContext().getAttribute("loyaltyTierRepo");
                var tierOpt = loyaltyTierRepo.findByName(c.getCustomerTier());
                if (tierOpt.isPresent()) {
                    response.addProperty("tierDiscountPercent", tierOpt.get().getDiscountPercent());
                } else {
                    response.addProperty("tierDiscountPercent", 0);
                }
                
                response.addProperty("totalPurchases", c.getTotalPurchases());
                resp.getWriter().write(gson.toJson(response));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Customer not found\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Unknown endpoint\"}");
        }
    }

    private void handleRegistration(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            JsonObject body = gson.fromJson(new InputStreamReader(req.getInputStream()), JsonObject.class);

            String fullName = body.get("fullName").getAsString().trim();
            String email = body.get("email").getAsString().trim().toLowerCase();
            String phone = body.get("phone").getAsString().trim();
            String password = body.get("password").getAsString().trim();

            // basic validations (added just for pragamtism)
            if (fullName.isEmpty()) {
                badRequest(resp, "Full name cannot be empty");
                return;
            }
            if (!email.contains("@")) {
                badRequest(resp, "Invalid email format");
                return;
            }
            if (!phone.matches("\\d+") || phone.isEmpty()) {
                badRequest(resp, "Phone must contain numbers only");
                return;
            }
            if (password.length() < 4) {
                badRequest(resp, "Password must be at least 4 characters");
                return;
            }

            CustomerRegistrationRequest request =
                    new CustomerRegistrationRequest(fullName, email, phone, password);

            Customer newCustomer = customerService.register(request);

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("customerId", newCustomer.getCustomerId());
            response.addProperty("fullName", newCustomer.getFullName());
            response.addProperty("email", newCustomer.getEmail());
            response.addProperty("customerTier", newCustomer.getCustomerTier());
            response.addProperty("loyaltyPoints", newCustomer.getLoyaltyPoints());
            response.addProperty("message", "Registration successful! Welcome to SYOS.");
            resp.getWriter().write(gson.toJson(response));

        } catch (IllegalArgumentException e) {
            badRequest(resp, e.getMessage());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void badRequest(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"success\": false, \"error\": \"" + message + "\"}");
    }
}