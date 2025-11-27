package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Random;

import models.TravelPlan;
import models.PaymentRequest;
import models.PaymentConfirmation;

public class PaymentAgent extends Agent {
    private Random random = new Random();

    @Override
    protected void setup() {
        System.out.println("PaymentAgent " + getLocalName() + " is ready.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                try {
                    ACLMessage msg = receive();
                    if (msg != null) {
                        handleMessage(msg);
                    } else {
                        block();
                    }
                } catch (Exception e) {
                    System.err.println("ERROR in PaymentAgent: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleMessage(ACLMessage msg) {
        try {
            Object content = msg.getContentObject();

            if (msg.getPerformative() == ACLMessage.REQUEST && content instanceof PaymentRequest) {
                // User wants to confirm payment with full details
                PaymentRequest paymentRequest = (PaymentRequest) content;
                processPaymentRequest(paymentRequest, msg.getSender());

            } else if (msg.getPerformative() == ACLMessage.INFORM && content instanceof TravelPlan[]) {
                // Just logging available plans (original behavior)
                logAvailablePlans((TravelPlan[]) content);
            }
        } catch (UnreadableException e) {
            System.err.println("ERROR: Unable to read payment message - " + e.getMessage());
            sendPaymentError(msg.getSender(), "Payment processing error");
        } catch (Exception e) {
            System.err.println("ERROR processing payment: " + e.getMessage());
            e.printStackTrace();
            sendPaymentError(msg.getSender(), "Unexpected payment error");
        }
    }

    private void logAvailablePlans(TravelPlan[] plans) {
        System.out.println("\n=== PAYMENT AGENT: Plans Ready for Booking ===");
        for (TravelPlan plan : plans) {
            String bookingRef = generateBookingReference();
            System.out.printf("Plan - Booking Ref: %s (Amount: $%.2f)\n",
                    bookingRef, plan.getTotalCost());
        }
        System.out.println("===============================================\n");
    }

    private void processPaymentConfirmation(TravelPlan plan, jade.core.AID userAID) {
        System.out.println("\n=== PROCESSING PAYMENT ===");
        System.out.printf("Amount: $%.2f\n", plan.getTotalCost());

        try {
            // Validate payment amount
            if (plan.getTotalCost() <= 0) {
                throw new IllegalArgumentException("Invalid payment amount");
            }

            // Simulate payment processing
            System.out.println("Contacting payment gateway...");
            Thread.sleep(2000);

            // Simulate 95% success rate
            boolean paymentSuccess = random.nextDouble() < 0.95;

            if (paymentSuccess) {
                String confirmationCode = generateConfirmationCode();
                String bookingRef = generateBookingReference();

                System.out.println("✓ Payment successful!");
                System.out.println("Confirmation Code: " + confirmationCode);
                System.out.println("Booking Reference: " + bookingRef);

                sendPaymentSuccess(userAID, confirmationCode, bookingRef, plan);
            } else {
                throw new Exception("Payment gateway declined transaction");
            }

        } catch (InterruptedException e) {
            System.err.println("ERROR: Payment processing interrupted");
            sendPaymentError(userAID, "Payment processing interrupted");
        } catch (Exception e) {
            System.err.println("ERROR: Payment failed - " + e.getMessage());
            sendPaymentError(userAID, "Payment declined: " + e.getMessage());
        }

        System.out.println("===========================\n");
    }

    private void processPaymentRequest(PaymentRequest paymentRequest, jade.core.AID userAID) {
        System.out.println("\n=== PROCESSING PAYMENT REQUEST ===");
        TravelPlan plan = paymentRequest.getPlan();
        System.out.printf("Amount: $%.2f\n", plan.getTotalCost());
        System.out.println("Payment Method: " + paymentRequest.getPaymentMethod());
        System.out.println("Customer: " + paymentRequest.getCardHolderName());

        try {
            // Validate payment request
            if (!paymentRequest.validate()) {
                throw new IllegalArgumentException("Invalid payment details provided");
            }

            // Validate payment amount
            if (plan.getTotalCost() <= 0) {
                throw new IllegalArgumentException("Invalid payment amount");
            }

            // Generate payment ID
            String paymentId = generatePaymentId();

            // Simulate payment gateway processing
            System.out.println("Contacting payment gateway...");
            System.out.println("Validating card details...");
            Thread.sleep(1500);

            System.out.println("Processing transaction...");
            Thread.sleep(1500);

            // Simulate 95% success rate
            boolean paymentSuccess = random.nextDouble() < 0.95;

            if (paymentSuccess) {
                String confirmationCode = generateConfirmationCode();
                String bookingRef = generateBookingReference();

                // Create payment confirmation object
                PaymentConfirmation confirmation = new PaymentConfirmation(
                        paymentId, bookingRef, confirmationCode,
                        plan.getTotalCost(), plan
                );
                confirmation.setPaymentMethod(paymentRequest.getPaymentMethod());
                confirmation.setCustomerEmail(paymentRequest.getCustomerEmail());

                System.out.println("✓ Payment successful!");
                System.out.println("Payment ID: " + paymentId);
                System.out.println("Confirmation Code: " + confirmationCode);
                System.out.println("Booking Reference: " + bookingRef);

                // Send confirmation to user
                sendPaymentConfirmation(userAID, confirmation);

                // Log for records
                logPaymentRecord(confirmation);

            } else {
                throw new Exception("Payment gateway declined transaction - insufficient funds");
            }

        } catch (InterruptedException e) {
            System.err.println("ERROR: Payment processing interrupted");
            sendPaymentError(userAID, "Payment processing interrupted");
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: " + e.getMessage());
            sendPaymentError(userAID, e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR: Payment failed - " + e.getMessage());
            sendPaymentError(userAID, "Payment declined: " + e.getMessage());
        }

        System.out.println("===================================\n");
    }

    private void sendPaymentConfirmation(jade.core.AID userAID, PaymentConfirmation confirmation) {
        try {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(userAID);
            reply.setContentObject(confirmation);
            send(reply);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to send payment confirmation - " + e.getMessage());
        }
    }

    private void logPaymentRecord(PaymentConfirmation confirmation) {
        System.out.println("\n--- Payment Record Logged ---");
        System.out.println("Payment ID: " + confirmation.getPaymentId());
        System.out.println("Transaction ID: " + confirmation.getTransactionId());
        System.out.println("Timestamp: " + confirmation.getTimestamp());
        System.out.println("Status: " + confirmation.getStatus());
        System.out.println("----------------------------\n");
    }

    private void sendPaymentSuccess(jade.core.AID userAID, String confirmationCode,
                                    String bookingRef, TravelPlan plan) {
        try {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(userAID);
            String message = String.format(
                    "PAYMENT_SUCCESS: Booking confirmed!\nConfirmation: %s\nBooking Ref: %s\nAmount: $%.2f",
                    confirmationCode, bookingRef, plan.getTotalCost()
            );
            reply.setContentObject(message);
            send(reply);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to send payment confirmation - " + e.getMessage());
        }
    }

    private String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis() + "-" + (1000 + random.nextInt(9000));
    }

    private void sendPaymentError(jade.core.AID userAID, String errorMessage) {
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(userAID);
        try {
            reply.setContentObject("PAYMENT_FAILED: " + errorMessage);
            send(reply);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to send payment error - " + e.getMessage());
        }
    }

    private String generateBookingReference() {
        return "BK" + (10000 + random.nextInt(90000));
    }

    private String generateConfirmationCode() {
        return "CONF-" + (1000 + random.nextInt(9000));
    }
}