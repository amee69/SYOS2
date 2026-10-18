package com.syos.infrastructure.concurrency;

import com.syos.application.dto.CheckoutRequest;
import com.syos.application.service.BillingService;
import com.syos.domain.entity.Bill;

import java.util.concurrent.*;


//  producer consumer pattern for checkout processing
 // producers os servlets receiving http requests
 // consumers is thread pool workers processing tasks mutual exclusion via billingservice lock

public class CheckoutQueueService {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    // blockingQueue blocks when empty
    // bounded waiting
    private final BlockingQueue<CheckoutTask> requestQueue = new LinkedBlockingQueue<>(1000); //fifo, also polymorphsim

    private final BillingService billingService;
    private final Thread queueProcessor;
    private volatile boolean running = true;

    public CheckoutQueueService(BillingService billingService) {
        this.billingService = billingService;

        //recheck
        //  thread runs nonsotp taking tasks from the queue and giving them to workers
        queueProcessor = new Thread(() -> {
            while (running) {
                try {
                    // .take() blcoks until a task arrives
                    CheckoutTask task = requestQueue.take();
                    // hand the task to a worker thread from the pool
                    threadPool.submit(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "checkout-queue-processor");

        queueProcessor.setDaemon(true); //dies when stopped
        queueProcessor.start();
        System.out.println("SYOS: Checkout queue service started (pool=10, queue=bounded).");
    }


//    public Future<Bill> submit(CheckoutRequest request) {
//        CheckoutTask task = new CheckoutTask(request, billingService);
//        requestQueue.offer(task);  // add to queue
//        return task.getFuture();
//    }

    //submit a checkout request to the queue.
    //returns a future so the servlet can wait for the result.
    public Future<Bill> submit(CheckoutRequest request) {
    CheckoutTask task = new CheckoutTask(request, billingService);
        boolean accepted = requestQueue.offer(task);
        if (!accepted) {
        // queue is full, reject immediately
        CompletableFuture<Bill> rejected = new CompletableFuture<>();
        rejected.completeExceptionally(
                new RuntimeException("Server is busy. Please try again.")
        );
            return rejected;
        }
        return task.getFuture();
}

    public int getQueueSize() {
        return requestQueue.size();
    }

    public void shutdown() {
        running = false;
        queueProcessor.interrupt();
        threadPool.shutdown();
        System.out.println("SYOS: Checkout queue service shut down.");
    }


      //implements runnable  the thread therfor can execute it

    private static class CheckoutTask implements Runnable {

        private final CheckoutRequest request;
        private final BillingService billingService;
        // cmakes servlet wait for this tasks result
        private final CompletableFuture<Bill> future = new CompletableFuture<>();

        CheckoutTask(CheckoutRequest request, BillingService billingService) {
            this.request = request;
            this.billingService = billingService;
        }

        @Override
        public void run() {
            try {
                // delegation to billing service
                Bill bill = billingService.processCheckout(request);
                future.complete(bill);         // signal success
            } catch (Exception e) {
                future.completeExceptionally(e); // signal failure
            }
        }

        public CompletableFuture<Bill> getFuture() { //getters for future

            return future;
        }
    }
}