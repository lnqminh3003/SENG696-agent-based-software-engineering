package models;

import java.io.Serializable;

public class PaymentRequest implements Serializable {
    private TravelPlan plan;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.
    private String cardNumber;
    private String cardHolderName;
    private String cvv;
    private String expiryDate;
    private String billingAddress;
    private String customerEmail;

    public PaymentRequest(TravelPlan plan, String paymentMethod) {
        this.plan = plan;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public TravelPlan getPlan() { return plan; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String method) { this.paymentMethod = method; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = maskCardNumber(cardNumber);
    }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String name) { this.cardHolderName = name; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String address) { this.billingAddress = address; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String email) { this.customerEmail = email; }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public boolean validate() {
        if (plan == null) return false;
        if (paymentMethod == null || paymentMethod.isEmpty()) return false;
        if (cardNumber == null || cardNumber.isEmpty()) return false;
        if (cardHolderName == null || cardHolderName.isEmpty()) return false;
        if (customerEmail == null || !customerEmail.contains("@")) return false;
        return true;
    }
}