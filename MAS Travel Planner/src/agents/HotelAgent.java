package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.HotelOption;
import models.UserRequest;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HotelAgent extends Agent {

    private Map<String, List<HotelOption>> hotelDatabase;

    @Override
    protected void setup() {
        System.out.println("HotelAgent " + getLocalName() + " is ready.");
        initializeDatabase();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    try {
                        UserRequest request = (UserRequest) msg.getContentObject();
                        provideHotelOptions(request, msg.getSender());
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else {
                    block();
                }
            }
        });
    }

    /**
     * Load hotel data from JSON file instead of hardcoding.
     */
    private void initializeDatabase() {
        ObjectMapper mapper = new ObjectMapper();

        // Path relative to project root
        File jsonFile = new File("src/database/hotel_data.json");

        try {
            hotelDatabase = mapper.readValue(
                    jsonFile,
                    new TypeReference<Map<String, List<HotelOption>>>() {}
            );
//            System.out.println("HotelAgent: Loaded hotel database successfully.");
        } catch (Exception e) {
            System.err.println("HotelAgent: ERROR loading JSON at: " + jsonFile.getAbsolutePath());
            e.printStackTrace();
            hotelDatabase = new HashMap<>();
        }
    }

    /**
     * Select and send hotel options back to requester
     */
    private void provideHotelOptions(UserRequest request, jade.core.AID sender) {
        String destination = request.getDestination();

        List<HotelOption> options = hotelDatabase.getOrDefault(
                destination,
                hotelDatabase.getOrDefault("Default", new ArrayList<>())
        );

        // Budget rule: assume 60% of budget should go to hotel (3 nights)
        double estimatedMaxHotelBudget = request.getBudget() * 0.6;

        List<HotelOption> affordableOptions = new ArrayList<>();
        for (HotelOption hotel : options) {
            if (hotel.getCostPerNight() * 3 <= estimatedMaxHotelBudget) {
                affordableOptions.add(hotel);
            }
        }

        // If none are affordable, return all
        if (affordableOptions.isEmpty()) {
            affordableOptions = options;
        }

        HotelOption[] optionsArray = affordableOptions.toArray(new HotelOption[0]);

        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(sender);

        try {
            reply.setContentObject(optionsArray);
            send(reply);
            System.out.println("HotelAgent: Sent " + affordableOptions.size() +
                    " hotel options for " + destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
