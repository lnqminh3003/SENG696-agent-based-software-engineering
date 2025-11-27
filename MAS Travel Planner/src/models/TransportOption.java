package models;

import java.io.Serializable;

public class TransportOption implements Serializable {
    private String type;
    private double cost;
    private String destination;

    public TransportOption(String type, double cost, String destination) {
        this.type = type;
        this.cost = cost;
        this.destination = destination;
    }

    public String getType() { return type; }
    public double getCost() { return cost; }
    public String getDestination() { return destination; }
}