package models;

import java.io.Serializable;

public class UserRequest implements Serializable {
    private String destination;
    private String startDate;
    private String endDate;
    private double budget;

    public UserRequest(String destination, String startDate, String endDate, double budget) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
    }

    public String getDestination() { return destination; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public double getBudget() { return budget; }
}