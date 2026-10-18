package com.syos.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;


  //test client 2 — simulates mixed operations.
  //sends POS checkouts and WEB checkouts simultaneously
  //to test concurrent access from different client types.

public class TestClient2 {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int POS_REQUESTS = 200;
    private static final int WEB_REQUESTS = 200;
    private static final int TOTAL = POS_REQUESTS + WEB_REQUESTS;

    public static void main(String[] args) throws Exception {
        System.out.println("=== TEST CLIENT 2: Mixed POS + WEB Stress Test ===");
        System.out.println("Sending " + POS_REQUESTS + " POS + " + WEB_REQUESTS + " WEB requests simultaneously...\n");

        HttpClient client = HttpClient.newHttpClient();
        ExecutorService pool = Executors.newFixedThreadPool(TOTAL);

        CountDownLatch ready = new CountDownLatch(TOTAL);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(TOTAL);

        int[] success = {0};
        int[] failed = {0};

        // POS requests
        for (int i = 0; i < POS_REQUESTS; i++) {
            final int num = i + 1;
            pool.submit(() -> {
                try {
                    ready.countDown();
                    go.await();

                    String json = """
                        {
                            "transactionType": "POS",
                            "cashierUsername": "admin",
                            "cashTendered": 500,
                            "items": [{"itemCode": "BEV-001-2026", "quantity": 1}]
                        }
                        """.formatted(num);

                    HttpResponse<String> response = sendRequest(client, json);
                    handleResponse("POS", num, response, success, failed);
                } catch (Exception e) {
                    synchronized (failed) { failed[0]++; }
                    System.out.printf("  [ERR] POS %d — %s%n", num, e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        // WEB requests
        for (int i = 0; i < WEB_REQUESTS; i++) {
            final int num = i + 1;
            pool.submit(() -> {
                try {
                    ready.countDown();
                    go.await();

                    String json = """
                        {
                            "transactionType": "WEB",
                            "cashierUsername": "WEB-SYSTEM",
                            "cashTendered": 500,
                            "items": [{"itemCode": "BEV-001-2026", "quantity": 1}]
                        }
                        """.formatted();

                    HttpResponse<String> response = sendRequest(client, json);
                    handleResponse("WEB", num, response, success, failed);
                } catch (Exception e) {
                    synchronized (failed) { failed[0]++; }
                    System.out.printf("  [ERR] WEB %d — %s%n", num, e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        System.out.println("All " + TOTAL + " threads ready. Firing simultaneously!\n");
        go.countDown();

        done.await();
        pool.shutdown();

        System.out.println("\n=== RESULTS ===");
        System.out.println("Success: " + success[0]);
        System.out.println("Failed:  " + failed[0]);
        System.out.println("Total:   " + TOTAL);
    }

    private static HttpResponse<String> sendRequest(HttpClient client, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/billing/checkout"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void handleResponse(String type, int num, HttpResponse<String> response,
                                       int[] success, int[] failed) {
        if (response.statusCode() == 200 && response.body().contains("serialNumber")) {
            synchronized (success) { success[0]++; }
            System.out.printf("  [OK]  %s %d — Bill processed%n", type, num);
        } else {
            synchronized (failed) { failed[0]++; }
            System.out.printf("  [FAIL] %s %d — %s%n", type, num,
                    response.body().substring(0, Math.min(80, response.body().length())));
        }
    }
}