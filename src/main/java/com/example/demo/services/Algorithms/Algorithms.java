package com.example.demo.services.Algorithms;


import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.Population;
import com.example.demo.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.example.demo.services.Algorithms.shortestPath.findShortestTotalDistance;


@Service
public class Algorithms {

    private double previousConvergenceArea = 100;

    public List<Itinerary> geneticAlgorithm(ArrayList<Place> places, int radius, Coordinate currentLocation, int accessibilityScore, int costScore, int popularityScore) {

        //googleMaps API to find location / distance as the current is based on euclidean distance not Manhattan

        //5 solutions. 3 extreme solutions (automatic optimals (infinite crowding distance), 2 specified to the user chosen objectives (Still more diversity))

        //proceed with scoredDestination places
        //perform genetic algorithm steps

        //Potential to add multi-day Itineraries?
        // In the generate page, another selectable tab that allows you to select from a calendar -> 16/08 - 24/08.

        // Change current single generate page to have a calendar as well. Send information through to the backend and show in accounts!

        //Show normalised values getting better with each generation, can be recorded for 25 generations on 10 different itineraries that are created.

        Random rand = new Random();
        int maxPopulation = 0;
        //Init the generations
        switch (radius) {

            case 1000, 2000, 3000, 4000 -> maxPopulation = 300;
            case 5000 -> maxPopulation = 300;

            default -> {
            }
        }
        int itineraryLength = 5;
        int generationalLoop = 0;
        int NoImprovementCount = 0;

        //Remove any destination with a closed businessStatus
        places.removeIf(place -> "CLOSED".equals(place.getBusinessStatus()));
        places.removeIf(place -> "CLOSED_TEMPORARILY".equals(place.getBusinessStatus()));

        //remove any duplicate locations (same names && same Ids)tfg
        removeDuplicates(places);


        //priceLevel per chosen placeType, filter places and choose by place-type. filter that placeTypeGroup by priceLevel > 0. Average priceLevel for all leftover. That is the averagePriceLevel.
        Map<String, Integer> averagePrices = new HashMap<>();
        String[] placeTypes = {
                "cafe", "bar", "bakery", "restaurant", "tourist_attraction",
                "point_of_interest", "amusement_park", "natural_feature",
                "art_gallery", "museum", "stadium", "book_store", "painter"
        };

        //Loop to fill Map averagePrices.
        for (String placeType : placeTypes) {
            averagePrices.put(placeType, averagePlaceTypePrice(placeType, places));
        }
        //Some destinations are missing price scores and ratings. This should be taken into account, for now a
        //basic algorithm to deal with this will be created, mapping certain place types with 'approximated' values
        //The approximated value will be the average value of price from the search space.
        for (Place place : places) {
            for (int j = 0; j < itineraryLength; j++) {
                if (place.getPrice() == 0) {
                    setPriceLevel(place, averagePrices);
                }
            }
        }

        //Future use to ensure we can get the place from just placeID
        Map<String, Place> mappedPlaces = new HashMap<>();
        for (Place place: places) {
            mappedPlaces.put(place.getPlaceId(), place);
        }

        //INITIALIZE POPULATION
        Population Population = new Population();
        initializePopulation(places, Population, maxPopulation, itineraryLength);


        //START GENERATIONAL LOOP, Stop after x generations or in the future when hyperVolume is not reducing.

        while (generationalLoop <= 50) {
            //Run objective functions on all destinations of each population. Tracks placeID for each destination in the Population.
            if (generationalLoop == 0) {
                runObjectiveFunctions(Population.getItineraries(), currentLocation, itineraryLength);
                //Non-dominated sorting + crowding distance --used a library
                geneticAlgoHelper.updatePopulationFitness(Population.getItineraries());
            }
            generationalLoop++;

            ArrayList<Itinerary> offSpring = new ArrayList<>();
            while(offSpring.size() < Population.getItineraries().size()) {
                ArrayList<Itinerary> tempOffSprings = new ArrayList<>();

                //Get Parents
                Itinerary father = GetParent(Population);
                Itinerary mother = GetParent(Population);

                while (mother == father)
                {
                    father = GetParent(Population);
                }

                //advanced settings + probability of crossover ()
                //Perform Crossover
                if (rand.nextDouble() < 0.90) {
                    tempOffSprings.add(GetOffspring(father,mother).get(0));
                    tempOffSprings.add(GetOffspring(father,mother).get(1));

                    Mutate(tempOffSprings.get(0), places);
                    Mutate(tempOffSprings.get(1), places);

                    offSpring.add(tempOffSprings.get(0));
                    offSpring.add(tempOffSprings.get(1));
                }

                //advanced settings
                //Mutate
                //Add offSpring to the population
            }

            runObjectiveFunctions(offSpring, currentLocation, itineraryLength);
            Population.getItineraries().addAll(offSpring);

            geneticAlgoHelper.updatePopulationFitness(Population.getItineraries());


            List<Itinerary> newPopulation;

            Population.getItineraries().sort(Comparator
                    .comparing(Itinerary::getRank)
                    .thenComparing(Itinerary::getCrowdingDistance, Comparator.reverseOrder()));


            //Find a reference point, pop, cost, access



            double averageCrowding = 0;
            int count = 0;

            for (Itinerary itinerary: Population.getItineraries()) {
                if(itinerary.getCrowdingDistance() != Double.POSITIVE_INFINITY) {
                    count++;
                    averageCrowding += itinerary.getCrowdingDistance();
                }
            }

            averageCrowding = averageCrowding/ count;


            /*System.out.println(averageCrowding);*/

            newPopulation = new ArrayList<>(Population.getItineraries().subList(0, maxPopulation));
            Population.getItineraries().clear();

            for (Itinerary itinerary: newPopulation) {
                Population.addItinerary(itinerary);
            }

            Population rank1 = new Population();

            for(Itinerary itinerary: Population.getItineraries()) {
                if(itinerary.getRank() == 1) {
                    rank1.addItinerary(itinerary);
                }
            }

            Collections.sort(rank1.getItineraries(), Comparator.comparingDouble(Itinerary::getNormalizedPopularityScore));

            double currentVolume = geneticAlgoHelper.computeHypervolume(rank1, 0.1, 1.1, 1.1);

            double averageCost = 0;
            double averagePop = 0;
            double averageAccess = 0;
            double lowestCost = 1;
            double lowestPop = 1;
            double lowestAccess = 1;
            double lowestCost2 = 1;
            double lowestPop2 = 1;
            double lowestAccess2 = 1;

            for (int i = 0; i < Population.getItineraries().size(); i++) {
                averageCost += Population.getItineraries().get(i).getNormalizedCostScore();
                averageAccess += Population.getItineraries().get(i).getNormalizedAccessibilityScore();
                averagePop += Population.getItineraries().get(i).getNormalizedPopularityScore();

                if (Population.getItineraries().get(i).getNormalizedCostScore() < lowestCost) {
                    lowestCost = Population.getItineraries().get(i).getNormalizedCostScore();
                    lowestCost2 = Population.getItineraries().get(i).getCostScore();
                }
                if (Population.getItineraries().get(i).getNormalizedAccessibilityScore() < lowestAccess) {
                    lowestAccess = Population.getItineraries().get(i).getNormalizedAccessibilityScore();
                    lowestAccess2 = Population.getItineraries().get(i).getAccessibilityScore();

                }
                if(Population.getItineraries().get(i).getNormalizedPopularityScore() < lowestPop) {
                    lowestPop = Population.getItineraries().get(i).getNormalizedPopularityScore();
                    lowestPop2 = Population.getItineraries().get(i).getPopularityScore();

                }

            }

            averageCost = averageCost/Population.getItineraries().size();
            averageAccess = averageAccess/Population.getItineraries().size();
            averagePop = averagePop/Population.getItineraries().size();



            System.out.println("AverageCost = " + averageCost + " AverageAccess = " + averageAccess + " AveragePop = " + averagePop + " HyperVolume = " + currentVolume + " AverageCrowding = " + averageCrowding);
            System.out.println("lowestCost = " + lowestCost + " lowestCost2 = " + lowestCost2 + " lowestAccess = " + lowestAccess +  " lowestAccess2 = " + lowestAccess2 + " lowestPop = " + lowestPop +  " lowestPop2 = " + lowestPop2);

            /*System.out.println("This generation rank 1 = " + rank1.getItineraries().size());
            System.out.println("This is averageCrowding = " + averageCrowding);*/
            /*for (int i = 0; i < Population.getItineraries().size(); i++) {
                System.out.println("NUMBER: " + i + "     Rank: " + Population.getItineraries().get(i).getRank() + " normalized values: " + Population.getItineraries().get(i).getNormalizedScoreList() + " //Crowding distance: " + Population.getItineraries().get(i).getCrowdingDistance());
                for (int j = 0; j < Population.getItineraries().get(i).getListOfDestinations().size(); j++) {
                    System.out.println(Population.getItineraries().get(i).getListOfDestinations().get(j).toString());
                }
                System.out.println(Population.getItineraries().get(i));
            }*/
            //testing (unit testing), problem instances (Sudan visits etc etc)

        }
        //testing (unit testing), problem instances (Sudan visits etc etc)

        //With the final list of itineraries, gravitate towards the users choice.

        System.out.println("Nice");

        ArrayList<Itinerary> finalList = new ArrayList<>(selectByUserPreference(Population.getItineraries(), accessibilityScore, costScore, popularityScore));
        ArrayList<Itinerary> toAddList = new ArrayList<>();

        for (Itinerary itin: Population.getItineraries()) {
            if (itin.getBestType().equals("Popularity") || itin.getBestType().equals("Accessibility") || itin.getBestType().equals("Cost")) {
                toAddList.add(itin);
            }
        }

        //If itinerary 1 2 or 3 is the same, the get the second best of Pop, Access or Cost.

        finalList.addAll(toAddList);


        for (Itinerary itinerary: finalList) {
            int order = 0;
            ArrayList<Place> clonedPlaces = new ArrayList<>();

            for (Place place: itinerary.getListOfDestinations()) {
                Place clonedPlace = place.clone();
                order++;
                clonedPlace.setOrder(order);
                clonedPlaces.add(clonedPlace);
                System.out.println(clonedPlace);
            }

            itinerary.setListOfDestinations(clonedPlaces); //
        }

        //getting random numbers as each place is unique and is getting re-written (setOrder) by the next itinerary that contains the same place.
        //Need a way to only affect the order of the place for the specific itinerary

        //Can use cloneing to fix the issue above^



        //Add order here where the sorting is done
        //The index of the ones below the added destination should be +1 to keep indexing in order.


        //order in the browser is set to 0.

        //The first should be the best priced
        //2nd -> best ratings
        //3rd -> best accessibility

        //4th -> best fit 1
        //5th -> best fit 2

        //System.out.println(Population.getItineraries().subList(0,5));

        return finalList;
    }



