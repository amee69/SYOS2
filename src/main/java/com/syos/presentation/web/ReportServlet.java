package com.syos.presentation.web;

import com.google.gson.*;
import com.syos.application.service.StockService;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.entity.Item;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.TransactionRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/reports/*")
public class ReportServlet extends HttpServlet {

    private StockService stockService;
    private ItemRepository itemRepo;
    private StockRepository stockRepo;
    private TransactionRepository transRepo;
    private Gson gson;

    @Override
    public void init() {
        stockService = (StockService) getServletContext().getAttribute("stockService");
        itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        stockRepo = (StockRepository) getServletContext().getAttribute("stockRepo");
        transRepo = (TransactionRepository) getServletContext().getAttribute("transRepo");
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            switch (pathInfo == null ? "" : pathInfo) {

                case "/sales" -> handleSalesReport(req, resp);
                case "/reorder" -> handleReorderReport(resp);
                case "/inventory" -> handleInventoryReport(resp);
                case "/batches" -> handleBatchReport(resp);
                case "/bills" -> handleBillReport(req, resp);
                case "/reshelving" -> handleReshelvingReport(resp);

                default -> {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Unknown report type\"}");
                }
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

        if ("/generate".equals(pathInfo)) {
            handleGenerateFile(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Unknown endpoint\"}");
        }
    }

    private void handleGenerateFile(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            JsonObject body = gson.fromJson(
                    new InputStreamReader(req.getInputStream()), JsonObject.class);

            String reportType = body.get("reportType").getAsString();
            String filter = body.has("filter") && !body.get("filter").isJsonNull()
                    ? body.get("filter").getAsString() : null;

            // Use existing report generators — same ones the CLI uses
            com.syos.presentation.cli.factory.ReportGenerator generator = switch (reportType) {
                case "sales" -> {
                    var g = new com.syos.presentation.cli.factory.SalesGenerator(transRepo);
                    g.setTransactionType(filter);
                    yield g;
                }
                case "reorder"    -> new com.syos.presentation.cli.factory.ReorderGenerator(stockService, itemRepo);
                case "inventory"  -> new com.syos.presentation.cli.factory.InventoryGenerator(stockService, itemRepo);
                case "batches"    -> new com.syos.presentation.cli.factory.BatchStockGenerator(stockRepo, itemRepo);
                case "bills" -> {
                    var g = new com.syos.presentation.cli.factory.BillReportGenerator(transRepo);
                    g.setTransactionType(filter);
                    yield g;
                }
                case "reshelving" -> new com.syos.presentation.cli.factory.ReshelvingGenerator(stockService, itemRepo);
                default -> null;
            };

            if (generator == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Unknown report type: " + reportType + "\"}");
                return;
            }

            // This calls AbstractReport.generate() → builds content → saves via FileHelper
            generator.run();

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Report saved to reports/ folder");
            response.addProperty("reportType", reportType);
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }






    // Daily sales report — items sold today with revenue
    private void handleSalesReport(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String transactionType = req.getParameter("type"); // null = ALL, POS, WEB
        List<TransactionRepository.ItemSaleSummary> sales =
                transRepo.getItemSalesByDate(LocalDate.now(), transactionType);

        JsonObject response = new JsonObject();
        response.addProperty("date", LocalDate.now().toString());
        response.addProperty("filter", transactionType == null ? "ALL" : transactionType);

        JsonArray itemsArray = new JsonArray();
        double grandTotal = 0;
        int totalQty = 0;

        for (TransactionRepository.ItemSaleSummary sale : sales) {
            JsonObject item = new JsonObject();
            item.addProperty("itemCode", sale.itemCode());
            item.addProperty("itemName", sale.itemName());
            item.addProperty("totalQuantity", sale.totalQuantity());
            item.addProperty("totalRevenue", sale.totalRevenue());
            itemsArray.add(item);
            grandTotal += sale.totalRevenue().doubleValue();
            totalQty += sale.totalQuantity();
        }

        response.add("items", itemsArray);
        response.addProperty("grandTotal", grandTotal);
        response.addProperty("totalQuantity", totalQty);
        resp.getWriter().write(gson.toJson(response));
    }

    // Reorder report — items below reorder threshold
    private void handleReorderReport(HttpServletResponse resp) throws IOException {
        List<Item> allItems = itemRepo.findAll();

        JsonObject response = new JsonObject();
        JsonArray itemsArray = new JsonArray();

        for (Item item : allItems) {
            int totalStock = stockService.getTotalStockCount(item.getItemCode());
            if (totalStock < item.getReorderLevel()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("itemCode", item.getItemCode());
                obj.addProperty("itemName", item.getName());
                obj.addProperty("currentStock", totalStock);
                obj.addProperty("reorderLevel", item.getReorderLevel());
                obj.addProperty("shortage", item.getReorderLevel() - totalStock);
                itemsArray.add(obj);
            }
        }

        response.add("items", itemsArray);
        response.addProperty("count", itemsArray.size());
        resp.getWriter().write(gson.toJson(response));
    }

    // Inventory report — stock levels by location for all items
    private void handleInventoryReport(HttpServletResponse resp) throws IOException {
        List<Item> allItems = itemRepo.findAll();

        JsonArray itemsArray = new JsonArray();

        for (Item item : allItems) {
            String code = item.getItemCode();
            JsonObject obj = new JsonObject();
            obj.addProperty("itemCode", code);
            obj.addProperty("itemName", item.getName());
            obj.addProperty("category", item.getCategory());
            obj.addProperty("unitPrice", item.getUnitPrice());
            obj.addProperty("shelf", stockService.getStockByLocation(code, "SHELF"));
            obj.addProperty("web", stockService.getStockByLocation(code, "WEB"));
            obj.addProperty("store", stockService.getStockByLocation(code, "STORE"));
            obj.addProperty("total", stockService.getTotalStockCount(code));
            itemsArray.add(obj);
        }

        resp.getWriter().write(gson.toJson(itemsArray));
    }

    // batch stock report, warehouse batches with expiry info
    private void handleBatchReport(HttpServletResponse resp) throws IOException {
        List<Item> allItems = itemRepo.findAll();
        JsonArray result = new JsonArray();

        for (Item item : allItems) {
            List<StockBatch> batches = stockRepo.getBatchesByItemCode(item.getItemCode());
            for (StockBatch batch : batches) {
                JsonObject obj = new JsonObject();
                obj.addProperty("batchId", batch.getBatchId());
                obj.addProperty("itemCode", item.getItemCode());
                obj.addProperty("itemName", item.getName());
                obj.addProperty("quantity", batch.getQuantity());
                obj.addProperty("purchaseDate", batch.getPurchaseDate().toString());
                obj.addProperty("expiryDate", batch.getExpiryDate().toString());
                boolean expired = batch.getExpiryDate().isBefore(LocalDate.now());
                boolean expiringSoon = !expired && batch.getExpiryDate()
                        .isBefore(LocalDate.now().plusDays(30));
                String status = expired ? "EXPIRED" : expiringSoon ? "EXPIRING_SOON" : "GOOD";
                obj.addProperty("status", status);
                result.add(obj);
            }
        }

        resp.getWriter().write(gson.toJson(result));
    }

    // bill report ->all transactions
    private void handleBillReport(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String transactionType = req.getParameter("type"); // null = ALL, POS, WEB
        List<Bill> bills = transRepo.getAllTransactions(transactionType);

        JsonArray billsArray = new JsonArray();

        for (Bill bill : bills) {
            JsonObject billObj = new JsonObject();
            billObj.addProperty("serialNumber", bill.getSerialNumber());
            billObj.addProperty("billDate", bill.getBillDate().toString());
            billObj.addProperty("transactionType", bill.getTransactionType());
            billObj.addProperty("cashierUsername", bill.getCashierUsername());
            billObj.addProperty("netTotal", bill.getNetTotal());
            billObj.addProperty("totalFullPrice", bill.getTotalFullPrice());
            billObj.addProperty("paymentType", bill.getPaymentType());

            JsonArray itemsArr = new JsonArray();
            for (BillItem item : bill.getBillItems()) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("itemName", item.getItemName());
                itemObj.addProperty("quantity", item.getQuantity());
                itemObj.addProperty("totalPrice", item.getTotalPrice());
                itemsArr.add(itemObj);
            }
            billObj.add("items", itemsArr);
            billsArray.add(billObj);
        }

        resp.getWriter().write(gson.toJson(billsArray));
    }

    // Reshelving report — items that need to be moved from store to shelf
//    private void handleReshelvingReport(HttpServletResponse resp) throws IOException {
//        List<Item> allItems = itemRepo.findAll();
//        JsonArray result = new JsonArray();
//
//        for (Item item : allItems) {
//            String code = item.getItemCode();
//            int shelfStock = stockService.getStockByLocation(code, "SHELF");
//            int storeStock = stockService.getStockByLocation(code, "STORE");
//
//            if (shelfStock < item.getReorderLevel() && storeStock > 0) {
//                JsonObject obj = new JsonObject();
//                obj.addProperty("itemCode", code);
//                obj.addProperty("itemName", item.getName());
//                obj.addProperty("shelfStock", shelfStock);
//                obj.addProperty("storeStock", storeStock);
//                obj.addProperty("reorderLevel", item.getReorderLevel());
//                obj.addProperty("suggestedMove",
//                        Math.min(storeStock, item.getReorderLevel() - shelfStock));
//                result.add(obj);
//            }
//        }
//
//        resp.getWriter().write(gson.toJson(result));
//    }

    private void handleReshelvingReport(HttpServletResponse resp) throws IOException {
        List<Item> allItems = itemRepo.findAll();
        JsonArray result = new JsonArray();

        for (Item item : allItems) {
            String code = item.getItemCode();
            int shelfStock = stockService.getStockByLocation(code, "SHELF");
            int webStock = stockService.getStockByLocation(code, "WEB");
            int storeStock = stockService.getStockByLocation(code, "STORE");

            // check shelf needs restocking
            if (shelfStock < item.getReorderLevel() && storeStock > 0) {
                JsonObject obj = new JsonObject();
                obj.addProperty("itemCode", code);
                obj.addProperty("itemName", item.getName());
                obj.addProperty("targetLocation", "SHELF");
                obj.addProperty("currentStock", shelfStock);
                obj.addProperty("storeStock", storeStock);
                obj.addProperty("reorderLevel", item.getReorderLevel());
                obj.addProperty("suggestedMove",
                        Math.min(storeStock, item.getReorderLevel() - shelfStock));
                result.add(obj);
            }

            // Check web needs restocking
            if (webStock < item.getReorderLevel() && storeStock > 0) {
                JsonObject obj = new JsonObject();
                obj.addProperty("itemCode", code);
                obj.addProperty("itemName", item.getName());
                obj.addProperty("targetLocation", "WEB");
                obj.addProperty("currentStock", webStock);
                obj.addProperty("storeStock", storeStock);
                obj.addProperty("reorderLevel", item.getReorderLevel());
                obj.addProperty("suggestedMove",
                        Math.min(storeStock, item.getReorderLevel() - webStock));
                result.add(obj);
            }
        }

        resp.getWriter().write(gson.toJson(result));
    }
}