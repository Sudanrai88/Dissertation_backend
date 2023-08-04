package com.example.demo.services;

import com.example.demo.SuperSecretApiKey;
import com.example.demo.model.Place;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PlaceService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PlaceService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ArrayList<Place> fetchAllPlaces(double latitude, double longitude, int radius, ArrayList<String> arrayOfPlaces) {
        ArrayList<Place> places = new ArrayList<>();

        HashMap<String, List<String>> groupToPlace = new HashMap<>();
        groupToPlace.put("Food", Arrays.asList("cafe", "bar", "bakery", "restaurant"));
        groupToPlace.put("Tourist Spots", Arrays.asList("tourist_attraction", "point_of_interest", "amusement_park", "natural_feature"));
        groupToPlace.put("Art", Arrays.asList("art_gallery", "museum"));
        groupToPlace.put("Active", Arrays.asList("stadium", "amusement_park"));
        groupToPlace.put("Relaxed", Arrays.asList("book_store", "cafe", "painter"));

        for (String arrayOfPlace : arrayOfPlaces) {
            List<String> placeTypes = groupToPlace.get(arrayOfPlace);

            if (placeTypes != null) {
                for (String placeType : placeTypes) {
                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                            latitude + "," + longitude +
                            "&radius=" + radius +
                            "&type=" + placeType +
                            "&key=" + SuperSecretApiKey.getApiKey();

                    String responseBody;
                    int counter = 0;

                    do {
                        ResponseEntity<String> response = makeApiRequest(url);
                        responseBody = response.getBody();
                        System.out.println(responseBody);

                        if (response.getStatusCode() == HttpStatus.OK) {
                            JsonNode root;
                            try {
                                root = objectMapper.readTree(responseBody);
                            } catch (JsonProcessingException e) {
                                // Handle JSON parsing exception if needed
                                e.printStackTrace();
                                break;
                            }

                            JsonNode resultsNode = root.get("results");

                            if (resultsNode.isArray()) {
                                for (JsonNode resultNode : resultsNode) {
                                    // Extract place details from each resultNode
                                    String businessStatus;

                                    if (resultNode.has("business_status")) {
                                        businessStatus = resultNode.get("business_status").asText();
                                    } else {
                                        businessStatus = "";
                                    }

                                    int price_level;
                                    if (resultNode.has("price_level")) {
                                        price_level = resultNode.get("price_level").asInt();
                                    } else {
                                        price_level = 0;
                                    }

                                    String name = resultNode.get("name").asText();
                                    String placeId = resultNode.get("place_id").asText();
                                    Double placeLatitude = resultNode.get("geometry").get("location").get("lat").asDouble();
                                    Double placeLongitude = resultNode.get("geometry").get("location").get("lng").asDouble();

                                    String imagesRef = "";
                                    if (resultNode.has("photos")) {
                                        JsonNode photosNode = resultNode.get("photos");
                                        if (photosNode.isArray() && photosNode.size() > 0) {
                                            imagesRef = photosNode.get(0).get("photo_reference").asText();
                                        }
                                    }

                                    double rating = resultNode.has("rating") ? resultNode.get("rating").asDouble() : 0;
                                    int userRatings = resultNode.has("user_ratings_total") ? resultNode.get("user_ratings_total").asInt() : 0;

                                    ArrayList<String> typeOfPlace = new ArrayList<>();
                                    for (int i = 0; i < resultNode.get("types").size() ; i++) {
                                        typeOfPlace.add(resultNode.get("types").get(i).asText());
                                    }

                                    // Create a new Place object and set its properties
                                    Place place = new Place();
                                    place.setBusinessStatus(businessStatus);
                                    place.setName(name);
                                    place.setPlaceId(placeId);
                                    place.setLatitude(placeLatitude);
                                    place.setLongitude(placeLongitude);
                                    place.setImages(imagesRef);
                                    place.setRating(rating);
                                    place.setRating_amount(userRatings);
                                    place.setPrice(price_level);
                                    place.setPlaceTypes(typeOfPlace);

                                    // Add the place to the list
                                    places.add(place);
                                }
                            }
                            counter++;
                            if (root.has("next_page_token") && counter < 3) {
                                String nextPageToken = root.get("next_page_token").asText();
                                // Wait for the token to become valid
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=" +
                                        nextPageToken + "&key=" + SuperSecretApiKey.getApiKey();
                            } else {
                                break;
                            }
                        } else {
                            // Handle the API request error if needed
                            break;
                        }
                    } while (true);
                }
            }
        }
        return places;
    }
        private ResponseEntity<String> makeApiRequest (String url){
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}




