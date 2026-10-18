package com.syos.presentation.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.syos.domain.entity.Customer;
import com.syos.domain.entity.User;
import com.syos.domain.repository.CustomerRepository;
import com.syos.domain.repository.UserRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.syos.domain.repository.LoyaltyTierRepository;

import java.io.IOException;
import java.io.InputStreamReader;

@WebServlet("/api/auth/*") //HttpServlet has built in methods to check if url is post or get etc..
public class AuthServlet extends HttpServlet {

    private UserRepository userRepo;
    private CustomerRepository customerRepo;
    private Gson gson;
    private LoyaltyTierRepository loyaltyTierRepo;

    @Override
    public void init() {
        userRepo = (UserRepository) getServletContext().getAttribute("userRepo");
        customerRepo = (CustomerRepository) getServletContext().getAttribute("customerRepo");
        loyaltyTierRepo = (LoyaltyTierRepository) getServletContext().getAttribute("loyaltyTierRepo");
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        if ("/staff".equals(pathInfo)) {
            handleStaffLogin(req, resp);
        } else if ("/customer".equals(pathInfo)) {
            handleCustomerLogin(req, resp);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Unknown auth endpoint\"}");
        }
    }

    private void handleStaffLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        JsonObject body = gson.fromJson(
                new InputStreamReader(req.getInputStream()), JsonObject.class);

        String username = body.get("username").getAsString().trim();
        String password = body.get("password").getAsString().trim();
        String requiredRole = body.has("role") ? body.get("role").getAsString().trim() : "CASHIER";

        var userOpt = userRepo.authenticate(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // MANAGER can access everything, otherwise role must match
            boolean hasAccess = user.getRole().equalsIgnoreCase("MANAGER")
                    || user.getRole().equalsIgnoreCase(requiredRole);

            if (hasAccess) {
                // store in session so subsequent requests know who is logged in
                HttpSession session = req.getSession(true); //create session if not exists
                session.setAttribute("staffUser", user.getUsername());
                session.setAttribute("staffRole", user.getRole());

                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("username", user.getUsername());
                response.addProperty("role", user.getRole());
                resp.getWriter().write(gson.toJson(response));
                return;
            }
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"success\": false, \"error\": \"Invalid credentials or insufficient role\"}");
    }

    private void handleCustomerLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        JsonObject body = gson.fromJson(
                new InputStreamReader(req.getInputStream()), JsonObject.class);

        String email = body.get("email").getAsString().trim().toLowerCase();
        String password = body.get("password").getAsString().trim();

        var customerOpt = customerRepo.authenticate(email, password);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // Store in session
            HttpSession session = req.getSession(true);
            session.setAttribute("customerId", customer.getCustomerId());
            session.setAttribute("customerName", customer.getFullName());

            // Build response manually — never send password back to browser
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("customerId", customer.getCustomerId());
            response.addProperty("fullName", customer.getFullName());
            response.addProperty("email", customer.getEmail());
            response.addProperty("loyaltyPoints", customer.getLoyaltyPoints());
            response.addProperty("customerTier", customer.getCustomerTier());

            var tierOpt = loyaltyTierRepo.findByName(customer.getCustomerTier());

            if (tierOpt.isPresent()) {
                response.addProperty("tierDiscountPercent", tierOpt.get().getDiscountPercent());
            } else {
                response.addProperty("tierDiscountPercent", 0);
            }

            response.addProperty("totalPurchases", customer.getTotalPurchases());

            resp.getWriter().write(gson.toJson(response));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"success\": false, \"error\": \"Invalid email or password\"}");
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.getWriter().write("{\"success\": true, \"message\": \"Logged out\"}");
    }
}