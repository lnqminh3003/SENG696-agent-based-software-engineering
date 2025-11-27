package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.*;
import models.TransportOption;
import models.UserRequest;

public class TransportAgent extends Agent {
    private Map<String, List<TransportOption>> transportDatabase;

    @Override
    protected void setup() {
        System.out.println("TransportAgent " + getLocalName() + " is ready.");
        initializeDatabase();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    try {
                        UserRequest request = (UserRequest) msg.getContentObject();
                        provideTransportOptions(request, msg.getSender());
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
        transportDatabase = new HashMap<>();

        // Paris options
        List<TransportOption> parisTransport = new ArrayList<>();
        parisTransport.add(new TransportOption("Flight", 350.0, "Paris"));
        parisTransport.add(new TransportOption("Train", 180.0, "Paris"));
        parisTransport.add(new TransportOption("Bus", 85.0, "Paris"));
        transportDatabase.put("Paris", parisTransport);

        // London options
        List<TransportOption> londonTransport = new ArrayList<>();
        londonTransport.add(new TransportOption("Flight", 280.0, "London"));
        londonTransport.add(new TransportOption("Train", 150.0, "London"));
        londonTransport.add(new TransportOption("Bus", 60.0, "London"));
        transportDatabase.put("London", londonTransport);

        // New York options
        List<TransportOption> nyTransport = new ArrayList<>();
        nyTransport.add(new TransportOption("Flight", 650.0, "New York"));
        nyTransport.add(new TransportOption("Train", 320.0, "New York"));
        nyTransport.add(new TransportOption("Bus", 150.0, "New York"));
        transportDatabase.put("New York", nyTransport);

        // Tokyo options
        List<TransportOption> tokyoTransport = new ArrayList<>();
        tokyoTransport.add(new TransportOption("Flight", 950.0, "Tokyo"));
        tokyoTransport.add(new TransportOption("Train", 520.0, "Tokyo"));
        transportDatabase.put("Tokyo", tokyoTransport);

        // Default options for any other destination
        List<TransportOption> defaultTransport = new ArrayList<>();
        defaultTransport.add(new TransportOption("Flight", 500.0, "Default"));
        defaultTransport.add(new TransportOption("Train", 250.0, "Default"));
        defaultTransport.add(new TransportOption("Bus", 120.0, "Default"));
        transportDatabase.put("Default", defaultTransport);
    }

    private void provideTransportOptions(UserRequest request, jade.core.AID sender) {
        String destination = request.getDestination();
        List<TransportOption> options = transportDatabase.getOrDefault(destination,
                transportDatabase.get("Default"));

        TransportOption[] optionsArray = options.toArray(new TransportOption[0]);

        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(sender);
        try {
            reply.setContentObject(optionsArray);
            send(reply);
            System.out.println("TransportAgent: Sent " + options.size() + " options for " + destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}