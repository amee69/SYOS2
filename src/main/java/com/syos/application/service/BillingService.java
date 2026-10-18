package com.syos.application.service;

import com.syos.application.dto.BillItemRequest;
import com.syos.application.dto.CheckoutRequest;
import com.syos.domain.entity.*;
import com.syos.domain.repository.CustomerRepository;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.TransactionRepository;
import com.syos.domain.service.PaymentStrategy;
import com.syos.infrastructure.payment.CashPayment;
import com.syos.infrastructure.payment.OnlinePayment;
import com.syos.domain.entity.BillItem;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Item;
import com.syos.domain.service.DiscountCalculator;
import com.syos.domain.discount.NoDiscount;
import com.syos.domain.discount.LoyaltyTierDiscount;
import com.syos.domain.discount.LoyaltyPointsRedemption;
import com.syos.domain.entity.LoyaltyTier;
import com.syos.domain.repository.LoyaltyTierRepository;
import com.syos.infrastructure.persistence.LoggingStockRepositoryProxy;
import java.util.concurrent.locks.ReentrantLock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BillingService {
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final StockRepository stockRepository;
    private final ItemRepository itemRepository; //imports interface-dip
    private final StockService stockService;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final BillFileWriter billFileWriter;

    private final ReentrantLock stockLock = new ReentrantLock();


    public BillingService(TransactionRepository transactionRepository,
                          CustomerRepository customerRepository,
                          StockRepository stockRepository,
                          ItemRepository itemRepository, //expects of type ItemRepository interface
                          StockService stockService,
                          LoyaltyTierRepository loyaltyTierRepository,
                          BillFileWriter billFileWriter) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.stockRepository = stockRepository;
        this.itemRepository = itemRepository;
        this.stockService = stockService;
        this.loyaltyTierRepository = loyaltyTierRepository;
        this.billFileWriter = billFileWriter;
    }




    public Bill processCheckout(CheckoutRequest request) {

        List<BillItem> items = new ArrayList<>();
        for (BillItemRequest req : request.getItems()) {
            Item item = itemRepository.findByCode(req.getItemCode())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + req.getItemCode()));

            items.add(new BillItem( //map to billItem entity from dto
                    item.getItemCode(),
                    item.getName(),
                    req.getQuantity(),
                    item.getUnitPrice()
            ));
        }

        // fetch customer (optional)
        Customer customer = null;
        if (request.hasCustomer()) {
            customer = customerRepository.findById(request.getCustomerId()).orElse(null);
        }