    private Itinerary GetParent(Population population) {
        ArrayList<Itinerary> candidates = geneticAlgoHelper.GetCandidateParents(population.getItineraries());
        Itinerary Candidate1 = candidates.get(0);
        Itinerary Candidate2 = candidates.get(1);

        return geneticAlgoHelper.TournamentSelection(Candidate1, Candidate2);
    }

    private ArrayList<Itinerary> GetOffspring(Itinerary itineraryA, Itinerary itineraryB) {

        ArrayList<Itinerary> offSprings = new ArrayList<>();
        // Generate the offspring from our selected parents
        Itinerary offspringA = DoCrossover(itineraryA, itineraryB);
        Itinerary offspringB = DoCrossover(itineraryB, itineraryA);

        offSprings.add(offspringA);
        offSprings.add(offspringB);

        return offSprings;
    }

    private Itinerary DoCrossover(Itinerary itineraryA, Itinerary itineraryB) {
        return geneticAlgoHelper.doCrossover(itineraryA, itineraryB);
    }

    private Itinerary Mutate(Itinerary itinerary, ArrayList<Place> places) {
        return geneticAlgoHelper.mutate(itinerary, places);
    }


    public ArrayList<Itinerary> selectByUserPreference(ArrayList<Itinerary> itineraries, int accessibilityScore, int costScore, int popularityScore) {

        Collections.sort(itineraries, (itin1, itin2) -> Double.compare(
                desirability(itin2, accessibilityScore, costScore, popularityScore),
                desirability(itin1, accessibilityScore, costScore, popularityScore)
        ));

        // 1. Remove itineraries with crowding distance of infinity
        ArrayList<Itinerary> filteredItineraries = (ArrayList<Itinerary>) itineraries.stream()
                .filter(itin -> itin.getCrowdingDistance() != Double.POSITIVE_INFINITY)
                .collect(Collectors.toList());

        // 3. Take top itinerary and then find a non-similar one
        ArrayList<Itinerary> top2 = new ArrayList<>();
        top2.add(filteredItineraries.get(0));

        for (int i = 1; i < filteredItineraries.size(); i++) {
            if (!areSimilar(top2.get(0), filteredItineraries.get(i))) {
                top2.add(filteredItineraries.get(i));
                break;
            }
        }

        return top2;
    }

