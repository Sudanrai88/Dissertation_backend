package com.example.demo.services.Algorithms;


import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class geneticAlgoHelper {

    private static Random random = new Random();

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
                .max().orElse(1.0); // To avoid division by zero

        itineraries.forEach(i -> i.setNormalizedPopularityScore(-i.getPopularityScore() / maxPopularity));
        itineraries.forEach(i -> i.setNormalizedCostScore(i.getCostScore() / maxCost));
        itineraries.forEach(i -> i.setNormalizedAccessibilityScore(i.getAccessibilityScore() / maxAccessibility));
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

                // A log to understand which object dominates and why
                /*System.out.println("Individual A [Popularity Score: " + individualA.getPopularityScore()
                        + ", Cost Score: " + individualA.getCostScore() + ", Accessibility Score: "
                        + individualA.getAccessibilityScore() + "] is dominated by Individual B [Popularity Score: "
                        + individualB.getPopularityScore() + ", Cost Score: " + individualB.getCostScore()
                        + ", Accessibility Score: " + individualB.getAccessibilityScore() + "].");*/

                return false;
            }
        }

        // Add a log to know when an individual is not dominated
        /*System.out.println("Individual A [Popularity Score: " + individualA.getPopularityScore()
                + ", Cost Score: " + individualA.getCostScore() + ", Accessibility Score: "
                + individualA.getAccessibilityScore() + "] is not dominated by any other individual.");*/

        return true;
    }

    public static ArrayList<Itinerary> GetCandidateParents(ArrayList<Itinerary> population)
    {
        ArrayList<Itinerary> pairedCandidates = new ArrayList<>();
        // Grab two random individuals from the population
        Itinerary candidateA = population.get(random.nextInt(population.size()));
        Itinerary candidateB = population.get(random.nextInt(population.size()));

        // Ensure that the two individuals are unique
        while (candidateA == candidateB)
        {
            candidateB = population.get(random.nextInt(population.size()));
        }

        pairedCandidates.add(candidateA);
        pairedCandidates.add(candidateB);

        return pairedCandidates;
    }

    public static Itinerary TournamentSelection(Itinerary candidate1, Itinerary candidate2) {
        Random random = new Random();

        Itinerary betterCandidate;
        Itinerary weakerCandidate;

        if (candidate1.getRank() < candidate2.getRank()) {
            betterCandidate = candidate1;
            weakerCandidate = candidate2;
        } else if (candidate1.getRank() > candidate2.getRank()) {
            betterCandidate = candidate2;
            weakerCandidate = candidate1;
        } else { // candidate1.getRank() == candidate2.getRank()
            if (candidate1.getCrowdingDistance() > candidate2.getCrowdingDistance()) {
                betterCandidate = candidate1;
                weakerCandidate = candidate2;
            } else {
                betterCandidate = candidate2;
                weakerCandidate = candidate1;
            }
        }

        // 75% chance to choose the better candidate
        return (random.nextDouble() < 0.75) ? betterCandidate : weakerCandidate;
    }

    public static Itinerary doCrossover(Itinerary itineraryA, Itinerary itineraryB, int crossoverPosition) {
        Random random = new Random();
        // Find the minimum size between both itineraries
        int minSize = Math.min(itineraryA.getListOfDestinations().size(), itineraryB.getListOfDestinations().size());

        // Generate a number between 1 and minSize - 1 to be our crossover position
        crossoverPosition = crossoverPosition == -1
                ? random.nextInt(minSize - 1) + 1
                : crossoverPosition;

        // Grab the head from the first individual
        ArrayList<Place> offspringSequence = new ArrayList<>(itineraryA.getListOfDestinations().subList(0, crossoverPosition));

        // Create a hash for quicker 'exists in head' checks
        Set<Place> appeared = new HashSet<>(offspringSequence);

        // Append individualB to the head, skipping any values that have already shown up in the head,
        // and not exceeding the minimum size of the original sequences
        for (Place place : itineraryB.getListOfDestinations()) {
            if (appeared.contains(place) || offspringSequence.size() >= minSize) {
                continue;
            }
            offspringSequence.add(place);
        }

        // Create the offspring itinerary
        Itinerary offspring = new Itinerary();
        offspring.setListOfDestinations(offspringSequence);

        // Return our new offspring!
        return offspring;
    }


    public static Itinerary mutate(Itinerary itinerary, ArrayList<Place> places) {
        Itinerary newItinerary = new Itinerary();
        ArrayList<Place> newItineraryList = new ArrayList<>(itinerary.getListOfDestinations());


        //use as a parameter (advanced setting / test by myself (hypervolume))
        if (random.nextDouble() < 0.05) {
            //choose a random num from 0 to length of itinerary - 1. Swap that place with another random place.
            System.out.println("Mutation Occurred");
            int indexToRemove = random.nextInt(newItineraryList.size());
            newItineraryList.remove(indexToRemove);

            //use the getUniquePlace method to add to the newItineraryList.
            //Ensure that the newly added place is not already present in the List (Can check using place.getID).
            Place uniquePlace;
            do {
                uniquePlace = getUniquePlace(places);
            } while(containsPlaceWithId(newItineraryList, uniquePlace.getPlaceId()));

            //The newItineraryList should be set to the newItinerary.
            newItineraryList.add(uniquePlace);
            newItinerary.setListOfDestinations(newItineraryList);
            return newItinerary;
        }

        return itinerary;
    }

    public static Place getUniquePlace(ArrayList<Place> places) {
        Random random = new Random();
        int placeIndex = random.nextInt(places.size());
        return places.get(placeIndex);
    }

    public static boolean containsPlaceWithId(ArrayList<Place> places, String id) {
        for (Place place : places) {
            if (place.getPlaceId().equals(id)) {
                return true;
            }
        }
        return false;
    }


}

