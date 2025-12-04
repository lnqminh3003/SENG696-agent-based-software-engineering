package agents;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import javax.swing.SwingUtilities;
import jade.lang.acl.UnreadableException;
import java.io.IOException;

import gui.TravelPlannerGUI;
import models.TravelPlan;
import models.UserRequest;
import models.PaymentConfirmation;
import models.PaymentRequest;


public class GUIUserAgent extends Agent {
    private TravelPlannerGUI gui;
    private TravelPlan[] currentPlans;

    @Override
    protected void setup() {
        System.out.println("GUIUserAgent " + getLocalName() + " is ready.");

        // Launch GUI in Swing thread
        SwingUtilities.invokeLater(() -> {
            gui = new TravelPlannerGUI(this);
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    handleMessage(msg);
                } else {
                    block();
                }
            }
        });
    }

    private void handleMessage(ACLMessage msg) {
        try {
            if (msg.getPerformative() == ACLMessage.INFORM) {
                Object content = msg.getContentObject();

                if (content instanceof TravelPlan[]) {
                    currentPlans = (TravelPlan[]) content;
                    displayPlansInGUI(currentPlans);
                } else if (content instanceof PaymentConfirmation) {
                    PaymentConfirmation confirmation = (PaymentConfirmation) content;
                    displayPaymentConfirmation(confirmation);
                }
            } else if (msg.getPerformative() == ACLMessage.FAILURE) {
                displayError(msg.getContent());
            }
        } catch (UnreadableException e) {
            System.err.println("Error reading message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayPlansInGUI(TravelPlan[] plans) {
        if (gui == null) return;

        if (plans == null || plans.length == 0) {
            gui.updateResults("⚠️ No travel plans found within your budget.\n\nTry:\n" +
                    "• Increasing your budget\n" +
                    "• Different travel dates\n" +
                    "• Another destination");
            gui.updatePlanSelector(new String[0]);
            return;
        }

        StringBuilder results = new StringBuilder();
        results.append("=== TOP TRAVEL PLANS ===\n\n");

        String[] planNames = new String[plans.length];

        for (int i = 0; i < plans.length; i++) {
            TravelPlan plan = plans[i];
            results.append(plan.toString()).append("\n");

            // Create plan selector entry
            planNames[i] = String.format("Plan #%d - $%.2f", i + 1, plan.getTotalCost());
        }

        results.append("\n✅ ").append(plans.length).append(" plans found!");

        gui.updateResults(results.toString());
        gui.updatePlanSelector(planNames);
    }

    private void displayPaymentConfirmation(PaymentConfirmation confirmation) {
        if (gui == null) return;

        StringBuilder message = new StringBuilder();
        message.append("=== BOOKING CONFIRMATION ===\n\n");
        message.append("Customer Name: Minh Le\n");
        message.append("Billing Address: 1904 17 Ave NW Calgary AB Canada\n\n");
        message.append(confirmation.toString()).append("\n\n");
        message.append("📧 Confirmation email sent to: ").append(confirmation.getCustomerEmail());

        gui.updateResults(message.toString());
    }

    private void displayError(String error) {
        if (gui == null) return;

        gui.updateResults("❌ ERROR: " + error + "\n\nPlease try again with different criteria.");
    }

    public void sendSearchRequest(String destination, String startDate, String endDate, double budget) {
        try {
            UserRequest request = new UserRequest(destination, startDate, endDate, budget);

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getAID("planner"));
            msg.setContentObject(request);
            send(msg);

            System.out.println("Search request sent: " + destination);
        } catch (IOException e) {
            System.err.println("Error sending search request: " + e.getMessage());
            e.printStackTrace();
            displayError("Failed to send search request");
        }
    }

    public void sendBookingRequest(int planIndex, String paymentMethod, String cardHolderName,
                                   String billingAddress, String email) {
        if (currentPlans == null || planIndex < 0 || planIndex >= currentPlans.length) {
            displayError("Invalid plan selection");
            return;
        }

        TravelPlan plan = currentPlans[planIndex];

        PaymentRequest paymentRequest = new PaymentRequest(
                plan, paymentMethod
        );

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(getAID("payment"));
                    msg.setContentObject(paymentRequest);
                    send(msg);
                    System.out.println("Booking request sent for plan #" + (planIndex + 1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    @Override
    protected void takeDown() {
        if (gui != null) {
            gui.dispose();
        }
        System.out.println("GUIUserAgent " + getLocalName() + " terminating.");
    }
}