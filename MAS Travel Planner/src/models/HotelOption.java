package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class HotelOption implements Serializable {

    private String name;

    @JsonProperty("price") // JSON → Java cost per night
    private double costPerNight;

    private String destination;

    public HotelOption() {}  // <-- REQUIRED for Jackson

    public HotelOption(String name, double costPerNight, String destination) {
        this.name = name;
        this.costPerNight = costPerNight;
        this.destination = destination;
    }

    public String getName() { return name; }
    public double getCostPerNight() { return costPerNight; }
    public String getDestination() { return destination; }
}
