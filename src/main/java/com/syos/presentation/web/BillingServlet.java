package com.syos.presentation.web;

import com.google.gson.*;
import com.syos.application.dto.BillItemRequest;
import com.syos.application.dto.CheckoutRequest;
import com.syos.application.service.BillingService;
import com.syos.application.service.ValidationService;
import com.syos.application.factory.ValidationServiceFactory;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.infrastructure.web.UpdateWebSocket;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.syos.infrastructure.concurrency.CheckoutQueueService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@WebServlet("/api/billing/*")
public class BillingServlet extends HttpServlet {

    private BillingService billingService;
    private ValidationServiceFactory validationFactory;
    private Gson gson;

    private CheckoutQueueService checkoutQueue;

    @Override
    public void init() {
        checkoutQueue = (CheckoutQueueService) getServletContext().getAttribute("checkoutQueue");
        billingService = (BillingService) getServletContext().getAttribute("billingService");
        validationFactory = (ValidationServiceFactory) getServletContext().getAttribute("validationFactory");
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    //overrides the java written doPost method, if it wasnt written, it would return 405 error
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) //inherit frin httpservelt
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        if ("/checkout".equals(pathInfo)) {
            handleCheckout(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Unknown billing endpoint\"}");
        }
    }

    private void handleCheckout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
//            JsonObject body = gson.fromJson(new InputStreamReader(req.getInputStream()), JsonObject.class);

            // get raw network data
            java.io.InputStream rawBytes = req.getInputStream();

            // translate raw data into text
            java.io.InputStreamReader textTranslator = new java.io.InputStreamReader(rawBytes);

            // parse text into a Java object
            JsonObject body = gson.fromJson(textTranslator, JsonObject.class);

            // read transaction type
            String transactionType = body.has("transactionType")
                    ? body.get("transactionType").getAsString() : "POS";

            // read items from request
            JsonArray itemsArray = body.getAsJsonArray("items");
            if (itemsArray == null || itemsArray.size() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"No items provided\"}");
                return;
            }

            //validation before prcses
            String stockType = transactionType.equalsIgnoreCase("WEB") ? "WEB" : "SHELF";
            ValidationService validator = validationFactory.create(stockType);
            List<BillItemRequest> itemRequests = new ArrayList<>();

            for (JsonElement el : itemsArray) {
                JsonObject itemObj = el.getAsJsonObject();
                String code = itemObj.get("itemCode").getAsString();
                int qty = itemObj.get("quantity").getAsInt();

                BillItemRequest itemReq = new BillItemRequest(code, qty);
                String validationError = validator.validateItem(itemReq);
                if (validationError != null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"" + validationError + "\"}");
                    return;
                }
                itemRequests.add(itemReq);
            }

            //read payment info
            BigDecimal cashTendered = body.has("cashTendered")
                    ? body.get("cashTendered").getAsBigDecimal()
                    : BigDecimal.ZERO;

            // read optional customer and discount
            Integer customerId = body.has("customerId") && !body.get("customerId").isJsonNull()
                    ? body.get("customerId").getAsInt() : null;

            int loyaltyPointsToRedeem = body.has("loyaltyPointsToRedeem")
                    ? body.get("loyaltyPointsToRedeem").getAsInt() : 0;



            // get cashier from session
            HttpSession session = req.getSession(false); //get existin session or return null)
            String cashierUsername = "WEB-SYSTEM";
            if (session != null && session.getAttribute("staffUser") != null) {
                cashierUsername = (String) session.getAttribute("staffUser");
            } else if (transactionType.equalsIgnoreCase("WEB")) {
                cashierUsername = "WEB-SYSTEM";
            } else if (body.has("cashierUsername") && !body.get("cashierUsername").isJsonNull()) { //added to support client testing as fall back
                cashierUsername = body.get("cashierUsername").getAsString();
            }

            // build checkout request
            String paymentType = transactionType.equalsIgnoreCase("WEB") ? "ONLINE" : "CASH";

            CheckoutRequest checkoutRequest = CheckoutRequest.builder()
                    .setItems(itemRequests)
                    .setTransactionType(transactionType)
                    .setCashTendered(cashTendered)
                    .setPaymentType(paymentType)
                    .setCustomerId(customerId)
                    .setCashierUsername(cashierUsername)
                    .setLoyaltyPointsToRedeem(loyaltyPointsToRedeem)
                    .setDiscountDetails(BigDecimal.ZERO, "")
                    .build();

            // submit to queue and wait for result
            Future<Bill> future = checkoutQueue.submit(checkoutRequest);
            Bill bill = future.get(30, TimeUnit.SECONDS);

            // build response
            UpdateWebSocket.broadcast("STOCK_UPDATED");

            JsonObject response = buildBillResponse(bill);
            resp.getWriter().write(gson.toJson(response));

        } catch (java.util.concurrent.TimeoutException e) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("{\"error\": \"Server is busy. Please try again.\"}");
        } catch (java.util.concurrent.ExecutionException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getCause().getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // converts Bill entity to json response
    private JsonObject buildBillResponse(Bill bill) {
        JsonObject response = new JsonObject();
        response.addProperty("serialNumber", bill.getSerialNumber());
        response.addProperty("billDate", bill.getBillDate().toString());
        response.addProperty("transactionType", bill.getTransactionType());
        response.addProperty("cashierUsername", bill.getCashierUsername());
        response.addProperty("totalFullPrice", bill.getTotalFullPrice());
        response.addProperty("totalDiscount", bill.getTotalDiscount());
        response.addProperty("netTotal", bill.getNetTotal());
        response.addProperty("cashTendered", bill.getCashTendered());
        response.addProperty("changeAmount", bill.getChangeAmount());
        response.addProperty("paymentType", bill.getPaymentType());
        response.addProperty("loyaltyPointsUsed", bill.getLoyaltyPointsUsed());
        response.addProperty("loyaltyPointsEarned", bill.getLoyaltyPointsEarned());

        JsonArray itemsArray = new JsonArray();
        for (BillItem item : bill.getBillItems()) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("itemCode", item.getItemCode());
            itemObj.addProperty("itemName", item.getItemName());
            itemObj.addProperty("quantity", item.getQuantity());
            itemObj.addProperty("unitPrice", item.getUnitPrice());
            itemObj.addProperty("totalPrice", item.getTotalPrice());
            itemsArray.add(itemObj);
        }
        response.add("items", itemsArray);

        return response;
    }
}