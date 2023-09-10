package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.model.Requests.ItineraryRequest;
import com.example.demo.model.Requests.OrderRequest;
import com.example.demo.model.Requests.PlaceRequest;
import com.example.demo.services.ItineraryService;
import com.example.demo.services.LocationService;
import com.example.demo.services.UserManagementService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ItineraryService itineraryService;

    @Autowired
    private LocationService locationService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @PostMapping("/select")
    public ResponseEntity<Void> selectItinerary(@RequestHeader("Authorization") String JWT, @RequestBody ItineraryRequest itineraryRequest) throws FirebaseAuthException, ExecutionException, InterruptedException {
        // Delete other itineraries and keep the one user selected

        System.out.println(itineraryRequest.getItineraryId());
        System.out.println(itineraryRequest.getItineraryName());

        itineraryService.selectItinerary(JWT, itineraryRequest.getItineraryId(), itineraryRequest.getItineraryName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @PostMapping("/addToPopular")
    public ResponseEntity<Void> addItineraryToPopularItineraries(@RequestHeader("Authorization") String JWT, @RequestBody ItineraryRequest itineraryRequest) throws FirebaseAuthException, ExecutionException, InterruptedException {

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        System.out.println(itineraryRequest.getItineraryId());


        itineraryService.addItineraryToPopularItineraries(user, itineraryRequest.getItineraryId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @PutMapping("/updatePlace")
    public ResponseEntity<Void> updatePlace(@RequestHeader("Authorization") String JWT,
                                            @RequestParam("itineraryId") String itineraryId,
                                            @RequestBody Place updatedPlace) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        itineraryService.updatePlaceInItinerary(userId, itineraryId, updatedPlace);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @DeleteMapping("/deletePlace")
    public ResponseEntity<Void> deletePlace(@RequestHeader("Authorization") String JWT,
                                            @RequestParam("itineraryId") String itineraryId,
                                            @RequestParam("placeId") String placeId, @RequestParam("index") int index) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        itineraryService.deletePlaceFromItinerary(userId, itineraryId, placeId, index);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @DeleteMapping("/deleteItinerary")
    public ResponseEntity<Void> deleteItinerary(@RequestHeader("Authorization") String JWT,
                                            @RequestParam("itineraryId") String itineraryId) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        itineraryService.deleteItinerary(userId, itineraryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @GetMapping("/fetchTempItineraries")
    public ResponseEntity<List<Itinerary>> fetchTempItineraries(@RequestHeader("Authorization") String JWT) throws FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        List<Itinerary> itineraries = new ArrayList<>(itineraryService.fetchTempItinerariesForUser(user));

        System.out.println("Fetched itineraries: " + itineraries.size());

        return new ResponseEntity<>(itineraries, HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @GetMapping("/fetchItineraries")
    public ResponseEntity<List<Itinerary>> fetchItineraries(@RequestHeader("Authorization") String JWT) throws FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        List<Itinerary> itineraries = new ArrayList<>(itineraryService.fetchItineraries(user));
        System.out.println("Fetched itineraries: " + itineraries.size());

        return new ResponseEntity<>(itineraries, HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @GetMapping("/fetchPopularItineraries")
    public ResponseEntity<List<Itinerary>> fetchPopularItineraries(@RequestHeader("Authorization") String JWT) throws FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        List<Itinerary> itineraries = new ArrayList<>(itineraryService.fetchPopularItineraries());

        Collections.sort(itineraries, (i1, i2) -> Integer.compare(i2.getUserLikes(), i1.getUserLikes()));

        System.out.println("Fetched Popular itineraries: " + itineraries.size());

        return new ResponseEntity<>(itineraries, HttpStatus.OK);
    }


    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @GetMapping("/changeLike")
    public void changeLike( @RequestParam("itineraryId") String itineraryId, @RequestParam("value") int value) throws FirebaseAuthException, ExecutionException, InterruptedException {
        System.out.println("Changed");

        itineraryService.changeLike(itineraryId, value);
    }


    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @GetMapping("/{id}")
    public ResponseEntity<Itinerary> getItinerary(@RequestHeader("Authorization") String JWT, @PathVariable String id) throws FirebaseAuthException, ExecutionException, InterruptedException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());

        Itinerary itinerary = itineraryService.fetchItineraryById(user, id);

        if (itinerary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(itinerary, HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @GetMapping("/popular/{id}")
    public ResponseEntity<Itinerary> getPopularItinerary( @PathVariable String id) throws FirebaseAuthException, ExecutionException, InterruptedException {

        Itinerary itinerary = itineraryService.fetchPopularItinerary(id);

        if (itinerary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(itinerary, HttpStatus.OK);
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @PostMapping("/addNewItinerary")
    public void addNewItinerary(@RequestHeader("Authorization") String JWT, @RequestBody PlaceRequest request) throws Exception {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);

        User user = new User();

        System.out.println(request.getIndex());

        user.setUid(decodedToken.getUid());
        itineraryService.addNewItinerary(request.getInputValue(), user, request.getItineraryId(), request.getIndex());
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})
    @PostMapping("/swapOrderIndex")
    public void swapOrderIndex(@RequestHeader("Authorization") String JWT, @RequestBody OrderRequest request) throws Exception {

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        User user = new User();
        user.setUid(decodedToken.getUid());
        System.out.println("Hello");

        itineraryService.swapIndexOrder(request.getItineraryId(), user, request.getToGoDestination(), request.getSource());
    }
}