    private boolean areSimilar(Itinerary itin1, Itinerary itin2) {
        List<String> places1 = itin1.getIdList(); // assuming there's a method like this
        List<String> places2 = itin2.getIdList();

        int commonCount = 0;
        for (String place : places1) {
            if (places2.contains(place)) {
                commonCount++;
            }
        }

        // If 3 or more places are the same, they are similar
        return commonCount >= 3;
    }

    private double desirability(Itinerary itin, int accessibilityScore, int costScore, int popularityScore) {
        // Assuming the normalized getters are named like getNormalizedCost(), getNormalizedAccessibility(), etc.

        double accessibilityValue = 1 - itin.getNormalizedAccessibilityScore(); // Better values are closer to 0, so 1 - value makes it more desirable
        double costValue = 1 - itin.getNormalizedCostScore();
        double popularityValue = itin.getNormalizedPopularityScore() * -1; // Since Popularity = 0 is the worst and -1 is the best, adding 1 will normalize it to [0, 1]

        // Return a combined score based on user preferences
        return accessibilityScore * accessibilityValue + costScore * costValue + popularityScore * popularityValue;
    }

    //CONSTRAINT THAT 3 Itineraries has to be from user group type selection 1, and 2 from user group type selection 2. E.g. selection 1: Food, selection 2: Artistic. 3/5 itineraries from Food, 2/5 from Artistic.
    public void initializePopulation (ArrayList<Place> places, Population population, int maxPopulation, int itineraryLength) {
        for (int i = 0; i < maxPopulation; i++) {
            Collections.shuffle(places);
            ArrayList<Place> shuffledList = new ArrayList<>(places.subList(0, itineraryLength));
            Itinerary itinerary = new Itinerary();
            itinerary.setListOfDestinations(shuffledList);
            itinerary.setItineraryId("Itinerary: " + i);
            population.addItinerary(itinerary);
/*
            System.out.println("//Name = " + population.getItineraries().get(i).getItineraryId() + " //Itinerary in population: " + population.getItineraries().get(i).getListOfDestinations());
*/
        }
    }

