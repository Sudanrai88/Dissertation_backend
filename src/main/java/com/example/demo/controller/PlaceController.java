package com.example.demo.controller;

import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.example.demo.services.Algorithms.Algorithms;
import com.example.demo.services.Algorithms.Coordinate;
import com.example.demo.services.PlaceService;
import com.example.demo.services.UserManagementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api")
public class PlaceController {
    private final PlaceService placeService;
    private final Algorithms algorithms;

    @Autowired
    private UserManagementService userManagementService;

    public PlaceController(PlaceService placeService, Algorithms algorithms) {
        this.placeService = placeService;
        this.algorithms = algorithms;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/search") //The responseEntity can be <void> after testing. Nothing needs to be sent back to the frontend.
    public ResponseEntity<List<Place>> searchPlaces(@RequestHeader("Authorization") String JWT, @RequestParam("text") String locationText, @RequestParam("longitude") double longitude, @RequestParam("latitude") double latitude,
                                                    @RequestParam("Cost") int costScore, @RequestParam("accessibility") int accessibilityScore, @RequestParam("popularity") int popularityScore,
                                                    @RequestParam("ArrayOfPlaces") ArrayList<String> arrayOfPlaces, @RequestParam("radius") int radius) throws JsonProcessingException, ExecutionException, InterruptedException, FirebaseAuthException {

        ArrayList<Place> places = placeService.fetchAllPlaces(latitude, longitude, radius, arrayOfPlaces);
        //Create the algorithms to sort out the data and give each place a fitness score depending on the objective functions.
        List<Place> top5Places = algorithms.top5(places);
        System.out.println(top5Places);

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());
        Coordinate currentLocation = new Coordinate(latitude, longitude);

        System.out.println("HERE");
        algorithms.geneticAlgorithm(places, radius, currentLocation);
        System.out.println("HERE");


        for (Place place:top5Places) {
            algorithms.saveItineraries(user, place, locationText, place.getName());
        }

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


