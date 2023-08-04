package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

//Chromosome

public class Itinerary {
    private ArrayList<Place> listOfDestinations;

    private ArrayList<Double> currentScore;
    private ArrayList<String> idList;
    private String itineraryId;
    private int rank;
    private double crowdingDistance;

    private double popularityScore;
    private double accessibilityScore;
    private double costScore;

    private double normalizedPopularityScore;
    private double normalizedAccessibilityScore;
    private double normalizedCostScore;


    public ArrayList<Double> getNormalizedScoreList() {
        ArrayList<Double> normalizedScoreList = new ArrayList<>();

        normalizedScoreList.add(normalizedPopularityScore);
        normalizedScoreList.add(normalizedCostScore);
        normalizedScoreList.add(normalizedAccessibilityScore);

        return normalizedScoreList;
    }

    public void setCurrentScore(ArrayList<Double> currentScore) {
        this.currentScore = currentScore;
    }

    public double getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(double popularityScore) {
        this.popularityScore = popularityScore;
    }

    public double getAccessibilityScore() {
        return accessibilityScore;
    }

    public void setAccessibilityScore(double accessibilityScore) {
        this.accessibilityScore = accessibilityScore;
    }

    public double getCostScore() {
        return costScore;
    }

    public void setCostScore(double costScore) {
        this.costScore = costScore;
    }

    public void setIdList(ArrayList<String> idList) {
        this.idList = idList;
    }

    public List<String> getIdList() {
        return idList;
    }

    public ArrayList<Double> getCurrentScore() {
        return currentScore;
    }

    public String getItineraryId() {
        return itineraryId;
    }

    public void setItineraryId(String itineraryId) {
        this.itineraryId = itineraryId;
    }

    public double getNormalizedPopularityScore() {
        return normalizedPopularityScore;
    }

    public void setNormalizedPopularityScore(double normalizedPopularityScore) {
        this.normalizedPopularityScore = normalizedPopularityScore;
    }

    public double getNormalizedAccessibilityScore() {
        return normalizedAccessibilityScore;
    }

    public void setNormalizedAccessibilityScore(double normalizedAccessibilityScore) {
        this.normalizedAccessibilityScore = normalizedAccessibilityScore;
    }

    public double getNormalizedCostScore() {
        return normalizedCostScore;
    }

    public void setNormalizedCostScore(double normalizedCostScore) {
        this.normalizedCostScore = normalizedCostScore;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public ArrayList<Place> getListOfDestinations() {
        return listOfDestinations;
    }

    public void setListOfDestinations(ArrayList<Place> listOfDestinations) {
        this.listOfDestinations = listOfDestinations;
    }

    @Override
    public String toString() {
        return "Itinerary{" +
                "listOfDestinations=" + listOfDestinations +
                ", currentScore=" + currentScore +
                ", idList=" + idList +
                ", itineraryId='" + itineraryId + '\'' +
                ", rank=" + rank +
                ", crowdingDistance=" + crowdingDistance +
                ", popularityScore=" + popularityScore +
                ", accessibilityScore=" + accessibilityScore +
                ", costScore=" + costScore +
                ", normalizedPopularityScore=" + normalizedPopularityScore +
                ", normalizedAccessibilityScore=" + normalizedAccessibilityScore +
                ", normalizedCostScore=" + normalizedCostScore +
                '}';
    }
}
