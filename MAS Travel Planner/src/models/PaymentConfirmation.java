package models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentConfirmation implements Serializable {
    private String paymentId;
    private String bookingReference;
    private String confirmationCode;
    private double amount;
    private String paymentMethod;
    private String status; // PENDING, SUCCESS, FAILED, REFUNDED
    private String timestamp;
    private TravelPlan bookedPlan;
    private String transactionId;
    private String customerEmail;

    public PaymentConfirmation(String paymentId, String bookingReference,
                               String confirmationCode, double amount,
                               TravelPlan bookedPlan) {
        this.paymentId = paymentId;
        this.bookingReference = bookingReference;
        this.confirmationCode = confirmationCode;
        this.amount = amount;
        this.bookedPlan = bookedPlan;
        this.status = "SUCCESS";
        this.paymentMethod = "CREDIT_CARD"; // Default
        this.timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.transactionId = generateTransactionId();
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis();
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public String getBookingReference() { return bookingReference; }
    public String getConfirmationCode() { return confirmationCode; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
    public TravelPlan getBookedPlan() { return bookedPlan; }
    public String getTransactionId() { return transactionId; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getCustomerEmail() { return customerEmail; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setPaymentMethod(String method) { this.paymentMethod = method; }
    public void setCustomerEmail(String email) { this.customerEmail = email; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔═══════════════════════════════════════════════╗\n");
        sb.append("║        PAYMENT CONFIRMATION RECEIPT          ║\n");
        sb.append("╠═══════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Payment ID:      %-28s║\n", paymentId));
        sb.append(String.format("║ Booking Ref:     %-28s║\n", bookingReference));
        sb.append(String.format("║ Confirmation:    %-28s║\n", confirmationCode));
        sb.append(String.format("║ Transaction ID:  %-28s║\n", transactionId));
        sb.append(String.format("║ Status:          %-28s║\n", status));
        sb.append(String.format("║ Amount:          $%-27.2f║\n", amount));
        sb.append(String.format("║ Payment Method:  %-28s║\n", paymentMethod));
        sb.append(String.format("║ Timestamp:       %-28s║\n", timestamp));
        sb.append("╠═══════════════════════════════════════════════╣\n");
        sb.append("║ TRAVEL DETAILS                               ║\n");

        if (bookedPlan != null) {
            String planStr = bookedPlan.toString();
            String[] lines = planStr.split("\n");
            for (int i = 1; i < lines.length && i < 3; i++) {
                String line = lines[i].trim();
                if (line.length() > 45) {
                    line = line.substring(0, 45);
                }
                sb.append(String.format("║ %-45s║\n", line));
            }
        }

        sb.append("╚═══════════════════════════════════════════════╝");
        return sb.toString();
    }

    public String toEmailFormat() {
        return String.format(
                "Dear Valued Customer,\n\n" +
                        "Your booking has been confirmed!\n\n" +
                        "Booking Reference: %s\n" +
                        "Confirmation Code: %s\n" +
                        "Payment ID: %s\n" +
                        "Amount Paid: $%.2f\n\n" +
                        "Travel Details:\n%s\n" +
                        "Thank you for choosing our service!\n\n" +
                        "Travel Safe,\nTravel Planner Team",
                bookingReference, confirmationCode, paymentId, amount,
                bookedPlan != null ? bookedPlan.toString() : "N/A"
        );
    }
}
