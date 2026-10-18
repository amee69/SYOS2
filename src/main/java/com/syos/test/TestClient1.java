package com.syos.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;


 //Test Client 1 — Simulates multiple POS cashiers.
 // Sends  checkout requests simultaneously to test server concurrency.

public class TestClient1 {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int TOTAL_REQUESTS = 2;

    public static void main(String[] args) throws Exception {
        System.out.println("=== TEST CLIENT 1: POS Stress Test ===");
        System.out.println("Sending " + TOTAL_REQUESTS + " simultaneous POS checkouts...\n");

        HttpClient client = HttpClient.newHttpClient();

        // thread pool with 10 threads  all fire at the same time
        ExecutorService pool = Executors.newFixedThreadPool(TOTAL_REQUESTS);

        // countDownLatch makes all threads wait then fire simultaneously
        CountDownLatch ready = new CountDownLatch(TOTAL_REQUESTS);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(TOTAL_REQUESTS);

        int[] success = {0}; //reference
        int[] failed = {0};


        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            final int requestNum = i + 1;

            pool.submit(() -> {
                try {
                    ready.countDown();   // signal ready
                    go.await();          // wait for the "go" signal

                    String json = """
                        {
                            "transactionType": "POS",
                            "cashierUsername": "admin",
                            "cashTendered": 1000,
                            "items": [{"itemCode": "BEV-001-2026", "quantity": 1}]
                        }
                        """.formatted(requestNum);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/api/billing/checkout"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    long start = System.currentTimeMillis();
                    HttpResponse<String> response = client.send(request,
                            HttpResponse.BodyHandlers.ofString());
                    long elapsed = System.currentTimeMillis() - start;

                    if (response.statusCode() == 200 && response.body().contains("serialNumber")) {
                        synchronized (success) {
                            success[0]++;
                        }
                        System.out.printf("  [OK]  Request %2d — %dms — %s%n",
                                requestNum, elapsed, extractBillNum(response.body()));
                    } else {
                        synchronized (failed) { failed[0]++; }
                        System.out.printf("  [FAIL] Request %2d — %dms — %s%n",
                                requestNum, elapsed, response.body().substring(0, Math.min(80, response.body().length())));
                    }

                } catch (Exception e) {
                    synchronized (failed) { failed[0]++; }
                    System.out.printf("  [ERR] Request %2d — %s%n", requestNum, e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        // Wait for all threads to be ready, then fire
        ready.await();
        System.out.println("All " + TOTAL_REQUESTS + " threads ready. Firing simultaneously!\n");
        go.countDown();
        // Wait for all to finish
        done.await();
        pool.shutdown();

        System.out.println("\n=== RESULTS ===");
        System.out.println("Success: " + success[0]);
        System.out.println("Failed:  " + failed[0]);
        System.out.println("Total:   " + TOTAL_REQUESTS);
    }

    private static String extractBillNum(String json) {
        int idx = json.indexOf("\"serialNumber\":");
        if (idx == -1) return "";
        int start = idx + 15;
        int end = json.indexOf(",", start);
        return "Bill #" + json.substring(start, end).trim();
    }
}