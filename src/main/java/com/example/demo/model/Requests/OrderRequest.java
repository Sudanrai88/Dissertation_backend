package com.example.demo.model.Requests;

public class OrderRequest {
    private String itineraryId;
    private int source;
    private int toGoDestination;

    public OrderRequest() {
    };

    public String getItineraryId() {
        return itineraryId;
    }

    public void setItineraryId(String itineraryId) {
        this.itineraryId = itineraryId;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getToGoDestination() {
        return toGoDestination;
    }

    public void setToGoDestination(int toGoDestination) {
        this.toGoDestination = toGoDestination;
    }
}
