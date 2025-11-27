package models;

import java.io.Serializable;

public class TravelPlan implements Serializable {
    private String destination;
    private String startDate;
    private String endDate;
    private String transportType;
    private double transportCost;
    private String hotelName;
    private double hotelCost;
    private double totalCost;
    private int rank;

    public TravelPlan(String destination, String startDate, String endDate,
                      String transportType, double transportCost,
                      String hotelName, double hotelCost) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transportType = transportType;
        this.transportCost = transportCost;
        this.hotelName = hotelName;
        this.hotelCost = hotelCost;
        this.totalCost = transportCost + hotelCost;
    }

    public double getTotalCost() { return totalCost; }
    public void setRank(int rank) { this.rank = rank; }

    @Override
    public String toString() {
        return String.format("Plan #%d:\n" +
                        "  Destination: %s\n" +
                        "  Dates: %s to %s\n" +
                        "  Transport: %s ($%.2f)\n" +
                        "  Hotel: %s ($%.2f)\n" +
                        "  TOTAL: $%.2f\n",
                rank, destination, startDate, endDate,
                transportType, transportCost, hotelName, hotelCost, totalCost);
    }
}