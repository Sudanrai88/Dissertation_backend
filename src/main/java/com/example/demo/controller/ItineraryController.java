package com.example.demo.controller;

import com.example.demo.model.Itinerary;
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

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ItineraryService itineraryService; // assuming you have a service for itineraries

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/select")
    public ResponseEntity<Void> selectItinerary(@RequestHeader("Authorization") String JWT, @RequestParam("itineraryId") String itineraryId) throws FirebaseAuthException, ExecutionException, InterruptedException {
        // Delete other itineraries and keep the one user selected
        itineraryService.selectItinerary(JWT, itineraryId);
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
    @GetMapping("/fetchItineraries")
    public ResponseEntity<List<Itinerary>> fetchItineraries(@RequestHeader("Authorization") String JWT) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        List<Itinerary> itineraries = itineraryService.fetchItinerariesForUser(user);
        return new ResponseEntity<>(itineraries, HttpStatus.OK);
    }


    // Add other CRUD endpoints for itineraries here if necessary
}
