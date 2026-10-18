package com.syos.application.service;

import com.syos.application.dto.CustomerRegistrationRequest;
import com.syos.domain.entity.Customer;
import com.syos.domain.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Nested
    @DisplayName("Registration Logic")
    class RegistrationTests {

        @Test
        @DisplayName("Should successfully register a new customer with default values")
        void register_Success() {
            // Arrange
            // Order must be (FullName, Email, Phone, Password) to match  DTO
            CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                    "Alice Wonderland",
                    "alice@example.com",
                    "0771234567",
                    "secret123"
            );

            when(customerRepository.existsByEmail("alice@example.com")).thenReturn(false);

            // Act
            Customer result = customerService.register(request);

            // Assert
            assertAll("Customer Registration Defaults",
                    () -> assertEquals("alice@example.com", result.getEmail()),
                    () -> assertEquals("Alice Wonderland", result.getFullName()),
                    () -> assertEquals("0771234567", result.getPhone()),
                    () -> assertEquals("BRONZE", result.getCustomerTier()),
                    () -> assertEquals(0, result.getLoyaltyPoints()),
                    () -> assertEquals(0, result.getTotalPurchases().compareTo(BigDecimal.ZERO))
            );

            verify(customerRepository, times(1)).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is already in use")
        void register_DuplicateEmail_ThrowsException() {
            // Arrange
            String existingEmail = "taken@example.com";
            //  Name first, then Email
            CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                    "Bob",
                    existingEmail,
                    "0112223334",
                    "password"
            );

            // Now Mockito sees the correct email being passed to the repository check
            when(customerRepository.existsByEmail(existingEmail)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    customerService.register(request)
            );

            assertEquals("Email already registered", exception.getMessage());
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }
}