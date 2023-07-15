package com.example.demo;

import com.example.demo.model.Place;
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
    @GetMapping("/search")
    public ResponseEntity<List<Place>> searchPlaces(@RequestParam("text") String locationText, @RequestParam("longitude") double longitude, @RequestParam("latitude") double latitude,
                                                    @RequestParam("Cost") int costScore, @RequestParam("accessibility") int accessibilityScore, @RequestParam("popularity") int popularityScore,
                                                    @RequestParam("ArrayOfPlaces") ArrayList<String> arrayOfPlaces, @RequestParam("radius") int radius) throws JsonProcessingException {

        List<Place> places = placeService.fetchAllPlaces(latitude, longitude, radius, arrayOfPlaces);

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


