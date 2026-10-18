package com.syos.presentation.cli;

import com.syos.application.service.CustomerService;
import com.syos.domain.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Controller UI Tests")
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private CustomerController controller;

    // helper to create the controller with "Fake" user input
    private void setupControllerWithInput(String userInput) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        Scanner mockScanner = new Scanner(inputStream);
        controller = new CustomerController(customerService, mockScanner);
    }

    @Test
    @DisplayName("Should successfully register a customer when all inputs are valid")
    void registerCustomer_HappyPath_Success() {
        // Simulate user input:
        // Name: John Doe
        //  Email: john@test.com
        //  Phone: 0771234567
        //Pass: 1234
        //  Confirm: 1234
        // Confirm Registration: y
        String input = "John Doe\njohn@test.com\n0771234567\n1234\n1234\ny\n";

        setupControllerWithInput(input);

        // Mock service to return a customer on successful registration
        Customer mockCustomer = new Customer(1, "john@test.com", "John Doe", "0771234567", "1234", 0, BigDecimal.ZERO, "BRONZE");
        when(customerService.register(any())).thenReturn(mockCustomer);

        // Act
        controller.registerCustomer();

        // Assert - verify service was called
        verify(customerService, times(1)).register(any());
    }

    @Test
    @DisplayName("Should stop registration if email is already taken")
    void registerCustomer_EmailTaken_Stops() {
        // User types Name, then an Email that exists
        String input = "John Doe\nexisting@test.com\n0771234567\n1234\n1234\ny\n";

        setupControllerWithInput(input);

        // Mock service to throw exception for duplicate email
        when(customerService.register(any())).thenThrow(new IllegalArgumentException("Email already registered"));

        // Act
        controller.registerCustomer();

        // Assert - service was called but threw exception
        verify(customerService, times(1)).register(any());
    }

    @Test
    @DisplayName("Should stop registration if passwords do not match")
    void registerCustomer_PasswordMismatch_Stops() {
        //  Name,  Email,  Phone,  Pass(1234), Confirm(9999) -> Mismatch
        String input = "John Doe\njohn@test.com\n0771234567\n1234\n9999\n";

        setupControllerWithInput(input);

        // Act
        controller.registerCustomer();

        // Assert - service should never be called because validation fails in controller
        verify(customerService, never()).register(any());
    }

    @Test
    @DisplayName("Should abort if user types 'n' at final confirmation")
    void registerCustomer_UserCancels_Stops() {
        // Valid inputs but 'n' at the end
        String input = "John Doe\njohn@test.com\n0771234567\n1234\n1234\nn\n";

        setupControllerWithInput(input);

        // Act
        controller.registerCustomer();

        // Assert - service should never be called
        verify(customerService, never()).register(any());
    }
}