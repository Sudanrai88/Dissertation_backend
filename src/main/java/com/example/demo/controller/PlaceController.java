package com.example.demo.controller;

import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.example.demo.services.Algorithms.Algorithms;
import com.example.demo.services.Algorithms.Coordinate;
import com.example.demo.services.LocationService;
import com.example.demo.services.PlaceService;
import com.example.demo.services.UserManagementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.Location;
import java.util.*;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api")
public class PlaceController {
    private final PlaceService placeService;
    private final Algorithms algorithms;
    private final LocationService locationService;

    @Autowired
    private UserManagementService userManagementService;

    public PlaceController(PlaceService placeService, Algorithms algorithms, LocationService locationService) {
        this.placeService = placeService;
        this.algorithms = algorithms;
        this.locationService = locationService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/search") //The responseEntity can be <void> after testing. Nothing needs to be sent back to the frontend.
    public ResponseEntity<List<Place>> searchPlaces(@RequestHeader("Authorization") String JWT, @RequestParam("text") String locationText,
                                                    @RequestParam("Cost") int costScore, @RequestParam("accessibility") int accessibilityScore, @RequestParam("popularity") int popularityScore,
                                                    @RequestParam("ArrayOfPlaces") ArrayList<String> arrayOfPlaces, @RequestParam("radius") int radius) throws Exception {


        //LocationService to turn "text" into longitude, latitude.
        System.out.println(locationText);
        List<Double> locations = locationService.getLocation(locationText);


        ArrayList<Place> places = placeService.fetchAllPlaces(locations.get(0), locations.get(1), radius, arrayOfPlaces, locations);
        System.out.println("Total Places = " + places.size());
        //Create the algorithms to sort out the data and give each place a fitness score depending on the objective functions

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        Coordinate currentLocation = new Coordinate(locations.get(0), locations.get(1));

        List<Itinerary> sortedBest = algorithms.geneticAlgorithm(places, radius, currentLocation, accessibilityScore, costScore, popularityScore);

        System.out.println(sortedBest);

        algorithms.saveItineraries(user, sortedBest);


        if (!places.isEmpty()) {
            for (Place place : places) {
                System.out.println(place.toString());
            }
            return new ResponseEntity<>(places, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }


    }
}


