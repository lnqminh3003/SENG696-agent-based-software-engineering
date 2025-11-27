package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import models.HotelOption;

import java.io.IOException;
import java.util.*;

import models.UserRequest;
import models.TransportOption;
import models.TravelPlan;

public class PlannerAgent extends Agent {
    private UserRequest currentRequest;
    private List<TransportOption> transportOptions;
    private List<HotelOption> hotelOptions;
    private boolean transportReceived = false;
    private boolean hotelReceived = false;
    private jade.core.AID userAID;
    private static final int TIMEOUT_MS = 10000; // 10 second timeout
    private boolean timeoutHandled = false;

    @Override
    protected void setup() {
        System.out.println("PlannerAgent " + getLocalName() + " is ready.");

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
                    System.err.println("ERROR in PlannerAgent: " + e.getMessage());
                    e.printStackTrace();
                    sendErrorToUser("Planning error: " + e.getMessage());
                }
            }
        });
    }

    private void handleMessage(ACLMessage msg) {
        try {
            if (msg.getPerformative() == ACLMessage.REQUEST) {
                Object content = msg.getContentObject();
                if (content instanceof UserRequest) {
                    userAID = msg.getSender();
                    currentRequest = (UserRequest) content;
                    System.out.println("PlannerAgent: Received request for " + currentRequest.getDestination());

                    // Reset flags
                    transportReceived = false;
                    hotelReceived = false;
                    transportOptions = null;
                    hotelOptions = null;
                    timeoutHandled = false;

                    requestTransportOptions();
                    requestHotelOptions();

                    // Set timeout for responses
                    addBehaviour(new WakerBehaviour(this, TIMEOUT_MS) {
                        @Override
                        protected void onWake() {
                            if (!timeoutHandled && (!transportReceived || !hotelReceived)) {
                                handleTimeout();
                            }
                        }
                    });
                }
            } else if (msg.getPerformative() == ACLMessage.INFORM) {
                Object content = msg.getContentObject();
                if (content instanceof TransportOption[]) {
                    transportOptions = Arrays.asList((TransportOption[]) content);
                    transportReceived = true;
                    System.out.println("PlannerAgent: Received " + transportOptions.size() + " transport options");
                } else if (content instanceof HotelOption[]) {
                    hotelOptions = Arrays.asList((HotelOption[]) content);
                    hotelReceived = true;
                    System.out.println("PlannerAgent: Received " + hotelOptions.size() + " hotel options");
                }

                if (transportReceived && hotelReceived) {
                    timeoutHandled = true; // Mark as handled to prevent timeout message
                    generatePlans();
                }
            }
        } catch (UnreadableException e) {
            System.err.println("ERROR: Unable to read message content - " + e.getMessage());
            sendErrorToUser("Failed to process request");
        } catch (Exception e) {
            System.err.println("ERROR in PlannerAgent message handling: " + e.getMessage());
            e.printStackTrace();
            sendErrorToUser("Planning error occurred");
        }
    }

    private void handleTimeout() {
        timeoutHandled = true;
        String error = "Timeout: ";
        if (!transportReceived) error += "Transport data unavailable. ";
        if (!hotelReceived) error += "Hotel data unavailable.";

        System.err.println("ERROR: " + error);
        sendErrorToUser(error);
    }

    private void requestTransportOptions() {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getAID("transport"));
            msg.setContentObject(currentRequest);
            send(msg);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to request transport options - " + e.getMessage());
            sendErrorToUser("Failed to fetch transport options");
        }
    }

    private void requestHotelOptions() {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getAID("hotel"));
            msg.setContentObject(currentRequest);
            send(msg);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to request hotel options - " + e.getMessage());
            sendErrorToUser("Failed to fetch hotel options");
        }
    }

    private void generatePlans() {
        try {
            if (transportOptions == null || transportOptions.isEmpty()) {
                throw new IllegalStateException("No transport options available");
            }
            if (hotelOptions == null || hotelOptions.isEmpty()) {
                throw new IllegalStateException("No hotel options available");
            }

            System.out.println("PlannerAgent: Generating travel plans...");
            List<TravelPlan> allPlans = new ArrayList<>();

            for (TransportOption transport : transportOptions) {
                for (HotelOption hotel : hotelOptions) {
                    int nights = calculateNights(currentRequest.getStartDate(), currentRequest.getEndDate());
                    double hotelTotalCost = hotel.getCostPerNight() * nights;
                    double totalCost = transport.getCost() + hotelTotalCost;

                    if (totalCost <= currentRequest.getBudget()) {
                        TravelPlan plan = new TravelPlan(
                                currentRequest.getDestination(),
                                currentRequest.getStartDate(),
                                currentRequest.getEndDate(),
                                transport.getType(),
                                transport.getCost(),
                                hotel.getName(),
                                hotelTotalCost
                        );
                        allPlans.add(plan);
                    }
                }
            }

            if (allPlans.isEmpty()) {
                sendErrorToUser("No plans available within budget of $" + currentRequest.getBudget());
                return;
            }

            allPlans.sort(Comparator.comparingDouble(TravelPlan::getTotalCost));

            int planCount = Math.min(3, allPlans.size());
            TravelPlan[] topPlans = new TravelPlan[planCount];
            for (int i = 0; i < planCount; i++) {
                topPlans[i] = allPlans.get(i);
                topPlans[i].setRank(i + 1);
            }

            sendPlansToUser(topPlans);

        } catch (Exception e) {
            System.err.println("ERROR generating plans: " + e.getMessage());
            e.printStackTrace();
            sendErrorToUser("Failed to generate travel plans");
        } finally {
            transportReceived = false;
            hotelReceived = false;
        }
    }

    private int calculateNights(String start, String end) {
        // Simplified calculation - in production, use proper date parsing
        return 3;
    }

    private void sendPlansToUser(TravelPlan[] plans) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(userAID);
            msg.setContentObject(plans);
            send(msg);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to send plans to user - " + e.getMessage());
        }
    }

    private void sendErrorToUser(String errorMessage) {
        if (userAID != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);
            msg.addReceiver(userAID);
            msg.setContent(errorMessage);
            send(msg);
        }
    }
}