//        PaymentStrategy payment = request.getPaymentStrategy(); //since builder, can use getter

        BigDecimal totalDiscount = BigDecimal.ZERO;
        String discountDescription = "";

        if (customer != null) {
            DiscountCalculator discountCalc = new NoDiscount();

            LoyaltyTier tier = loyaltyTierRepository.findByName(customer.getCustomerTier())
                    .orElse(null);

            if (tier != null) {
                BigDecimal tierPercent = customer.getTierDiscountPercent(tier);
                if (tierPercent.compareTo(BigDecimal.ZERO) > 0) {
                    discountCalc = new LoyaltyTierDiscount(discountCalc, customer.getCustomerTier(), tierPercent);
                }
            }

            if (request.getLoyaltyPointsToRedeem() > 0) {
                discountCalc = new LoyaltyPointsRedemption(discountCalc, request.getLoyaltyPointsToRedeem());
            }

            totalDiscount = discountCalc.calculateDiscount(calculateTotal(items));
            discountDescription = discountCalc.getDiscountDescription();
        }

        // build payment strategy after discount is calculated
        BigDecimal subtotal = calculateTotal(items);
        BigDecimal netForPayment = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO);
        PaymentStrategy payment;

        if ("WEB".equalsIgnoreCase(request.getTransactionType())) {
            payment = new OnlinePayment(netForPayment);
        } else {
            payment = new CashPayment(request.getCashTendered());
            payment.process(netForPayment);
        }

        return processSaleInternal(
                items,
                payment,
                totalDiscount,
                discountDescription,
                request.getTransactionType(),
                request.getCashierUsername(),
                customer,
                request.getLoyaltyPointsToRedeem()
        );
    }



      //internal private logic down handles the transaction stock reduction and alerts

    private Bill processSaleInternal(
            List<BillItem> items,
            PaymentStrategy chosenPaymentMethod,
            BigDecimal discount,
            String discountDescription,
            String transactionType,
            String cashierUsername,
            Customer customer,
            int pointsToRedeem
    ) {
        // calculate logic
        BigDecimal total = calculateTotal(items);
        BigDecimal netAmount = total.subtract(discount).max(BigDecimal.ZERO);

        //  loyalty redemption
        int pointsUsed = 0;
        if (customer != null && pointsToRedeem > 0) {
            if (customer.canRedeemPoints(pointsToRedeem)) {
                pointsUsed = pointsToRedeem;
                customer.redeemLoyaltyPoints(pointsToRedeem);
            } else {
                throw new RuntimeException("Insufficient loyalty points");
            }
        }

        // loyalty earning part
        int pointsEarned = 0;
        if (customer != null) {
            LoyaltyTier currentTier = loyaltyTierRepository.findByName(customer.getCustomerTier())
                    .orElse(null);

            pointsEarned = customer.calculatePointsEarned(netAmount, currentTier);
            customer.addLoyaltyPoints(pointsEarned);
            customer.addPurchase(netAmount);

            loyaltyTierRepository.findTierByPurchaseAmount(customer.getTotalPurchases())
                    .ifPresent(newTier -> customer.updateTier(newTier.getTierName()));
        }

        //  payment processing
        if (!chosenPaymentMethod.process(netAmount)) {
            throw new RuntimeException("Payment processing failed.");
        }

        BigDecimal cashTendered = chosenPaymentMethod.getCashTendered();
        BigDecimal changeAmount = chosenPaymentMethod.getChangeDue();

        // inside lock = shared stuff = critical sectionn
        stockLock.lock();
        try {
            // serial number inside lock to prevent duplicates
            int serial = transactionRepository.getNextSerialNumber();

            Bill.Builder billBuilder = new Bill.Builder()
                    .setSerial(serial)
                    .setItems(items)
                    .setTotal(total)
                    .setDiscount(discount)
                    .setPaymentType(chosenPaymentMethod.getPaymentType())
                    .setTransactionType(transactionType)
                    .setCashierUsername(cashierUsername)
                    .setDiscountDescription(discountDescription)
                    .setLoyaltyPointsUsed(pointsUsed)
                    .setLoyaltyPointsEarned(pointsEarned)
                    .setCashTendered(cashTendered)
                    .setChangeAmount(changeAmount);

            if (customer != null) {
                billBuilder.setCustomerId(customer.getCustomerId());
            }

            Bill newBill = billBuilder.build();
            transactionRepository.saveTransaction(newBill);

            // reduce inventory
            String stockType = transactionType.equalsIgnoreCase("POS") ? "SHELF" : "WEB";
            stockRepository.setCurrentUser(cashierUsername);
            stockRepository.setCurrentBillSerial(serial);

            for (BillItem item : items) {
                stockRepository.reduceStock(item.getItemCode(), item.getQuantity(), stockType);

                if (stockType.equalsIgnoreCase("SHELF")) {
                    System.out.println("() Reduced " + item.getQuantity() + " units of " +
                            item.getItemName() + " from " + stockType);
                }

                if (stockType.equalsIgnoreCase("SHELF")) {
                    stockService.checkLowStock(item.getItemCode());
                }
            }

            stockRepository.clearContext();

            // update customer stats
            if (customer != null) {
                customerRepository.updateLoyaltyPoints(customer.getCustomerId(), customer.getLoyaltyPoints());
                customerRepository.updateTotalPurchases(customer.getCustomerId(), customer.getTotalPurchases());
            }

            billFileWriter.save(newBill);

            return newBill;

        } finally {
            stockLock.unlock(); //so deadlock doent happen
        }
    }


    //private helpers....
    private BigDecimal calculateTotal(List<BillItem> items) {
        return items.stream()
                .map(BillItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    //moved billFileWriter fpr srp reasons

}