import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class TravelPlannerLauncher {

    public static void main(String[] args) {
        System.setProperty("jade_core_messaging_MessageManager_level", "SEVERE");
        System.setProperty("jade.logging", "off");

        try {
            // Get JADE runtime
            Runtime runtime = Runtime.instance();

            // Create main container profile
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");

            // Create main container
            AgentContainer mainContainer = runtime.createMainContainer(profile);

            // Create and start agents
            AgentController userAgent = mainContainer.createNewAgent(
                    "user", "agents.UserAgent", null);

            AgentController plannerAgent = mainContainer.createNewAgent(
                    "planner", "agents.PlannerAgent", null);

            AgentController transportAgent = mainContainer.createNewAgent(
                    "transport", "agents.TransportAgent", null);

            AgentController hotelAgent = mainContainer.createNewAgent(
                    "hotel", "agents.HotelAgent", null);

            AgentController paymentAgent = mainContainer.createNewAgent(
                    "payment", "agents.PaymentAgent", null);

            // Start all agents
            userAgent.start();
            plannerAgent.start();
            transportAgent.start();
            hotelAgent.start();
            paymentAgent.start();

            System.out.println("\n=================================");
            System.out.println("Simple Travel Planner MAS Started");
            System.out.println("=================================\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}