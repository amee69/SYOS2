package com.syos.presentation.web;

import com.google.gson.*;
import com.syos.application.dto.ItemCreateRequest;
import com.syos.application.dto.StockBatchRequest;
import com.syos.application.service.ItemManagementFacade;
import com.syos.application.service.StockService;
import com.syos.domain.entity.Category;
import com.syos.domain.entity.Item;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.syos.infrastructure.web.UpdateWebSocket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/stock/*")
public class StockServlet extends HttpServlet {

    private StockService stockService;
    private ItemManagementFacade itemFacade;
    private ItemRepository itemRepo;
    private StockRepository stockRepo;
    private Gson gson;

    @Override
    public void init() {
        stockService = (StockService) getServletContext().getAttribute("stockService");
        itemFacade = (ItemManagementFacade) getServletContext().getAttribute("itemFacade");
        itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        stockRepo = (StockRepository) getServletContext().getAttribute("stockRepo");
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if ("/categories".equals(pathInfo)) {
                // GET /api/stock/categories
                List<Category> categories = itemFacade.getCategories();
                resp.getWriter().write(gson.toJson(categories));

            } else if (pathInfo != null && pathInfo.startsWith("/levels/")) {
                // GET /api/stock/levels/{itemCode}
                String itemCode = pathInfo.substring("/levels/".length());
                String location = req.getParameter("location");

                JsonObject response = new JsonObject();
                response.addProperty("itemCode", itemCode);

                if (location != null) {
                    int qty = stockService.getStockByLocation(itemCode, location.toUpperCase());
                    response.addProperty("location", location.toUpperCase());
                    response.addProperty("quantity", qty);
                } else {
                    int shelf = stockService.getStockByLocation(itemCode, "SHELF");
                    int web = stockService.getStockByLocation(itemCode, "WEB");
                    int store = stockService.getStockByLocation(itemCode, "STORE");
                    response.addProperty("shelf", shelf);
                    response.addProperty("web", web);
                    response.addProperty("store", store);
                    response.addProperty("total", shelf + web + store);
                }

                resp.getWriter().write(gson.toJson(response));

            } else if ("/batches".equals(pathInfo)) {
                // GET /api/stock/batches?itemCode=XXX
                String itemCode = req.getParameter("itemCode");
                if (itemCode == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"itemCode parameter required\"}");
                    return;
                }
                List<StockBatch> batches = stockRepo.getBatchesByItemCode(itemCode);
                resp.getWriter().write(gson.toJson(batches));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Unknown stock endpoint\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if ("/batch".equals(pathInfo)) {
                handleReceiveBatch(req, resp);

            } else if ("/move".equals(pathInfo)) {
                handleMoveStock(req, resp);

            } else if ("/items".equals(pathInfo)) {
                handleAddItem(req, resp);

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Unknown stock endpoint\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                String itemCode = pathInfo.substring("/items/".length());
                itemFacade.deleteItem(itemCode);

                UpdateWebSocket.broadcast("STOCK_UPDATED");

                resp.getWriter().write("{\"success\": true, \"message\": \"Item deleted: " + itemCode + "\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Unknown endpoint\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleReceiveBatch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        JsonObject body = gson.fromJson(
                new InputStreamReader(req.getInputStream()), JsonObject.class);

        String itemCode = body.get("itemCode").getAsString().trim();
        int quantity = body.get("quantity").getAsInt();
        LocalDate expiryDate = LocalDate.parse(body.get("expiryDate").getAsString());

        if (expiryDate.isBefore(LocalDate.now())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Expiry date cannot be in the past\"}");
            return;
        }

        if (!itemRepo.existsByCode(itemCode)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Item not found: " + itemCode + "\"}");
            return;
        }

        StockBatchRequest request = new StockBatchRequest(itemCode, quantity, expiryDate);
        stockService.addBatch(request);

        UpdateWebSocket.broadcast("STOCK_UPDATED");

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "Batch received successfully");
        response.addProperty("itemCode", itemCode);
        response.addProperty("quantity", quantity);
        response.addProperty("expiryDate", expiryDate.toString());
        resp.getWriter().write(gson.toJson(response));
    }

    private void handleMoveStock(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        JsonObject body = gson.fromJson(
                new InputStreamReader(req.getInputStream()), JsonObject.class);

        String itemCode = body.get("itemCode").getAsString().trim();
        int quantity = body.get("quantity").getAsInt();
        String targetLocation = body.get("targetLocation").getAsString().trim().toUpperCase();

        if (!targetLocation.equals("SHELF") && !targetLocation.equals("WEB")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Target location must be SHELF or WEB\"}");
            return;
        }

        int beforeStock = stockService.getStockByLocation(itemCode, targetLocation);
        stockService.moveStockToLocation(itemCode, quantity, targetLocation);

        UpdateWebSocket.broadcast("STOCK_UPDATED");

        int afterStock = stockService.getStockByLocation(itemCode, targetLocation);

        int actualMoved = afterStock - beforeStock;

        JsonObject response = new JsonObject();
        response.addProperty("itemCode", itemCode);
        response.addProperty("targetLocation", targetLocation);
        response.addProperty("quantityMoved", actualMoved);
        response.addProperty("newStockLevel", afterStock);

        if (actualMoved == 0) {
            // check if batches are expired
            var batches = stockRepo.getBatchesByItemCode(itemCode);
            boolean allExpired = batches.stream()
                    .allMatch(b -> b.getExpiryDate().isBefore(java.time.LocalDate.now()));

            response.addProperty("success", false);
            if (allExpired && !batches.isEmpty()) {
                response.addProperty("error", "All available batches for " + itemCode +
                        " are expired. Please dispose of expired stock and receive new batches.");
            } else {
                response.addProperty("error", "No stock was moved. Check warehouse availability.");
            }
        } else if (actualMoved < quantity) {
            response.addProperty("success", true);
            response.addProperty("warning", "Only " + actualMoved + " of " + quantity +
                    " units moved. Some batches may be expired.");
        } else {
            response.addProperty("success", true);
        }

        resp.getWriter().write(gson.toJson(response));
    }

    private void handleAddItem(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        JsonObject body = gson.fromJson(
                new InputStreamReader(req.getInputStream()), JsonObject.class);

        String name = body.get("name").getAsString().trim();
        BigDecimal price = body.get("unitPrice").getAsBigDecimal();
        String categoryCode = body.get("categoryCode").getAsString().trim();
        int reorderThreshold = body.has("reorderThreshold")
                ? body.get("reorderThreshold").getAsInt() : 50;

        ItemCreateRequest request = new ItemCreateRequest(name, price, categoryCode, reorderThreshold);
        Item newItem = itemFacade.createItem(request);

        UpdateWebSocket.broadcast("STOCK_UPDATED");

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("itemCode", newItem.getItemCode());
        response.addProperty("name", newItem.getName());
        response.addProperty("unitPrice", newItem.getUnitPrice());
        response.addProperty("category", newItem.getCategory());
        resp.getWriter().write(gson.toJson(response));
    }
}