package models;

import java.io.Serializable;

public class TransportOption implements Serializable {
    private String type;
    private double cost;
    private String destination;

    // 🔥 REQUIRED by Jackson
    public TransportOption() {
    }

    public TransportOption(String type, double cost, String destination) {
        this.type = type;
        this.cost = cost;
        this.destination = destination;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
}
