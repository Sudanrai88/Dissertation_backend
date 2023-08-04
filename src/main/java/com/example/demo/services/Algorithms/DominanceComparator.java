package com.example.demo.services.Algorithms;


import com.example.demo.model.Itinerary;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class DominanceComparator {

    public static void updatePopulationFitness(ArrayList<Itinerary> population) {
        // Clear the existing ranks and crowding distances
        for (Itinerary itinerary : population) {
            itinerary.setRank(-1);
            itinerary.setCrowdingDistance(-1);
        }

        normalizeFitnessValues(population);

        ArrayList<Itinerary> remainingToBeRanked = new ArrayList<>(population);

        // Put every individual in the population into their fronts
        int rank = 1;
        while (!remainingToBeRanked.isEmpty()) {
            ArrayList<Itinerary> itinerariesInRank = new ArrayList<>();

            for (int i = 0; i < remainingToBeRanked.size(); i++) {
                Itinerary itinerary = remainingToBeRanked.get(i);
                if (isNotDominated(itinerary, remainingToBeRanked)) {
                    itinerary.setRank(rank);
                    itinerariesInRank.add(itinerary);
                }
            }

            itinerariesInRank.forEach(remainingToBeRanked::remove);

            rank++;
        }

        // For each rank, calculate the crowding distance for each individual
        Map<Integer, ArrayList<Itinerary>> ranks = population.stream().collect(Collectors.groupingBy(Itinerary::getRank, Collectors.toCollection(ArrayList::new)));
        ranks.forEach((rankNumber, singleRank) -> calculateCrowdingDistance(singleRank));
    }

    private static void calculateCrowdingDistance(ArrayList<Itinerary> singleRank) {
        // As we only have three objectives, ordering individuals along one front allows us to make assumptions
        // about the locations of the neighboring individuals in the array.
        List<Itinerary> orderedItineraries = singleRank;
        List<Itinerary> boundaries = new ArrayList<>();
        Itinerary bestPop = bestPopularity(orderedItineraries);
        Itinerary bestCost = bestCost(orderedItineraries);
        Itinerary bestAcc = bestAccessibility(orderedItineraries);

        orderedItineraries.remove(bestPop);
        orderedItineraries.remove(bestCost);
        orderedItineraries.remove(bestAcc);

        int itinerariesInFront = orderedItineraries.size();

        boundaries.add(bestPop);
        boundaries.add(bestCost);
        boundaries.add(bestAcc);

        //For the remaining itineraries calculate the 3D Euclidean distance
        Map<Integer, ArrayList<Double>> intToDistance = new HashMap<>();

        for (int i = 0; i < itinerariesInFront; i++) {
            ArrayList<Double> listOfDistances = new ArrayList<>();
            double distance;
            Vector3D currentItinerary = new Vector3D(orderedItineraries.get(i).getNormalizedPopularityScore(),
                    orderedItineraries.get(i).getNormalizedPopularityScore(),
                    orderedItineraries.get(i).getNormalizedPopularityScore());

            for (int j = 0; j < itinerariesInFront; j++) {
                if (i != j) {
                Vector3D otherItinerary = new Vector3D(orderedItineraries.get(j).getNormalizedPopularityScore(),
                        orderedItineraries.get(j).getNormalizedPopularityScore(),
                        orderedItineraries.get(j).getNormalizedPopularityScore());

                    distance = currentItinerary.distance(otherItinerary);

                    listOfDistances.add(distance);
                }
            }

            intToDistance.put(i, listOfDistances);
        }

        /*for (int i = 0; i < intToDistance.size(); i++) {
            System.out.println("Key: " + i + intToDistance.get(i));
        }*/

        Double maxDistance = intToDistance.values()
                .stream()
                .flatMap(List::stream) // Flatten the list of distances
                .max(Double::compareTo) // Find the maximum distance
                .orElse(0.0);

        //Normalize the distance
        for (ArrayList<Double> distances : intToDistance.values()) {
            for (int i = 0; i < distances.size(); i++) {
                distances.set(i, distances.get(i) / maxDistance);
            }
        }

        /*for (int i = 0; i < intToDistance.size(); i++) {
            System.out.println("Key: " + i + intToDistance.get(i));
        }*/

        for (int i = 0; i < itinerariesInFront; i++) {
            List<Double> distances = intToDistance.get(i);

            // Find the smallest three distances
            List<Double> smallestThree = distances.stream()
                    .sorted() // Sort the distances in ascending order
                    .limit(3)  // Take the first three (smallest) distances
                    .collect(Collectors.toList());

            double closest3 = smallestThree.stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            System.out.println(closest3);

            orderedItineraries.get(i).setCrowdingDistance(closest3/3);
        }

    }

    private static Itinerary bestCost(List<Itinerary> orderedItineraries) {
        double minCost = Double.POSITIVE_INFINITY;
        Itinerary selectedItinerary = null;
        for (Itinerary itinerary : orderedItineraries) {
            double costScore = itinerary.getCostScore();
            if (costScore < minCost) {
                minCost = costScore;
                selectedItinerary = itinerary;
            }
        }
        assert selectedItinerary != null;
        selectedItinerary.setCrowdingDistance(Double.POSITIVE_INFINITY);
        return selectedItinerary;
    }

    private static Itinerary bestPopularity(List<Itinerary> orderedItineraries) {
        double minPopularity = Double.POSITIVE_INFINITY;
        Itinerary selectedItinerary = null;
        for (Itinerary itinerary : orderedItineraries) {
            double popularityScore = itinerary.getPopularityScore();
            if (popularityScore < minPopularity) {
                minPopularity = popularityScore;
                selectedItinerary = itinerary;
            }
        }
        assert selectedItinerary != null;
        selectedItinerary.setCrowdingDistance(Double.POSITIVE_INFINITY);
        return selectedItinerary;
    }

    private static Itinerary bestAccessibility(List<Itinerary> orderedItineraries) {
        double minAccessibility = Double.POSITIVE_INFINITY;
        Itinerary selectedItinerary = null;
        for (Itinerary itinerary : orderedItineraries) {
            double accessibilityScore = itinerary.getAccessibilityScore();
            if (accessibilityScore < minAccessibility) {
                minAccessibility = accessibilityScore;
                selectedItinerary = itinerary;
            }
        }
        assert selectedItinerary != null;
        selectedItinerary.setCrowdingDistance(Double.POSITIVE_INFINITY);
        return selectedItinerary;
    }

    private static void normalizeFitnessValues(ArrayList<Itinerary> itineraries)
    {
        double maxPopularity = itineraries.stream()
                .mapToDouble(Itinerary::getPopularityScore)
                .min().orElse(1.0); // To avoid division by zero

        double maxCost = itineraries.stream()
                .mapToDouble(Itinerary::getCostScore)
                .max().orElse(1.0); // To avoid division by zero

        double maxAccessibility = itineraries.stream()
                .mapToDouble(Itinerary::getAccessibilityScore)
                .min().orElse(1.0); // To avoid division by zero

        itineraries.forEach(i -> i.setNormalizedPopularityScore(-i.getPopularityScore() / maxPopularity));
        itineraries.forEach(i -> i.setNormalizedCostScore(i.getCostScore() / maxCost));
        itineraries.forEach(i -> i.setNormalizedAccessibilityScore(-i.getAccessibilityScore() / maxAccessibility));
    }

    private static boolean isNotDominated(Itinerary individualA, ArrayList<Itinerary> remainingToBeRanked) {
        // Loop over each individual and check if it dominates this individual.
        for (Itinerary individualB : remainingToBeRanked) {
            if (individualA.equals(individualB)) {
                continue;
            }
            // If this individual is at least better than us in one objective and equal in another,
            // then we are dominated by this individual
            if (individualB.getPopularityScore() <= individualA.getPopularityScore() &&
                    individualB.getCostScore() <= individualA.getCostScore() &&
                    individualB.getAccessibilityScore() <= individualA.getAccessibilityScore()) {
                return false;
            }
        }

        return true;
    }


}

