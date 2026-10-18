package com.syos.application.service;

import com.syos.application.dto.BillItemRequest;
import com.syos.application.dto.CheckoutRequest;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Customer;
import com.syos.domain.entity.Item;
import com.syos.domain.entity.LoyaltyTier;
import com.syos.domain.repository.CustomerRepository;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.LoyaltyTierRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingService Logic Tests")
class BillingServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private StockRepository stockRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private StockService stockService;
    @Mock private LoyaltyTierRepository loyaltyTierRepository;
    @Mock private BillFileWriter billFileWriter;

    private BillingService billingService;

    private Item mockItem;
    private Customer mockCustomer;
    private BillItemRequest itemRequest;
    private LoyaltyTier mockTier;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(
                transactionRepository,
                customerRepository,
                stockRepository,
                itemRepository,
                stockService,
                loyaltyTierRepository,
                billFileWriter
        );

        mockItem = new Item("DRY-001", "Rice 5kg", new BigDecimal("1000.00"), "Food", 50);

        mockCustomer = new Customer(
                1, "john@mail.com", "John", 0, BigDecimal.ZERO, "BRONZE"
        );

        mockTier = new LoyaltyTier("BRONZE", BigDecimal.ZERO, 1, BigDecimal.ZERO);

        lenient().when(loyaltyTierRepository.findByName(anyString()))
                .thenReturn(Optional.of(mockTier));

        itemRequest = new BillItemRequest("DRY-001", 1);
    }

    @Nested
    @DisplayName("Process Checkout - Happy Paths")
    class HappyPathTests {

        @Test
        @DisplayName("Should process a valid cash sale successfully")
        void processCheckout_ValidCashSale_Success() {
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));
            when(transactionRepository.getNextSerialNumber()).thenReturn(101);

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(itemRequest))
                    .setCashTendered(new BigDecimal("1000.00"))
                    .setPaymentType("CASH")
                    .setTransactionType("POS")
                    .setCashierUsername("admin")
                    .build();

            Bill bill = billingService.processCheckout(request);

            assertNotNull(bill);
            assertEquals(101, bill.getSerialNumber());
            assertEquals(new BigDecimal("1000.00"), bill.getTotalFullPrice());

            verify(stockRepository).reduceStock("DRY-001", 1, "SHELF");
            verify(transactionRepository).saveTransaction(any(Bill.class));
            verify(stockService).checkLowStock("DRY-001");
            verify(billFileWriter).save(any(Bill.class));
        }

        @Test
        @DisplayName("Should process loyalty points earning for customer")
        void processCheckout_WithCustomer_EarnsPoints() {
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));
            when(customerRepository.findById(1)).thenReturn(Optional.of(mockCustomer));
            when(transactionRepository.getNextSerialNumber()).thenReturn(102);

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(itemRequest))
                    .setCashTendered(new BigDecimal("1000.00"))
                    .setPaymentType("CASH")
                    .setCustomerId(1)
                    .build();

            billingService.processCheckout(request);

            verify(customerRepository).updateLoyaltyPoints(eq(1), anyInt());
            verify(customerRepository).updateTotalPurchases(eq(1), any(BigDecimal.class));
        }
    }

    @Nested
    @DisplayName("Process Checkout - Error Scenarios")
    class ErrorTests {

        @Test
        @DisplayName("Should throw exception if item does not exist")
        void processCheckout_ItemNotFound_ThrowsException() {
            when(itemRepository.findByCode("INVALID-001")).thenReturn(Optional.empty());

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(new BillItemRequest("INVALID-001", 1)))
                    .setCashTendered(BigDecimal.TEN)
                    .setPaymentType("CASH")
                    .build();

            Exception e = assertThrows(RuntimeException.class, () ->
                    billingService.processCheckout(request)
            );
            assertTrue(e.getMessage().contains("Item not found"));
        }

        @Test
        @DisplayName("Should throw exception if payment fails")
        void processCheckout_InsufficientPayment_ThrowsException() {
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(itemRequest))
                    .setCashTendered(new BigDecimal("500.00"))
                    .setPaymentType("CASH")
                    .setTransactionType("POS")
                    .build();

            Exception e = assertThrows(RuntimeException.class, () ->
                    billingService.processCheckout(request)
            );
            assertTrue(e.getMessage().contains("Payment processing failed"));

            verify(transactionRepository, never()).saveTransaction(any());
        }

        @Test
        @DisplayName("Should throw exception if customer has insufficient points")
        void processCheckout_InsufficientLoyaltyPoints_ThrowsException() {
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));
            when(customerRepository.findById(1)).thenReturn(Optional.of(mockCustomer));

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(itemRequest))
                    .setCashTendered(new BigDecimal("1000.00"))
                    .setPaymentType("CASH")
                    .setCustomerId(1)
                    .setLoyaltyPointsToRedeem(100)
                    .build();

            Exception e = assertThrows(RuntimeException.class, () ->
                    billingService.processCheckout(request)
            );
            assertTrue(e.getMessage().contains("Insufficient loyalty points"));
        }

        @Test
        @DisplayName("Should throw exception if builder has no items")
        void builder_NoItems_ThrowsException() {
            CheckoutRequest.Builder builder = CheckoutRequest.builder()
                    .setCashTendered(BigDecimal.TEN)
                    .setPaymentType("CASH");

            Exception e = assertThrows(IllegalStateException.class, builder::build);
            assertEquals("Checkout must have at least one item", e.getMessage());
        }

        @Test
        @DisplayName("Should process WEB order with Online Payment correctly")
        void processCheckout_WebOrder_OnlinePayment_Success() {
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));
            when(transactionRepository.getNextSerialNumber()).thenReturn(200);

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(itemRequest))
                    .setTransactionType("WEB")
                    .setPaymentType("ONLINE")
                    .setCashTendered(new BigDecimal("1000.00"))
                    .setCashierUsername("WEB-SYSTEM")
                    .build();

            billingService.processCheckout(request);

            verify(stockRepository).reduceStock("DRY-001", 1, "WEB");
            verify(stockService, never()).checkLowStock(anyString());
        }

        @Test
        @DisplayName("Should calculate tier discount for Gold customer")
        void processCheckout_GoldCustomer_AppliesTierDiscount() {
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));
            when(transactionRepository.getNextSerialNumber()).thenReturn(200);

            Customer goldCustomer = new Customer(
                    2, "gold@mail.com", "Gold User", 0, new BigDecimal("60000"), "GOLD"
            );
            when(customerRepository.findById(2)).thenReturn(Optional.of(goldCustomer));

            LoyaltyTier goldTier = new LoyaltyTier("GOLD", new BigDecimal("50000"), 3, new BigDecimal("10"));
            when(loyaltyTierRepository.findByName("GOLD")).thenReturn(Optional.of(goldTier));

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(itemRequest))
                    .setCashTendered(new BigDecimal("1000"))
                    .setPaymentType("CASH")
                    .setTransactionType("POS")
                    .setCustomerId(2)
                    .build();

            Bill bill = billingService.processCheckout(request);

            assertNotNull(bill);
            // 1000 * 10% = 100 discount
            assertEquals(new BigDecimal("100.00"), bill.getTotalDiscount());
        }

        @Test
        @DisplayName("Should calculate tier discount plus points redemption")
        void processCheckout_GoldCustomerWithPoints_AppliesBothDiscounts() {
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));
            when(transactionRepository.getNextSerialNumber()).thenReturn(201);

            Customer goldCustomer = new Customer(
                    2, "gold@mail.com", "Gold User", 50, new BigDecimal("60000"), "GOLD"
            );
            when(customerRepository.findById(2)).thenReturn(Optional.of(goldCustomer));

            LoyaltyTier goldTier = new LoyaltyTier("GOLD", new BigDecimal("50000"), 3, new BigDecimal("10"));
            when(loyaltyTierRepository.findByName("GOLD")).thenReturn(Optional.of(goldTier));

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(Collections.singletonList(itemRequest))
                    .setCashTendered(new BigDecimal("1000"))
                    .setPaymentType("CASH")
                    .setTransactionType("POS")
                    .setCustomerId(2)
                    .setLoyaltyPointsToRedeem(20)
                    .build();

            Bill bill = billingService.processCheckout(request);

            assertNotNull(bill);
            // 1000 * 10% = 100 tier + 20 points = 120 total discount
            assertEquals(new BigDecimal("120.00"), bill.getTotalDiscount());
        }
    }
}