    public void runObjectiveFunctions (ArrayList<Itinerary> itineraries, Coordinate currentLocation, int itineraryLength) {
        for (int i = 0; i < itineraries.size(); i++) {
            List<Place> placeList = itineraries.get(i).getListOfDestinations();
            double totalPopularity = 0;
            double totalPrice = 0;
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            ArrayList<String> listOfPlaceId = new ArrayList<>();
            ArrayList<Double> scoredPopulation = new ArrayList<>();
            for (int j = 0; j < itineraryLength; j++) {
                totalPopularity += scorePopularity(placeList.get(j).getRating(), placeList.get(j).getRating_amount());
                totalPrice += placeList.get(j).getPrice();
                coordinates.add(j, new Coordinate(placeList.get(j).getLatitude(), placeList.get(j).getLongitude()));
                listOfPlaceId.add(placeList.get(j).getPlaceId());
            }
            itineraries.get(i).setPopularityScore((totalPopularity/itineraryLength) * -1);
            itineraries.get(i).setCostScore((totalPrice/itineraryLength) * 1);
            itineraries.get(i).setAccessibilityScore(findShortestTotalDistance(coordinates, currentLocation) * 1000);
            scoredPopulation.add(itineraries.get(i).getPopularityScore());
            scoredPopulation.add(itineraries.get(i).getCostScore());
            scoredPopulation.add(itineraries.get(i).getAccessibilityScore());
            itineraries.get(i).setIdList(listOfPlaceId);
            itineraries.get(i).setCurrentScore(scoredPopulation);
            coordinates.clear();
        }
    }

