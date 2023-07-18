package com.example.demo.controller;

import com.example.demo.model.Place;
import com.example.demo.services.PlaceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("/api")
public class PlaceController {
    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/search") //The responseEntity can be <void> after testing. Nothing needs to be sent back to the frontend.
    public ResponseEntity<List<Place>> searchPlaces(@RequestParam("text") String locationText, @RequestParam("longitude") double longitude, @RequestParam("latitude") double latitude,
                                                    @RequestParam("Cost") int costScore, @RequestParam("accessibility") int accessibilityScore, @RequestParam("popularity") int popularityScore,
                                                    @RequestParam("ArrayOfPlaces") ArrayList<String> arrayOfPlaces, @RequestParam("radius") int radius) throws JsonProcessingException {

        List<Place> places = placeService.fetchAllPlaces(latitude, longitude, radius, arrayOfPlaces);

        //Create the algorithms to sort out the data and give each place a fitness score depending on the objective functions.


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


