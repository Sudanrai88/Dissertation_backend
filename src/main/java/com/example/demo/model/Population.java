package com.example.demo.model;

import java.util.ArrayList;

public class Population {
    private ArrayList<Itinerary> itineraries = new ArrayList<>();
    Itinerary itinerary;

    public Population() {
    }

    public ArrayList<Itinerary> getItineraries() {
        return itineraries;
    }

    public void setItineraries(ArrayList<Itinerary> itineraries) {
        this.itineraries = itineraries;
    }

    public void addItinerary(Itinerary itinerary) {
        itineraries.add(itinerary);
    }

    public Itinerary getSpecificItinerary(String intineraryID) {
        return itinerary;
    }
}
