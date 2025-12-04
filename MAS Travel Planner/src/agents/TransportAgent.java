package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.TransportOption;
import models.UserRequest;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TransportAgent extends Agent {

    private Map<String, List<TransportOption>> transportDatabase;

    @Override
    protected void setup() {
        System.out.println("TransportAgent " + getLocalName() + " is ready.");

        loadDatabaseFromJson();

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

    private void loadDatabaseFromJson() {
        ObjectMapper mapper = new ObjectMapper();

        // Build a relative path to the project directory
        String basePath = System.getProperty("user.dir");  // project root folder
        String fullPath = basePath + File.separator + "src" + File.separator +
                "database" + File.separator + "transport_data.json";

        try {
            transportDatabase = mapper.readValue(
                    new File(fullPath),
                    new TypeReference<Map<String, List<TransportOption>>>() {}
            );

//            System.out.println("TransportAgent: Loaded transport data from " + fullPath);

        } catch (IOException e) {
            System.err.println("TransportAgent: ERROR loading JSON at: " + fullPath);
            transportDatabase = new HashMap<>();
            e.printStackTrace();
        }
    }


    private void provideTransportOptions(UserRequest request, jade.core.AID sender) {
        String destination = request.getDestination();

        List<TransportOption> options = transportDatabase.getOrDefault(
                destination,
                transportDatabase.getOrDefault("Default", new ArrayList<>())
        );

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
