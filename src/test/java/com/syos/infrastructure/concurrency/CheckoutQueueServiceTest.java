package com.syos.infrastructure.concurrency;

import com.syos.application.dto.BillItemRequest;
import com.syos.application.dto.CheckoutRequest;
import com.syos.application.service.BillingService;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutQueueService Tests")
class CheckoutQueueServiceTest {

    @Mock
    private BillingService billingService;

    private CheckoutQueueService queueService;

    @BeforeEach
    void setUp() {
        queueService = new CheckoutQueueService(billingService);
    }

    @AfterEach
    void tearDown() {
        queueService.shutdown();
    }

    private CheckoutRequest createSimpleRequest() {
        return CheckoutRequest.builder()
                .setItems(Collections.singletonList(new BillItemRequest("DRY-001", 1)))
                .setCashTendered(new BigDecimal("1000"))
                .setPaymentType("CASH")
                .setTransactionType("POS")
                .setCashierUsername("admin")
                .build();
    }

    private Bill createMockBill(int serial) {
        return new Bill.Builder()
                .setSerial(serial)
                .setItems(Collections.singletonList(
                        new BillItem("DRY-001", "Rice", 1, new BigDecimal("650"))))
                .setTotal(new BigDecimal("650"))
                .setDiscount(BigDecimal.ZERO)
                .setPaymentType("CASH")
                .setTransactionType("POS")
                .setCashierUsername("admin")
                .setCashTendered(new BigDecimal("1000"))
                .setChangeAmount(new BigDecimal("350"))
                .build();
    }

    @Test
    @DisplayName("Should process a single checkout request successfully")
    void submit_SingleRequest_Success() throws Exception {
        Bill mockBill = createMockBill(1001);
        when(billingService.processCheckout(any())).thenReturn(mockBill);

        Future<Bill> future = queueService.submit(createSimpleRequest());
        Bill result = future.get(10, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(1001, result.getSerialNumber());
        verify(billingService).processCheckout(any());
    }

    @Test
    @DisplayName("Should process multiple requests successfully")
    void submit_MultipleRequests_AllProcessed() throws Exception {
        when(billingService.processCheckout(any()))
                .thenReturn(createMockBill(1001));

        Future<Bill> f1 = queueService.submit(createSimpleRequest());
        Future<Bill> f2 = queueService.submit(createSimpleRequest());
        Future<Bill> f3 = queueService.submit(createSimpleRequest());

        assertNotNull(f1.get(10, TimeUnit.SECONDS));
        assertNotNull(f2.get(10, TimeUnit.SECONDS));
        assertNotNull(f3.get(10, TimeUnit.SECONDS));

        verify(billingService, times(3)).processCheckout(any());
    }

    @Test
    @DisplayName("Should return failed future when billing service throws exception")
    void submit_ServiceThrows_FutureCompletesExceptionally() throws Exception {
        when(billingService.processCheckout(any()))
                .thenThrow(new RuntimeException("Insufficient stock"));

        Future<Bill> future = queueService.submit(createSimpleRequest());

        assertThrows(Exception.class, () -> future.get(10, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should report queue size correctly")
    void getQueueSize_ReturnsCorrectSize() {
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    @DisplayName("Should shutdown gracefully")
    void shutdown_CompletesWithoutError() {
        assertDoesNotThrow(() -> queueService.shutdown());
    }

    @Test
    @DisplayName("Should handle concurrent submissions")
    void submit_ConcurrentRequests_AllSucceed() throws Exception {
        when(billingService.processCheckout(any())).thenAnswer(invocation -> {
            Thread.sleep(50); // simulate processing time
            return createMockBill((int) (Math.random() * 10000));
        });

        Future<Bill>[] futures = new Future[5];
        for (int i = 0; i < 5; i++) {
            futures[i] = queueService.submit(createSimpleRequest());
        }

        int successCount = 0;
        for (Future<Bill> f : futures) {
            Bill result = f.get(10, TimeUnit.SECONDS);
            if (result != null) successCount++;
        }

        assertEquals(5, successCount);
        verify(billingService, times(5)).processCheckout(any());
    }
}