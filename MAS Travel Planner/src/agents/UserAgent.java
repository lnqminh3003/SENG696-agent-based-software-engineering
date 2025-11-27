package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Scanner;

import models.TravelPlan;
import models.PaymentConfirmation;
import models.UserRequest;
import models.PaymentRequest;


public class UserAgent extends Agent {
    private TravelPlan[] currentPlans;
    private Scanner scanner;

    @Override
    protected void setup() {
        System.out.println("UserAgent " + getLocalName() + " is ready.");
        scanner = new Scanner(System.in);

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    collectUserInput();
                } catch (Exception e) {
                    System.err.println("ERROR in UserAgent input collection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                try {
                    ACLMessage msg = receive();
                    if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                        handleIncomingMessage(msg);
                    } else if (msg != null && msg.getPerformative() == ACLMessage.FAILURE) {
                        System.err.println("ERROR: " + msg.getContent());
                        askToContinue();
                    } else {
                        block();
                    }
                } catch (Exception e) {
                    System.err.println("ERROR in UserAgent message handling: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleIncomingMessage(ACLMessage msg) throws UnreadableException {
        Object content = msg.getContentObject();

        if (content instanceof TravelPlan[]) {
            currentPlans = (TravelPlan[]) content;
            displayPlans(currentPlans);

            // Only prompt for booking if plans were found
            if (currentPlans != null && currentPlans.length > 0) {
                promptForBooking();
            } else {
                askToContinue();
            }
        } else if (content instanceof PaymentConfirmation) {
            PaymentConfirmation confirmation = (PaymentConfirmation) content;
            displayPaymentConfirmation(confirmation);
        } else if (content instanceof String) {
            String response = (String) content;
            if (response.startsWith("PAYMENT_FAILED:")) {
                System.err.println("\n✗ " + response);
                askToContinue();
            }
        }
    }

    private void displayPaymentConfirmation(PaymentConfirmation confirmation) {
        System.out.println("\n" + confirmation.toString());
        System.out.println("\n📧 Confirmation email sent to: " +
                (confirmation.getCustomerEmail() != null ?
                        confirmation.getCustomerEmail() : "your email"));
        System.out.println("\nThank you for booking with us! Have a great trip! ✈️");

        askToContinue();
    }

    private void askToContinue() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    System.out.print("\n🔄 Would you like to search for another trip? (yes/no): ");
                    String response = scanner.nextLine().trim().toLowerCase();

                    if (response.equals("yes")) {
                        System.out.println("\n" + "=".repeat(50) + "\n");
                        collectUserInput();
                    } else {
                        System.out.println("\n✈️ Thank you for using Travel Planner! Safe travels! ✈️");
                        doDelete();
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void collectUserInput() {
        try {
            System.out.println("\n=== SIMPLE TRAVEL PLANNER ===");
            System.out.print("Enter destination: ");
            String destination = scanner.nextLine().trim();

            if (destination.isEmpty()) {
                throw new IllegalArgumentException("Destination cannot be empty");
            }

            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = scanner.nextLine().trim();
            validateDate(startDate);

            System.out.print("Enter end date (YYYY-MM-DD): ");
            String endDate = scanner.nextLine().trim();
            validateDate(endDate);

            System.out.print("Enter budget ($): ");
            String budgetStr = scanner.nextLine().trim();
            double budget = Double.parseDouble(budgetStr);

            if (budget <= 0) {
                throw new IllegalArgumentException("Budget must be positive");
            }

            UserRequest request = new UserRequest(destination, startDate, endDate, budget);
            sendRequestToPlanner(request);

        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid budget format. Please enter a number.");
            collectUserInput(); // Retry
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: " + e.getMessage());
            collectUserInput(); // Retry
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error during input - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateDate(String date) throws IllegalArgumentException {
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }
    }

    private void sendRequestToPlanner(UserRequest request) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getAID("planner"));
            msg.setContentObject(request);
            send(msg);
            System.out.println("\nRequest sent to Planner Agent...\n");
        } catch (IOException e) {
            System.err.println("ERROR: Failed to send request to planner - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayPlans(TravelPlan[] plans) {
        if (plans == null || plans.length == 0) {
            System.out.println("\n⚠ No travel plans available within your budget.");
            return;
        }

        System.out.println("\n=== TOP 3 TRAVEL PLANS ===\n");
        for (TravelPlan plan : plans) {
            System.out.println(plan);
        }
    }

    private void promptForBooking() {
        if (currentPlans == null || currentPlans.length == 0) {
            return;
        }

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    System.out.print("\nWould you like to book one of these plans? (yes/no): ");
                    String response = scanner.nextLine().trim().toLowerCase();

                    if (response.equals("yes")) {
                        System.out.print("Enter plan number to book (1-" + currentPlans.length + "): ");
                        int planNumber = Integer.parseInt(scanner.nextLine().trim());

                        if (planNumber < 1 || planNumber > currentPlans.length) {
                            throw new IllegalArgumentException("Invalid plan number");
                        }

                        confirmPayment(currentPlans[planNumber - 1]);
                    } else {
                        System.out.println("No problem! You can search for another trip.");
                        askToContinue();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Please enter a valid number");
                    askToContinue();
                } catch (IllegalArgumentException e) {
                    System.err.println("ERROR: " + e.getMessage());
                    askToContinue();
                } catch (Exception e) {
                    System.err.println("ERROR: Booking failed - " + e.getMessage());
                    e.printStackTrace();
                    askToContinue();
                }
            }
        });
    }

    private void confirmPayment(TravelPlan selectedPlan) {
        try {
            System.out.println("\n=== PAYMENT CONFIRMATION ===");
            System.out.printf("Total Amount: $%.2f\n", selectedPlan.getTotalCost());

            // Collect payment details
            System.out.print("Payment Method (CREDIT_CARD/DEBIT_CARD): ");
            String paymentMethod = scanner.nextLine().trim().toUpperCase();

            // Validate payment method
            if (!paymentMethod.equals("CREDIT_CARD") &&
                    !paymentMethod.equals("DEBIT_CARD") &&
                    !paymentMethod.equals("PAYPAL")) {
                throw new IllegalArgumentException("Invalid payment method. Please choose CREDIT_CARD, DEBIT_CARD, or PAYPAL");
            }

            PaymentRequest paymentRequest = new PaymentRequest(selectedPlan, paymentMethod);

            if (paymentMethod.equals("CREDIT_CARD") || paymentMethod.equals("DEBIT_CARD")) {
                System.out.print("Card Number: ");
                String cardNumber = scanner.nextLine().trim();
                paymentRequest.setCardNumber(cardNumber);

                System.out.print("Cardholder Name: ");
                String cardHolder = scanner.nextLine().trim();
                paymentRequest.setCardHolderName(cardHolder);

                System.out.print("CVV: ");
                String cvv = scanner.nextLine().trim();
                paymentRequest.setCvv(cvv);

                System.out.print("Expiry Date (MM/YY): ");
                String expiry = scanner.nextLine().trim();
                paymentRequest.setExpiryDate(expiry);
            } else if (paymentMethod.equals("PAYPAL")) {
                System.out.println("You will be redirected to PayPal for payment...");
            }

            System.out.print("Email Address: ");
            String email = scanner.nextLine().trim();
            paymentRequest.setCustomerEmail(email);

            System.out.print("Billing Address: ");
            String address = scanner.nextLine().trim();
            paymentRequest.setBillingAddress(address);

            // Validate payment request
            if (!paymentRequest.validate()) {
                throw new IllegalArgumentException("Invalid payment details. Please check email format and required fields.");
            }

            System.out.print("\nConfirm payment? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("yes")) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getAID("payment"));
                msg.setContentObject(paymentRequest);
                send(msg);
                System.out.println("\n⏳ Processing payment...");
            } else {
                System.out.println("Payment cancelled.");
                askToContinue();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Failed to process payment - " + e.getMessage());
            e.printStackTrace();
            askToContinue();
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.out.println("\nPlease try again.");
            askToContinue();
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error during payment - " + e.getMessage());
            e.printStackTrace();
            askToContinue();
        }
    }

    @Override
    protected void takeDown() {
        if (scanner != null) {
            scanner.close();
        }
        System.out.println("UserAgent " + getLocalName() + " terminating.");
    }
}
