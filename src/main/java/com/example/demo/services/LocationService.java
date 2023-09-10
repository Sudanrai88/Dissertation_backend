package com.example.demo.services;

import com.example.demo.SuperSecretApiKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class LocationService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LocationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Double> getLocation(String locationText) throws Exception {

        List<Double> geoLocation = new ArrayList<>();

        //URL Encoding:
        //Directly appending locationText to the URL can cause issues if the text contains characters not valid for a URL. You should use URLEncoder.encode(locationText, StandardCharsets.UTF_8.toString()) to safely encode the text.
        String encodedLocation = URLEncoder.encode(locationText, StandardCharsets.UTF_8.toString());
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedLocation + "&key=" + SuperSecretApiKey.getApiKey();
        ResponseEntity<String> response = makeApiRequest(url);
        String responseBody = response.getBody();

        if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode resultsNode = root.path("results");

            // This ensures you get the first result (assuming there might be multiple)
            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode geometryNode = resultsNode.get(0).path("geometry");
                JsonNode locationNode = geometryNode.path("location");

                Double placeLatitude = locationNode.path("lat").asDouble();
                Double placeLongitude = locationNode.path("lng").asDouble();

                geoLocation.add(placeLatitude);
                geoLocation.add(placeLongitude);
            } else {
                throw new Exception("No results found in the response.");
            }
        } else {
            throw new Exception("Geo-Location API Error. Please try again.");
        }

        return geoLocation;
    }

    private ResponseEntity<String> makeApiRequest (String url){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}
