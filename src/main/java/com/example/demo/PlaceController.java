package com.example.demo;

import com.example.demo.model.Places;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PlaceController {
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/search")
    public ResponseEntity<List<Places>> searchPlaces(@RequestParam("text") String locationText, @RequestParam("longitude") double longitude, @RequestParam("latitude") double latitude,
                                                     @RequestParam("Cost") int costScore, @RequestParam("accessibility") int accessibilityScore, @RequestParam("popularity") int popularityScore,
                                                     @RequestParam("ArrayOfPlaces") ArrayList<String> arrayOfPlaces, @RequestParam("radius") int radius) {
        // Handle the search request
        //nearby search
        //Only 1 type is allowed, possibly running N number of GET requests may be needed to collect a certain amount of destinations?
        RestTemplate restTemplate = new RestTemplate();

        HashMap<String, List<String>> GroupToPlace = new HashMap<>();
        List<String> foodArray1 = Arrays.asList("cafe", "bar", "bakery", "restaurant");
        GroupToPlace.put("Food", foodArray1);

        String groupType1 = GroupToPlace.get(arrayOfPlaces.get(0)).get(0);

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude +
                "," + //cool, was %2C, java transforms % into %252C (Look into it, HTML url Encoding)
                longitude +
                "&radius=" +
                radius +
                "&type=" +
                groupType1 +
                "&key=" +
                SuperSecretApiKey.getApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        System.out.println(url);

       ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            System.out.println(responseBody);
            // Process the response body as needed
        } else {
            // Handle error cases
        }

        ResponseEntity<List<Places>> placesList2 = new ResponseEntity<>(HttpStatus.OK);
        return placesList2;

        //In my frontend I have longitude/latitude information
        //I will have other parameters that the user will select
        //I just want to get them into my backend so it can be manipulated

        //longitude, latitude,

        //The frontend is dumb, it cannot do anything other than obtain the data. I want to create
        //end points in my backend which will be able to receive the information.

    }

}
