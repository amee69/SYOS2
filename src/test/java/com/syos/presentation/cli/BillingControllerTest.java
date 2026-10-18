package com.syos.presentation.cli;

import com.syos.application.dto.CheckoutRequest;
import com.syos.application.factory.ValidationServiceFactory;
import com.syos.application.service.BillingService;
import com.syos.application.service.ValidationService;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.CustomerRepository;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.LoyaltyTierRepository;
import com.syos.domain.service.PaymentStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    @Mock private BillingService billingService;
    @Mock private ItemRepository itemRepo;
    @Mock private CustomerRepository customerRepo;
    @Mock private ValidationServiceFactory validationFactory;
    @Mock private LoyaltyTierRepository tierRepo;
    @Mock private ValidationService validationService;
    @Mock private PaymentFactory paymentFactory;  // ADD
    @Mock private PaymentStrategy paymentStrategy;  // ADD

    @Test
    @DisplayName("POS Flow: Scan Item -> Pay Cash -> Success")
    void execute_POS_HappyPath() {
        String userInput = "D01\n2\ndone\n\nexit\n";  // Removed cash input - factory handles it
        Scanner mockScanner = new Scanner(userInput);

        // validation mocks
        when(validationFactory.create(anyString())).thenReturn(validationService);
        when(validationService.validateItem(any())).thenReturn(null);

        // Item mock
        Item mockItem = new Item("D01", "Milk", new BigDecimal("400.00"), "DAIRY", 10);
        when(itemRepo.findByCode("D01")).thenReturn(Optional.of(mockItem));

        // Payment factory mock
        when(paymentFactory.create(anyString(), any(BigDecimal.class), any(Scanner.class)))
                .thenReturn(paymentStrategy);

        // Bill mock
        Bill mockBill = new Bill.Builder()
                .setSerial(123)
                .setBillDate(LocalDateTime.now())
                .setTransactionType("POS")
                .setCashierUsername("cashier1")
                .setTotal(new BigDecimal("800.00"))
                .setDiscount(BigDecimal.ZERO)
                .setItems(List.of(new BillItem("D01", "Milk", 2, new BigDecimal("400.00"))))
                .build();

        when(billingService.processCheckout(any(CheckoutRequest.class))).thenReturn(mockBill);

        BillingController controller = new BillingController(
                billingService, itemRepo, mockScanner, customerRepo,
                validationFactory, tierRepo, paymentFactory  // ADD paymentFactory
        );

        controller.execute("POS", "cashier1");

        ArgumentCaptor<CheckoutRequest> captor = ArgumentCaptor.forClass(CheckoutRequest.class);
        verify(billingService).processCheckout(captor.capture());

        CheckoutRequest request = captor.getValue();
        assertEquals("cashier1", request.getCashierUsername());
        assertEquals(1, request.getItems().size());
        assertEquals("D01", request.getItems().get(0).getItemCode());
        assertEquals(2, request.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Validation Error: Should handle invalid quantity gracefully")
    void execute_InvalidQuantity() {
        String userInput = "D01\nabc\ndone\nexit\n";
        Scanner mockScanner = new Scanner(userInput);

        BillingController controller = new BillingController(
                billingService, itemRepo, mockScanner, customerRepo,
                validationFactory, tierRepo, paymentFactory  // ADD paymentFactory
        );

        controller.execute("POS", "cashier1");

        verify(billingService, never()).processCheckout(any());
    }
}