    public double scorePopularity (double rating, int ratingAmount) {
        int cutoff = 40;

        // Step 1: Check if the rating amount is above the cutoff.
        if (ratingAmount >= cutoff) {
            // If above the cutoff, use the actual rating as the final rating.
            return rating;
        } else {
            // Calculate the popularity score
            double popularityScore = (0.7 * rating) + (0.3 * Math.log(1 + ratingAmount));
            popularityScore = Math.round(popularityScore * 100.0) / 100.0;
            return popularityScore;
        }
    }

    public void setPriceLevel(Place destination, Map<String,Integer> averagePrices) {
        List<String> placeTypes = destination.getPlaceTypes();
        double averageTotalPrice = 0;
        double validPlaceTypesCount = 0; // Count of place types with valid average price

        for (String placeType : placeTypes) {
            if (averagePrices.containsKey(placeType)) {
                // Get the average price for the placeType from the map
                int averagePrice = averagePrices.get(placeType);
                averageTotalPrice += averagePrice;
                validPlaceTypesCount++;
            }
        }
        // To get the average, divide by validPlaceTypesCount
        if (validPlaceTypesCount > 0) {
            double averagePricePerPlaceType = averageTotalPrice / validPlaceTypesCount;
            int averagePriceTotal = (int) Math.round(averagePricePerPlaceType);
            destination.setPrice(averagePriceTotal);
        } else {
            destination.setPrice(0);
        }
    }

        public int averagePlaceTypePrice(String placeType, List<Place> places) {
            List<Place> placeTypes = places
                .stream()
                .filter(c -> c.getPlaceTypes().contains(placeType))
                .filter(c -> c.getPrice() > 0)
                .toList();

        //get everything

        if (placeTypes.isEmpty()) {
            int total = 1;
            return total;// return 1 to cover any places with is of the placeType but has a value of 0. E.g. ESTABLISHMENT, for some reason is always 0...
        }

        double averagePrice = 0;
        int validPlaces= 0;

        for (Place place: placeTypes) {
            if(place.getPrice() != 0) {
                averagePrice += place.getPrice();
                validPlaces++;
            }
        }
        if(validPlaces == 0) {
            int total = 1;
            return total;
        }
        averagePrice = averagePrice/validPlaces;
        int totalAveragePrice = (int) Math.round(averagePrice);
        return totalAveragePrice;
    }

    //Somewhere here should be a service that makes a place details call to the description of each place.

    public void saveItineraries(User user, List<Itinerary> itineraries) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference tempItinerariesRef = dbFirestore.collection("users").document(user.getUid()).collection("tempItineraries");

        int i = 0;
        for (Itinerary itinerary : itineraries) {
            i++;
            String itineraryName = "Itinerary: " + i;
            itinerary.setItineraryId(itineraryName);
            // Save each itinerary as a document under the "itineraries" sub-collection.
            tempItinerariesRef.document(itineraryName)
                    .set(itinerary);

            // Save each place as a document under the "places" sub-collection of the itinerary.

            for (Place place : itinerary.getListOfDestinations()) {
                String destinationId = place.getPlaceId();
                ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("users").document(user.getUid())
                        .collection("tempItineraries").document(itineraryName)
                        .collection("Destination List").document("Destination: " + destinationId)
                        .set(place);

            }
        }
    }

    public void removeDuplicates (ArrayList<Place> places) {

        for (int i = 0; i < places.size(); i++) {
            Place currentPlace = places.get(i);
            for (int j = i + 1; j < places.size(); j++) {
                Place comparingPlace = places.get(j);
                if (currentPlace.getName().equals(comparingPlace.getName()) ||
                        currentPlace.getPlaceId().equals(comparingPlace.getPlaceId())) {
                    places.remove(j);
                    j--;  // Adjust the index due to removal
                }
            }
        }
    }










}

