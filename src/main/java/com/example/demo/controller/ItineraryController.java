package com.example.demo.controller;

import com.example.demo.model.Itinerary;
import com.example.demo.model.ItineraryRequest;
import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.example.demo.services.ItineraryService;
import com.example.demo.services.UserManagementService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/select")
    public ResponseEntity<Void> selectItinerary(@RequestHeader("Authorization") String JWT, @RequestBody ItineraryRequest itineraryRequest) throws FirebaseAuthException, ExecutionException, InterruptedException {
        // Delete other itineraries and keep the one user selected

        System.out.println(itineraryRequest.getItineraryId());
        System.out.println(itineraryRequest.getItineraryName());

        itineraryService.selectItinerary(JWT, itineraryRequest.getItineraryId(), itineraryRequest.getItineraryName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping("/updatePlace")
    public ResponseEntity<Void> updatePlace(@RequestHeader("Authorization") String JWT,
                                            @RequestParam("itineraryId") String itineraryId,
                                            @RequestBody Place updatedPlace) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        itineraryService.updatePlaceInItinerary(userId, itineraryId, updatedPlace);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @DeleteMapping("/deletePlace")
    public ResponseEntity<Void> deletePlace(@RequestHeader("Authorization") String JWT,
                                            @RequestParam("itineraryId") String itineraryId,
                                            @RequestParam("placeId") String placeId) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        itineraryService.deletePlaceFromItinerary(userId, itineraryId, placeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/fetchTempItineraries")
    public ResponseEntity<List<Itinerary>> fetchTempItineraries(@RequestHeader("Authorization") String JWT) throws FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        List<Itinerary> itineraries = new ArrayList<>(itineraryService.fetchItinerariesForUser(user));

        System.out.println("Fetched itineraries: " + itineraries.size());

        return new ResponseEntity<>(itineraries, HttpStatus.OK);
    }


    // Add other CRUD endpoints for itineraries here if necessary
}
