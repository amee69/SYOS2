package com.syos.presentation.cli;

import com.syos.domain.entity.Customer;
import com.syos.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Scanner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainMenuTest {

    @Mock AuthenticationController authController;
    @Mock BillingController billingController;
    @Mock WarehouseController warehouseController;
    @Mock ReportController reportController;
    @Mock CustomerController customerController;

    // helper to make scanner input easier to read
    private Scanner createScanner(String input) {
        return new Scanner(input);
    }

    @Test
    @DisplayName("Option 1: POS Login (Success)")
    void testPosLoginSuccess() {
        // flow select 1 (POS) -> then 0 to exit app
        Scanner scanner = createScanner("1\n0\n");

        User mockUser = new User("cashier", "CASHIER");
        when(authController.loginStaff("CASHIER")).thenReturn(mockUser);

        MainMenu menu = new MainMenu(
                authController, billingController, warehouseController,
                reportController, customerController, scanner
        );

        menu.display();

        // check if it actually tried to launch the billing screen
        verify(billingController).execute("POS", "cashier");
    }

    @Test
    @DisplayName("Option 2: Customer Login (Success)")
    void testCustomerLoginSuccess() {
        // flow: select 2 -> then 0 to exit
        Scanner scanner = createScanner("2\n0\n");

        Customer mockCustomer = mock(Customer.class);
        when(authController.loginCustomer()).thenReturn(mockCustomer);

        MainMenu menu = new MainMenu(
                authController, billingController, warehouseController,
                reportController, customerController, scanner
        );

        menu.display();

        // make sure it opened the web interface for the customer
        verify(billingController).executeWebOrder(mockCustomer);
    }

    @Test
    @DisplayName("Option 3: Admin Reports -> Enter Submenu -> Run Report -> Back -> Exit")
    void testAdminReportMenu() {
        //  need to navigate INTO the menu and back OUT
        // 3 = Reports (Main Menu)
        // 1 = Daily Sales (Sub Menu)
        // 0 = Back (Sub Menu)
        // 0 = Exit (Main Menu)
        Scanner scanner = createScanner("3\n1\n0\n0\n");

        // fake the manager login so it lets in
        User mockManager = new User("admin", "MANAGER");
        when(authController.loginStaff("MANAGER")).thenReturn(mockManager);

        MainMenu menu = new MainMenu(
                authController, billingController, warehouseController,
                reportController, customerController, scanner
        );

        menu.display();

        // verify that the report controller was actually triggered inside the loop
        verify(reportController).runDailySalesReport();
    }

    @Test
    @DisplayName("Option 4: Warehouse -> Enter Submenu -> Run Command -> Back -> Exit")
    void testWarehouseMenu() {
        //  (Warehouse) -> 3 (Add Item) -> 0 (Back) -> 0 (Exit)
        Scanner scanner = createScanner("4\n3\n0\n0\n");

        User mockManager = new User("admin", "MANAGER");
        when(authController.loginStaff("MANAGER")).thenReturn(mockManager);

        MainMenu menu = new MainMenu(
                authController, billingController, warehouseController,
                reportController, customerController, scanner
        );

        menu.display();

        // did it try to add an item?
        verify(warehouseController).addNewItem();
    }

    @Test
    @DisplayName("Option 5: Registration")
    void testCustomerRegistration() {
        // flow: 5 (Register) -> 0 (Exit)
        Scanner scanner = createScanner("5\n0\n");

        MainMenu menu = new MainMenu(
                authController, billingController, warehouseController,
                reportController, customerController, scanner
        );

        menu.display();

        verify(customerController).registerCustomer();
    }

    @Test
    @DisplayName("Access Denied: Manager fails login")
    void testManagerAccessDenied() {
        // try to go to reports (3), but login fails, so just exit (0)
        Scanner scanner = createScanner("3\n0\n");

        // force login to fail (return null)
        when(authController.loginStaff("MANAGER")).thenReturn(null);

        MainMenu menu = new MainMenu(
                authController, billingController, warehouseController,
                reportController, customerController, scanner
        );

        menu.display();

        // ensures it never got into the reports menu
        verifyNoInteractions(reportController);
    }

    @Test
    @DisplayName("Option 0: Exit immediately")
    void testExitOption() {
        Scanner scanner = createScanner("0\n");

        MainMenu menu = new MainMenu(
                authController, billingController, warehouseController,
                reportController, customerController, scanner
        );

        menu.display();

        // shouldn't have done anything
        verifyNoInteractions(billingController);
    }
}