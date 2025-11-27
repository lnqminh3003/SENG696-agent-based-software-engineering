package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import models.HotelOption;

import java.io.IOException;
import java.util.*;

import models.UserRequest;

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

    private void initializeDatabase() {
        hotelDatabase = new HashMap<>();

        // Paris hotels
        List<HotelOption> parisHotels = new ArrayList<>();
        parisHotels.add(new HotelOption("Hotel Luxe Paris", 180.0, "Paris"));
        parisHotels.add(new HotelOption("Budget Inn Paris", 80.0, "Paris"));
        parisHotels.add(new HotelOption("Mid-Range Paris Hotel", 120.0, "Paris"));
        hotelDatabase.put("Paris", parisHotels);

        // London hotels
        List<HotelOption> londonHotels = new ArrayList<>();
        londonHotels.add(new HotelOption("London Grand Hotel", 200.0, "London"));
        londonHotels.add(new HotelOption("Cozy London Stay", 90.0, "London"));
        londonHotels.add(new HotelOption("Thames View Hotel", 140.0, "London"));
        hotelDatabase.put("London", londonHotels);

        // New York hotels
        List<HotelOption> nyHotels = new ArrayList<>();
        nyHotels.add(new HotelOption("Manhattan Luxury Suites", 280.0, "New York"));
        nyHotels.add(new HotelOption("Brooklyn Budget Hotel", 110.0, "New York"));
        nyHotels.add(new HotelOption("Times Square Hotel", 190.0, "New York"));
        hotelDatabase.put("New York", nyHotels);

        // Tokyo hotels
        List<HotelOption> tokyoHotels = new ArrayList<>();
        tokyoHotels.add(new HotelOption("Tokyo Imperial Hotel", 250.0, "Tokyo"));
        tokyoHotels.add(new HotelOption("Shibuya Capsule Hotel", 60.0, "Tokyo"));
        tokyoHotels.add(new HotelOption("Shinjuku Business Hotel", 130.0, "Tokyo"));
        hotelDatabase.put("Tokyo", tokyoHotels);

        // Default hotels
        List<HotelOption> defaultHotels = new ArrayList<>();
        defaultHotels.add(new HotelOption("Premium Hotel", 160.0, "Default"));
        defaultHotels.add(new HotelOption("Budget Hotel", 70.0, "Default"));
        defaultHotels.add(new HotelOption("Standard Hotel", 100.0, "Default"));
        hotelDatabase.put("Default", defaultHotels);
    }

    private void provideHotelOptions(UserRequest request, jade.core.AID sender) {
        String destination = request.getDestination();
        List<HotelOption> options = hotelDatabase.getOrDefault(destination,
                hotelDatabase.get("Default"));

        // Filter based on budget (rough estimate: budget - transport cost)
        double estimatedMaxHotelBudget = request.getBudget() * 0.6; // Assume 60% for hotel
        List<HotelOption> affordableOptions = new ArrayList<>();
        for (HotelOption hotel : options) {
            if (hotel.getCostPerNight() * 3 <= estimatedMaxHotelBudget) {
                affordableOptions.add(hotel);
            }
        }

        if (affordableOptions.isEmpty()) {
            affordableOptions = options; // Include all if none are affordable
        }

        HotelOption[] optionsArray = affordableOptions.toArray(new HotelOption[0]);

        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(sender);
        try {
            reply.setContentObject(optionsArray);
            send(reply);
            System.out.println("HotelAgent: Sent " + affordableOptions.size() + " options for " + destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}