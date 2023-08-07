package com.example.demo.services.Algorithms;


import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.Population;
import com.example.demo.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.example.demo.services.Algorithms.shortestPath.findShortestTotalDistance;


@Service
public class Algorithms {
    public List<Itinerary> geneticAlgorithm(ArrayList<Place> places, int radius, Coordinate currentLocation) {

        //proceed with scoredDestination places
        //perform genetic algorithm steps
        int maxPopulation = 0;
        //Init the generations
        switch (radius) {
            case 1000 -> maxPopulation = 10;
            case 2000 -> maxPopulation = 20;
            case 3000 -> maxPopulation = 40;
            case 4000 -> maxPopulation = 50;
            case 5000 -> maxPopulation = 60;
            default -> {
            }
        }
        int itineraryLength = 5;
        int generationalLoop = 0;

        //Remove any destination with a closed businessStatus
        places.removeIf(place -> "CLOSED".equals(place.getBusinessStatus()));
        places.removeIf(place -> "CLOSED_TEMPORARILY".equals(place.getBusinessStatus()));


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
        while (generationalLoop <= 5) {
            //Run objective functions on all destinations of each population. Tracks placeID for each destination in the Population.
            if (generationalLoop == 0) {
                runObjectiveFunctions(Population.getItineraries(), currentLocation, itineraryLength);
                //Non-dominated sorting + crowding distance --used a library
                geneticAlgoHelper.updatePopulationFitness(Population.getItineraries());
            }

            System.out.println("This is generation: " + generationalLoop);

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

                //Perform Crossover

                tempOffSprings.add(GetOffspring(father,mother).get(0));
                tempOffSprings.add(GetOffspring(father,mother).get(1));

                //Mutate
                Mutate(tempOffSprings.get(0), places);
                Mutate(tempOffSprings.get(1), places);

                //Add offSpring to the population
                offSpring.add(tempOffSprings.get(0));
                offSpring.add(tempOffSprings.get(1));
            }

            Population.getItineraries().addAll(offSpring);
            runObjectiveFunctions(Population.getItineraries(), currentLocation, itineraryLength);

            geneticAlgoHelper.updatePopulationFitness(Population.getItineraries());

            List<Itinerary> newPopulation;

            Population.getItineraries().sort(Comparator
                    .comparing(Itinerary::getRank)
                    .thenComparing(Itinerary::getCrowdingDistance, Comparator.reverseOrder()));

            newPopulation = new ArrayList<>(Population.getItineraries().subList(0, maxPopulation));

            Population.getItineraries().clear();

            for (Itinerary itinerary: newPopulation) {
                Population.addItinerary(itinerary);
            }



            for (int i = 0; i < 5; i++) {
                System.out.println("Rank: " + Population.getItineraries().get(i).getRank() + " normalized values: " + Population.getItineraries().get(i).getNormalizedScoreList() + " //Crowding distance: " + Population.getItineraries().get(i).getCrowdingDistance());
            }

            System.out.println(Population.getItineraries().get(0).toString());
            //testing (unit testing), problem instances (Sudan visits etc etc)

        }
        //testing (unit testing), problem instances (Sudan visits etc etc)
        //With the final list of itineraries, gravitate towards the users choice. E.g. put a weighting through

        System.out.println("Nice");
        return Population.getItineraries().subList(0,5);
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
        return geneticAlgoHelper.doCrossover(itineraryA, itineraryB, -1);
    }

    private Itinerary Mutate(Itinerary itinerary, ArrayList<Place> places) {
        return geneticAlgoHelper.mutate(itinerary, places);
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
            itineraries.get(i).setCostScore((totalPrice/itineraryLength));
            itineraries.get(i).setAccessibilityScore(findShortestTotalDistance(coordinates, currentLocation) * -1000);
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

    public List<Place> top5(List<Place> places) {
        List<Place> newPlaces = places.subList(0,5);
        return newPlaces;
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
                .filter(c -> c.getPrice() > 0)//filter out anything that doesn't contain a cafe and anythign with price = 0
                .toList();

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

    public List<String> saveItineraries(User user, List<Itinerary> itineraries) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<String> timestamps = new ArrayList<>();

        int i = 0;
        for (Itinerary itinerary : itineraries) {
            i++;
            String itineraryName = "Itinerary: " + i;

            // Save each itinerary as a document under the "itineraries" sub-collection.
            dbFirestore.collection("users").document(user.getUid())
                    .collection("itineraries").document(itineraryName)
                    .set(itinerary);

            // Save each place as a document under the "places" sub-collection of the itinerary.
            for (Place place : itinerary.getListOfDestinations()) {
                String destinationName = place.getName();
                ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("users").document(user.getUid())
                        .collection("tempItineraries").document(itineraryName)
                        .collection("Destination List").document("Destination: " + destinationName)
                        .set(place);

                timestamps.add(collectionApiFuture.get().getUpdateTime().toString());
            }
        }

        return timestamps;
    }










}

