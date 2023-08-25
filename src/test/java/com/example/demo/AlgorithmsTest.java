package com.example.demo;

import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.services.Algorithms.Algorithms;
import com.example.demo.services.Algorithms.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlgorithmsTest {

    private Algorithms algorithms;

    @BeforeEach
    public void setUp() {
        algorithms = new Algorithms();

    }



    @Test
    public void testGeneticAlgorithm() {
        // Test geneticAlgorithm method with sample data
        ArrayList<Place> places = new ArrayList<>();

        places.add(new Place("OPEN", "Place1", "ID1", -1.1539236, 52.952304, "images1", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 2.5, 100, 2));
        places.add(new Place("OPEN", "Place2", "ID2", -1.1602149, 52.92756749999999, "images2", new ArrayList<>(Arrays.asList("bar", "restaurant", "point_of_interest", "establishment")), 4.2, 50, 3));
        places.add(new Place("OPEN", "Place3", "ID3", -1.16436, 52.9447152, "images3", new ArrayList<>(Arrays.asList("point_of_interest", "amusement_park")), 3.1, 80, 4));
        places.add(new Place("OPEN", "Place4", "ID4", -1.2079374, 52.9514874, "images4", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 2.0, 60, 0));
        places.add(new Place("OPEN", "Place5", "ID5", -1.1524323, 52.951975, "images5", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.9, 90, 4));
        places.add(new Place("CLOSED", "Place6", "ID6", -1.1938571, 52.9547335, "images6", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.3, 110, 3));
        places.add(new Place("OPEN", "Place7", "ID7", -1.1534742, 52.95548110000001, "images7", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.8, 70, 2));
        places.add(new Place("OPEN", "Place8", "ID8", -1.1628795, 52.9343679, "images8", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 1.6, 40, 1));
        places.add(new Place("CLOSED", "Place9", "ID9", -1.1523079, 52.9536032, "images9", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 2.7, 80, 1));
        places.add(new Place("OPEN", "Place10", "ID10", -1.1517388, 52.947984, "images10", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 1.4, 120, 2));
        places.add(new Place("OPEN", "Place11", "ID11", -1.1485477, 52.9482739, "images11", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.2, 90, 4));
        places.add(new Place("CLOSED", "Place12", "ID12", -1.1528439, 52.95369179999999, "images12", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.5, 70, 3));
        places.add(new Place("OPEN", "Place13", "ID13", -1.1813696, 52.9314309, "images13", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 1.1, 100, 1));
        places.add(new Place("OPEN", "Place14", "ID14", -1.1546937, 52.9539574, "images14", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 0.6, 60, 2));
        places.add(new Place("CLOSED", "Place15", "ID15", -1.153436, 52.95295600000001, "images15", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.7, 80, 1));
        places.add(new Place("OPEN", "Place16", "ID16", -1.1542065, 52.9516463, "images16", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.0, 110, 3));
        places.add(new Place("OPEN", "Place17", "ID17", -1.1747454, 52.9557731, "images17", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 1.4, 70, 2));
        places.add(new Place("CLOSED", "Place18", "ID18", -1.1879212, 52.93865539999999, "images18", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.6, 40, 4));
        places.add(new Place("OPEN", "Place19", "ID19", -1.164975, 52.937935, "images19", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.8, 90, 1));
        places.add(new Place("OPEN", "Place20", "ID20", -1.16436, 52.9447152, "images20", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 2.2, 60, 5));
        places.add(new Place("CLOSED", "Place21", "ID21", -1.1517144, 52.9525071, "images21", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.3, 120, 3));
        places.add(new Place("OPEN", "Place22", "ID22", -1.1879212, 52.93865539999999, "images22", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.7, 100, 2));
        places.add(new Place("OPEN", "Place23", "ID23", -1.1716972, 52.9385089, "images23", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.1, 80, 4));
        places.add(new Place("CLOSED", "Place24", "ID24", -1.1873113, 52.938183, "images24", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.0, 50, 5));
        places.add(new Place("OPEN", "Resort Paradise", "ID25", -1.2079374, 52.9514874, "images25", new ArrayList<>(Arrays.asList("resort", "beach")), 4.7, 200, 4));
        places.add(new Place("OPEN", "Mountain Retreat", "ID26", -1.1787915, 52.935103, "images26", new ArrayList<>(Arrays.asList("mountain", "hiking")), 0.4, 150, 1));
        places.add(new Place("OPEN", "Historic Castle", "ID27", -1.1540254, 52.9354258, "images27", new ArrayList<>(Arrays.asList("castle", "history")), 2.6, 180, 3));
        places.add(new Place("CLOSED", "Mystery Island", "ID28", -1.1530009, 52.9549692, "images28", new ArrayList<>(Arrays.asList("island", "adventure")), 4.2, 100, 0));
        places.add(new Place("OPEN", "Safari Park", "ID29", -1.1875022, 52.9382503, "images29", new ArrayList<>(Arrays.asList("safari", "wildlife")), 3.3, 120, 2));
        places.add(new Place("OPEN", "Artistic Village", "ID30", -1.183609, 52.9475878, "images30", new ArrayList<>(Arrays.asList("art", "culture")), 1.1, 80, 4));
        places.add(new Place("CLOSED", "Underwater Wonderland", "ID31", -1.1890411, 52.9413071, "images31", new ArrayList<>(Arrays.asList("underwater", "scuba_diving")), 4.5, 90, 2));
        places.add(new Place("OPEN", "Adventure Park", "ID32", -1.1500284, 52.9439098, "images32", new ArrayList<>(Arrays.asList("adventure", "amusement_park")), 4.0, 160, 3));
        places.add(new Place("OPEN", "Cultural Center", "ID33", -1.175493, 52.93539, "images33", new ArrayList<>(Arrays.asList("culture", "history")), 1.9, 110, 2));
        places.add(new Place("CLOSED", "Sky High Observatory", "ID34", -1.1629057, 52.94485400000001, "images34", new ArrayList<>(Arrays.asList("observatory", "sightseeing")), 4.6, 70, 4));
        places.add(new Place("OPEN", "Cozy Retreat", "ID35", -1.1716972, 52.9385089, "images35", new ArrayList<>(Arrays.asList("relaxation", "nature")), 2.8, 130, 1));
        // Randomized placeTypes

        Coordinate currentLocation = new Coordinate(52.9428103, -1.1793827);

        List<Itinerary> result = algorithms.geneticAlgorithm(places, 3000,   currentLocation, 1,1,4);
        // Ensure the result contains the correct number of itineraries (maxPopulation)
        assertEquals(5, result.get(0).getListOfDestinations().size());

        for (int i = 0; i < 5; i++) {
            System.out.println(result.get(0).getListOfDestinations().get(i).getOrder());
        }
        //System.out.println(result.get(0).getListOfDestinations());
    }

    @Test
    public void testScorePopularity() {
        // Test scorePopularity method with different rating amounts
        double rating1 = 4.5;
        int ratingAmount1 = 50;
        double expectedScore1 = 4.5;

        double rating2 = 3.7;
        int ratingAmount2 = 20;
        double expectedScore2 = 3.35; // Calculated based on the formula in the method

       // assertEquals(expectedScore1, algorithms.scorePopularity(rating1, ratingAmount1));
        assertEquals(expectedScore2, algorithms.scorePopularity(rating2, ratingAmount2));
    }


    @Test
    public void  testSetPriceLevel() {
        // Test setPriceLevel method with sample data
        ArrayList<Place> places = new ArrayList<>();

        places.add(new Place("OPEN", "Place1", "ID1", -1.1539236, 52.952304, "images1", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.5, 100, 2));
        places.add(new Place("OPEN", "Place2", "ID2", -1.1602149, 52.92756749999999, "images2", new ArrayList<>(Arrays.asList("bar", "restaurant", "point_of_interest", "establishment")), 4.2, 50, 3));
        places.add(new Place("OPEN", "Place3", "ID3", -1.16436, 52.9447152, "images3", new ArrayList<>(Arrays.asList("point_of_interest", "amusement_park")), 4.1, 80, 4));
        places.add(new Place("OPEN", "Place4", "ID4", -1.2079374, 52.9514874, "images4", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.0, 60, 0));
        places.add(new Place("OPEN", "Place5", "ID5", -1.1524323, 52.951975, "images5", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.9, 90, 4));
        places.add(new Place("CLOSED", "Place6", "ID6", -1.1938571, 52.9547335, "images6", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.3, 110, 3));
        places.add(new Place("OPEN", "Place7", "ID7", -1.1534742, 52.95548110000001, "images7", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.8, 70, 2));
        places.add(new Place("OPEN", "Place8", "ID8", -1.1628795, 52.9343679, "images8", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.6, 40, 1));
        places.add(new Place("CLOSED", "Place9", "ID9", -1.1523079, 52.9536032, "images9", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.7, 80, 5));
        places.add(new Place("OPEN", "Place10", "ID10", -1.1517388, 52.947984, "images10", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.4, 120, 2));
        places.add(new Place("OPEN", "Place11", "ID11", -1.1485477, 52.9482739, "images11", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.2, 90, 4));
        places.add(new Place("CLOSED", "Place12", "ID12", -1.1528439, 52.95369179999999, "images12", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.5, 70, 3));
        places.add(new Place("OPEN", "Place13", "ID13", -1.1813696, 52.9314309, "images13", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.1, 100, 1));
        places.add(new Place("OPEN", "Place14", "ID14", -1.1546937, 52.9539574, "images14", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.9, 60, 2));
        places.add(new Place("CLOSED", "Place15", "ID15", -1.153436, 52.95295600000001, "images15", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.7, 80, 5));
        places.add(new Place("OPEN", "Place16", "ID16", -1.1542065, 52.9516463, "images16", new ArrayList<>(Arrays.asList("cafe", "bakery", "tourist_attraction")), 4.0, 110, 3));
        places.add(new Place("OPEN", "Place17", "ID17", 1.1747454, 52.9557731, "images17", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.4, 70, 2));
        places.add(new Place("CLOSED", "Place18", "ID18", -1.1879212, 52.93865539999999, "images18", new ArrayList<>(Arrays.asList("cafe", "restaurant", "bakery")), 4.6, 40, 4));
        places.add(new Place("OPEN", "Place19", "ID19", -1.164975, 52.937935, "images19", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.8, 90, 1));
        places.add(new Place("OPEN", "Place20", "ID20", -1.16436, 52.9447152, "images20", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.2, 60, 5));
        places.add(new Place("CLOSED", "Place21", "ID21", -1.1517144, 52.9525071, "images21", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.3, 120, 3));
        places.add(new Place("OPEN", "Place22", "ID22", -1.1879212, 52.93865539999999, "images22", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 3.7, 100, 2));
        places.add(new Place("OPEN", "Place23", "ID23", -1.1716972, 52.9385089, "images23", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.1, 80, 4));
        places.add(new Place("CLOSED", "Place24", "ID24", -1.1873113, 52.938183, "images24", new ArrayList<>(Arrays.asList("cafe", "restaurant", "tourist_attraction")), 4.0, 50, 5));
        places.add(new Place("OPEN", "Resort Paradise", "ID25", -1.2079374, 52.9514874, "images25", new ArrayList<>(Arrays.asList("resort", "beach")), 4.7, 200, 4));
        places.add(new Place("OPEN", "Mountain Retreat", "ID26", -1.1787915, 52.935103, "images26", new ArrayList<>(Arrays.asList("mountain", "hiking")), 4.5, 150, 5));
        places.add(new Place("OPEN", "Historic Castle", "ID27", -1.1540254, 52.9354258, "images27", new ArrayList<>(Arrays.asList("castle", "history")), 4.6, 180, 3));
        places.add(new Place("CLOSED", "Mystery Island", "ID28", -1.1530009, 52.9549692, "images28", new ArrayList<>(Arrays.asList("island", "adventure")), 4.2, 100, 0));
        places.add(new Place("OPEN", "Safari Park", "ID29", -1.1875022, 52.9382503, "images29", new ArrayList<>(Arrays.asList("safari", "wildlife")), 4.3, 120, 2));
        places.add(new Place("OPEN", "Artistic Village", "ID30", -1.183609, 52.9475878, "images30", new ArrayList<>(Arrays.asList("art", "culture")), 4.1, 80, 4));
        places.add(new Place("CLOSED", "Underwater Wonderland", "ID31", -1.1890411, 52.9413071, "images31", new ArrayList<>(Arrays.asList("underwater", "scuba_diving")), 4.5, 90, 5));
        places.add(new Place("OPEN", "Adventure Park", "ID32", -1.1500284, 52.9439098, "images32", new ArrayList<>(Arrays.asList("adventure", "amusement_park")), 4.0, 160, 3));
        places.add(new Place("OPEN", "Cultural Center", "ID33", -1.175493, 52.93539, "images33", new ArrayList<>(Arrays.asList("culture", "history")), 3.9, 110, 2));
        places.add(new Place("CLOSED", "Sky High Observatory", "ID34", -1.1629057, 52.94485400000001, "images34", new ArrayList<>(Arrays.asList("observatory", "sightseeing")), 4.6, 70, 4));
        places.add(new Place("OPEN", "Cozy Retreat", "ID35", -1.1716972, 52.9385089, "images35", new ArrayList<>(Arrays.asList("relaxation", "nature")), 3.8, 130, 1));

        java.util.Map<String, Integer> averagePrices = new java.util.HashMap<>();

        String[] placeTypes = {
                "cafe", "bar", "bakery", "restaurant", "tourist_attraction",
                "point_of_interest", "amusement_park", "natural_feature",
                "art_gallery", "museum", "stadium", "book_store", "painter"
        };

        //Loop to fill Map averagePrices.
        for (String placeType : placeTypes) {
            averagePrices.put(placeType, algorithms.averagePlaceTypePrice(placeType, places));
        }

        for (String placeType: placeTypes) {
            System.out.println(placeType + ": " +averagePrices.get(placeType));
        }

        System.out.println(places.get(3).getPrice()); //cafe, restaurant, tourist attraction        (3 + 3 + 2) / 3
        algorithms.setPriceLevel(places.get(3), averagePrices); //
        System.out.println(places.get(3).getPrice());

        assertEquals(3, places.get(3).getPrice());

       // Ensure the price level is set to the average price for the respective place type

    }

    @Test
    public void testAveragePlaceTypePrice() {
        // Test averagePlaceTypePrice method with sample data
        List<Place> places = Arrays.asList(
                new Place("OPEN", "Place1", "ID1", 37.7749, -122.4194, "images1", new ArrayList<>(Arrays.asList("cafe")), 4.5, 100, 10),
                new Place("OPEN", "Place2", "ID2", 37.7749, -122.4194, "images2", new ArrayList<>(Arrays.asList("cafe")), 4.2, 50, 15),
                new Place("CLOSED", "Place3", "ID3", 37.7749, -122.4194, "images3", new ArrayList<>(Arrays.asList("restaurant")), 3.8, 70, 20),
                new Place("OPEN", "Place4", "ID4", 37.7749, -122.4194, "images4", new ArrayList<>(Arrays.asList("restaurant")), 4.7, 30, 25),
                new Place("CLOSED", "Place5", "ID5", 37.7749, -122.4194, "images5", new ArrayList<>(Arrays.asList("bar")), 4.0, 120, 30)
        );

        int averageCafePrice = algorithms.averagePlaceTypePrice("cafe", places);
        int averageRestaurantPrice = algorithms.averagePlaceTypePrice("restaurant", places);
        int averageBarPrice = algorithms.averagePlaceTypePrice("bar", places);

        // Calculated averages:
        // Cafe prices: (10 + 15) / 2 = 12.5 (rounded to 13)
        // Restaurant prices: (20 + 25) / 2 = 22.5 (rounded to 23)
        // Bar prices: 30
       // assertEquals(13, averageCafePrice);
        //assertEquals(23, averageRestaurantPrice);
        //assertEquals(30, averageBarPrice);
    }
}