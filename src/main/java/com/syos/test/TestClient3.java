package com.syos.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

/**
 * Test Client 3 — Simulates rapid stock level queries.
 * Tests that stock level reads remain consistent under load.
 */
public class TestClient3 {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int TOTAL_REQUESTS = 200;

    private static final String[] ITEMS = {
            "BEV-001-2026", "DRY-001-2026", "DAI-001-2026",
            "SNK-001-2026", "PER-003-2026"
    };

    public static void main(String[] args) throws Exception {
        System.out.println("=== TEST CLIENT 4: Rapid Stock Query Test ===");
        System.out.println("Sending " + TOTAL_REQUESTS + " simultaneous stock level queries...\n");

        HttpClient client = HttpClient.newHttpClient();
        ExecutorService pool = Executors.newFixedThreadPool(TOTAL_REQUESTS);

        CountDownLatch ready = new CountDownLatch(TOTAL_REQUESTS);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(TOTAL_REQUESTS);

        int[] success = {0};
        int[] failed = {0};

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            final int num = i + 1;
            final String itemCode = ITEMS[i % ITEMS.length];

            pool.submit(() -> {
                try {
                    ready.countDown();
                    go.await();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/api/stock/levels/" + itemCode))
                            .GET()
                            .build();

                    long start = System.currentTimeMillis();
                    HttpResponse<String> response = client.send(request,
                            HttpResponse.BodyHandlers.ofString());
                    long elapsed = System.currentTimeMillis() - start;

                    if (response.statusCode() == 200 && response.body().contains("shelf")) {
                        synchronized (success) { success[0]++; }
                        System.out.printf("  [OK]  Query %2d — %s — %dms%n", num, itemCode, elapsed);
                    } else {
                        synchronized (failed) { failed[0]++; }
                        System.out.printf("  [FAIL] Query %2d — %s — %s%n", num, itemCode,
                                response.body().substring(0, Math.min(60, response.body().length())));
                    }
                } catch (Exception e) {
                    synchronized (failed) { failed[0]++; }
                    System.out.printf("  [ERR] Query %2d — %s%n", num, e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        System.out.println("All " + TOTAL_REQUESTS + " threads ready. Firing simultaneously!\n");
        go.countDown();

        done.await();
        pool.shutdown();

        System.out.println("\n=== RESULTS ===");
        System.out.println("Success: " + success[0]);
        System.out.println("Failed:  " + failed[0]);
        System.out.println("Total:   " + TOTAL_REQUESTS);
    }
}