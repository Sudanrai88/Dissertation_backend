package com.example.demo.services.Algorithms;



import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.Population;
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
        int size = singleRank.size();

        // Initialize crowding distances to 0.
        for (Itinerary itin : singleRank) {
            itin.setCrowdingDistance(0.0);
            itin.setBestType("");
        }

        // Assign infinity to the best values for each objective
        Itinerary bestPopularity = bestPopularity(singleRank);
        Itinerary bestCost = bestCost(singleRank);
        Itinerary bestAccessibility = bestAccessibility(singleRank);

        bestPopularity.setCrowdingDistance(Double.POSITIVE_INFINITY);
        bestCost.setCrowdingDistance(Double.POSITIVE_INFINITY);
        bestAccessibility.setCrowdingDistance(Double.POSITIVE_INFINITY);

        // Calculate the crowding distance based on the objectives
        for (int obj = 0; obj < 3; obj++) {
            // Sort based on the objective
            int finalObj = obj;
            singleRank.sort(Comparator.comparing(itin -> getObjective(itin, finalObj)));

            for (int i = 1; i < size - 1; i++) {
                Itinerary itin = singleRank.get(i);
                if (itin.getCrowdingDistance() != Double.POSITIVE_INFINITY) {
                    double previousObjectiveValue = getObjective(singleRank.get(i - 1), obj);
                    double nextObjectiveValue = getObjective(singleRank.get(i + 1), obj);
                    double objectiveRange = getObjective(singleRank.get(size - 1), obj) - getObjective(singleRank.get(0), obj);
                    // Update the crowding distance for this objective
                    if (objectiveRange != 0) {
                        itin.setCrowdingDistance(itin.getCrowdingDistance() + (nextObjectiveValue - previousObjectiveValue) / objectiveRange);
                    }
                }
            }
        }
    }

    private static double getObjective(Itinerary itinerary, int objectiveIndex) {
        switch (objectiveIndex) {
            case 0:
                return itinerary.getNormalizedPopularityScore();
            case 1:
                return itinerary.getNormalizedCostScore();
            case 2:
                return itinerary.getNormalizedAccessibilityScore();
            default:
                throw new IllegalArgumentException("Invalid objective index");
        }
    }

    private static Itinerary bestCost(List<Itinerary> orderedItineraries) {
        double minCost = Double.POSITIVE_INFINITY;
        Itinerary selectedItinerary = null;

        for (Itinerary itinerary : orderedItineraries) {
            double costScore = itinerary.getNormalizedCostScore();
            if (costScore < minCost) {
                minCost = costScore;
                selectedItinerary = itinerary;
            }
        }
        assert selectedItinerary != null;
        selectedItinerary.setCrowdingDistance(Double.POSITIVE_INFINITY);
        selectedItinerary.setBestType("Cost");

        return selectedItinerary;
    }

    private static Itinerary bestPopularity(List<Itinerary> orderedItineraries) {
        double minPopularity = Double.POSITIVE_INFINITY;
        Itinerary selectedItinerary = null;
        for (Itinerary itinerary : orderedItineraries) {
            double popularityScore = itinerary.getNormalizedPopularityScore();
            if (popularityScore < minPopularity) {
                minPopularity = popularityScore;
                selectedItinerary = itinerary;
            }
        }
        assert selectedItinerary != null;
        selectedItinerary.setCrowdingDistance(Double.POSITIVE_INFINITY);
        selectedItinerary.setBestType("Popularity");
        return selectedItinerary;
    }

    private static Itinerary bestAccessibility(List<Itinerary> orderedItineraries) {
        double minAccessibility = Double.POSITIVE_INFINITY;
        Itinerary selectedItinerary = null;
        for (Itinerary itinerary : orderedItineraries) {
            double accessibilityScore = itinerary.getNormalizedAccessibilityScore();
            if (accessibilityScore < minAccessibility) {
                minAccessibility = accessibilityScore;
                selectedItinerary = itinerary;
            }
        }
        assert selectedItinerary != null;
        selectedItinerary.setCrowdingDistance(Double.POSITIVE_INFINITY);
        selectedItinerary.setBestType("Accessibility");

        return selectedItinerary;
    }

    private static void normalizeFitnessValues(ArrayList<Itinerary> itineraries) {
        double maxPopularity = itineraries.stream()
                .mapToDouble(Itinerary::getPopularityScore)
                .min().orElse(1.0);

        double maxCost = itineraries.stream()
                .mapToDouble(Itinerary::getCostScore)
                .max().orElse(1.0);

        double maxAccessibility = itineraries.stream()
                .mapToDouble(Itinerary::getAccessibilityScore)
                .max().orElse(1.0);

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
            // Check domination conditions
            boolean atLeastEqualInPopularity = individualB.getNormalizedPopularityScore() <= individualA.getNormalizedPopularityScore();
            boolean atLeastEqualInCost = individualB.getNormalizedCostScore() <= individualA.getNormalizedCostScore();
            boolean atLeastEqualInAccessibility = individualB.getNormalizedAccessibilityScore() <= individualA.getNormalizedAccessibilityScore();

            boolean strictlyBetterInAtLeastOne = (individualB.getNormalizedPopularityScore() < individualA.getNormalizedPopularityScore()) ||
                    (individualB.getNormalizedCostScore() < individualA.getNormalizedCostScore()) ||
                    (individualB.getNormalizedAccessibilityScore() < individualA.getNormalizedAccessibilityScore());

            if (atLeastEqualInPopularity && atLeastEqualInCost && atLeastEqualInAccessibility && strictlyBetterInAtLeastOne) {
                return false;
            }
        }
        return true;
    }

    public static ArrayList<Itinerary> GetCandidateParents(ArrayList<Itinerary> population) {
        ArrayList<Itinerary> chosenCandidates = new ArrayList<>();
        // Grab two random individuals from the population
        Itinerary candidateA = population.get(random.nextInt(population.size()));
        Itinerary candidateB = population.get(random.nextInt(population.size()));

        // Ensure that the two individuals are unique
        Collections.shuffle(population);

        candidateA = population.get(0);
        candidateB = population.get(1);

        chosenCandidates.add(candidateA);
        chosenCandidates.add(candidateB);

        return chosenCandidates;
    }

    public static Itinerary TournamentSelection(Itinerary candidate1, Itinerary candidate2) {

        Itinerary betterCandidate;
        Itinerary weakerCandidate;


        if (candidate1.getRank() < candidate2.getRank()) {
            betterCandidate = candidate1;
            weakerCandidate = candidate2;
        } else if (candidate1.getRank() > candidate2.getRank()) {
            betterCandidate = candidate2;
            weakerCandidate = candidate1;
        } else {
            // Rank is equal, use crowding distance to determine.
            betterCandidate = (candidate1.getCrowdingDistance() > candidate2.getCrowdingDistance()) ? candidate1 : candidate2;
            weakerCandidate = (betterCandidate == candidate1) ? candidate2 : candidate1;
        }

        int rankDifference = Math.abs(candidate1.getRank() - candidate2.getRank());

        // Adjust selection probability based on rank difference
        double selectionProbability = 0.9 - (0.05 * rankDifference);

        // 75% chance to choose the better candidate, low to increase diversity
        return (random.nextDouble() < selectionProbability) ? betterCandidate : weakerCandidate;
    }

    //changed from one-point to a uniform crossover.
    public static Itinerary doCrossover(Itinerary itineraryA, Itinerary itineraryB) {
        Random random = new Random();

        int sizeA = itineraryA.getListOfDestinations().size();
        int sizeB = itineraryB.getListOfDestinations().size();

        if (sizeA != sizeB) {
            throw new IllegalArgumentException("The sizes of the two itineraries must be the same for uniform crossover.");
        }
        List<Place> offspringSequence = new ArrayList<>(sizeA);
        // Uniform crossover
        for (int i = 0; i < sizeA; i++) {
            if (random.nextBoolean()) {
                offspringSequence.add(itineraryA.getListOfDestinations().get(i));
            } else {
                offspringSequence.add(itineraryB.getListOfDestinations().get(i));
            }
        }
        // Correct duplicates
        Set<Place> uniquePlaces = new HashSet<>(offspringSequence);
        for (int i = 0; i < offspringSequence.size(); i++) {
            Place currentPlace = offspringSequence.get(i);
            if (Collections.frequency(offspringSequence, currentPlace) > 1) { // if duplicate
                for (Place place : itineraryB.getListOfDestinations()) {
                    if (!uniquePlaces.contains(place)) {
                        offspringSequence.set(i, place);
                        uniquePlaces.add(place);
                        break;
                    }
                }
            }
        }
        Itinerary offspring = new Itinerary();
        offspring.setListOfDestinations(new ArrayList<>(offspringSequence));
        return offspring;
    }


    public static Itinerary mutate(Itinerary itinerary, ArrayList<Place> places) {
        Itinerary newItinerary = new Itinerary();
        ArrayList<Place> newItineraryList = new ArrayList<>(itinerary.getListOfDestinations());

        //use as a parameter (advanced setting / test by myself (hypervolume))
        if (random.nextDouble() < 0.1) {
            //choose a random num from 0 to length of itinerary - 1. Swap that place with another random place.
            int indexToRemove = random.nextInt(newItineraryList.size());
            newItineraryList.remove(indexToRemove);

            //use the getUniquePlace method to add to the newItineraryList.
            //Ensure that the newly added place is not already present in the List (Can check using place.getID).
            Place uniquePlace;
            do {
                uniquePlace = getUniquePlace(places);
            } while (containsPlaceWithId(newItineraryList, uniquePlace.getPlaceId()));

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


    public static double computeHypervolume(Population rank1, double ref_x, double ref_y, double ref_z) {
        List<Itinerary> sortedItineraries = new ArrayList<>(rank1.getItineraries());
        Collections.sort(sortedItineraries, Comparator.comparingDouble(Itinerary::getNormalizedPopularityScore));

        double hypervolume = 0;

        double prev_x = ref_x, prev_y = ref_y, prev_z = ref_z;


        for (Itinerary itinerary : sortedItineraries) {
            double x = itinerary.getNormalizedPopularityScore();
            double y = itinerary.getNormalizedAccessibilityScore();
            double z = itinerary.getNormalizedCostScore();

            double volume = (ref_x - x) * (ref_y - y) * (ref_z - z);
            double overlapVolume = (ref_x - x) * (ref_y - prev_y) * (ref_z - prev_z);

            hypervolume += volume - overlapVolume;

            prev_x = x;
            prev_y = y;
            prev_z = z;
        }

        return hypervolume;
    }
}

