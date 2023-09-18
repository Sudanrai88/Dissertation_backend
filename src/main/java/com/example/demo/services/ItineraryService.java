package com.example.demo.services;

import com.example.demo.SuperSecretApiKey;
import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ItineraryService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Autowired
    private Firestore firestore;  // Assuming you're using Firestore. Initialize this as needed.

    public ItineraryService(RestTemplate restTemplate, ObjectMapper objectMapper, Firestore firestore) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.firestore = firestore;
    }


    public void selectItinerary(String JWT, String itineraryId, String itineraryName) throws FirebaseAuthException, ExecutionException, InterruptedException {

        // Make sure after selection and moving the itinerary, ALL data is moved over in the correct format. Right now only the itinerary is moved over. Need to get
        // each destination!

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        // Fetch the selected itinerary
        DocumentReference selectedItinerary = firestore.collection("users").document(userId).collection("tempItineraries").document(itineraryId);
        DocumentReference newItinerary = firestore.collection("users").document(userId).collection("itineraries").document(itineraryName);

        DocumentSnapshot selectedSnapshot = selectedItinerary.get().get();

        if (selectedSnapshot.exists()) {

            // Copy the main document
            newItinerary.set(Objects.requireNonNull(selectedSnapshot.getData())).get();

            // Copy documents inside the 'Destination List' sub-collection
            CollectionReference sourceDestinationList = selectedItinerary.collection("Destination List");
            CollectionReference targetDestinationList = newItinerary.collection("Destination List");

            QuerySnapshot destinations = sourceDestinationList.get().get();
            for (DocumentSnapshot destination : destinations.getDocuments()) {
                targetDestinationList.document(destination.getId()).set(Objects.requireNonNull(destination.getData())).get();
            }

            // Delete the original itinerary (main document + its sub-collections)
            for (DocumentSnapshot destination : destinations.getDocuments()) {
                sourceDestinationList.document(destination.getId()).delete().get();
            }
            selectedItinerary.delete().get();

            // Delete all other itineraries for the user except the selected one
            QuerySnapshot itineraries = firestore.collection("users").document(userId).collection("tempItineraries").get().get();
            for (DocumentSnapshot itinerary : itineraries.getDocuments()) {
                if (!itinerary.getId().equals(itineraryId)) {// Selected itinerary not deleted again
                    if(!itinerary.getId().equals("placeHolder")) {
                        CollectionReference destList = itinerary.getReference().collection("Destination List");
                        QuerySnapshot unchosenDestinations = destList.get().get();
                        for (DocumentSnapshot destination : unchosenDestinations.getDocuments()) {
                            destination.getReference().delete().get();
                        }

                        itinerary.getReference().delete().get();
                    }
                }
            }
        }
    }

    public void updatePlaceInItinerary(String userId, String itineraryId, Place updatedPlace) {
        DocumentReference itineraryRef = firestore.collection("users").document(userId).collection(itineraryId + "Itinerary").document(itineraryId);
        // Update Itinerary by adding list of destinations generated from top 20 places
        // Possible List of destinations should be created

        itineraryRef.update("places." + updatedPlace.getPlaceId(), updatedPlace); // If Place is stored as a map
    }

    public void deletePlaceFromItinerary(String userId, String itineraryId, String placeId, int index) {
        DocumentReference itineraryRef = firestore.collection("users").document(userId).collection("itineraries")
                .document(itineraryId).collection("Destination List").document("Destination: " + placeId);
        // The 'Place' is stored as a subcollection within the itinerary

        itineraryRef.delete();

        CollectionReference DestinationRef = firestore.collection("users").document(userId).collection("itineraries")
                .document(itineraryId).collection("Destination List");

        ApiFuture<QuerySnapshot> futureDestinationsToUpdate = DestinationRef.whereGreaterThanOrEqualTo("order", index + 1).get();

        try {
            QuerySnapshot destinationsToUpdateSnapshot = futureDestinationsToUpdate.get();

            // Loop through each destination and increment the order
            for (QueryDocumentSnapshot destinationDocument : destinationsToUpdateSnapshot.getDocuments()) {
                int currentOrder = destinationDocument.getLong("order").intValue();
                DocumentReference destinationRef = destinationDocument.getReference();
                destinationRef.update("order", currentOrder - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteItinerary (String userId, String itineraryId) {
        DocumentReference itineraryRef = firestore.collection("users").document(userId).collection("itineraries")
                .document(itineraryId);

        CollectionReference itineraryRef2 = firestore.collection("users").document(userId).collection("itineraries")
                .document(itineraryId).collection("Destination List");

        Iterable<DocumentReference> listOfDocs = itineraryRef2.listDocuments();
        for (DocumentReference doc : listOfDocs) {
            doc.delete();
        }

        itineraryRef.delete();
        System.out.println("Deleted");

    }


    public List<Itinerary> fetchTempItinerariesForUser(User user) throws ExecutionException, InterruptedException {
        System.out.println("UID: " + user.getUid());

        List<Itinerary> itineraries = new ArrayList<>();

        CollectionReference itineraryRef = firestore.collection("users").document(user.getUid()).collection("tempItineraries");


        // Loop through the 5 itineraries
        for (int i = 1; i <= 5; i++) {
            Itinerary itinerary = new Itinerary();

            DocumentReference itineraryDocRef = itineraryRef.document("Itinerary: " + i);
            CollectionReference destinationListRef = itineraryDocRef.collection("Destination List");

            ApiFuture<QuerySnapshot> future = destinationListRef.get(); //DestinationList
            QuerySnapshot snapshot = future.get();

            ApiFuture<QuerySnapshot> future2 = itineraryRef.get(); //tempItin
            QueryDocumentSnapshot snapshot2 = future2.get().getDocuments().get(i -1);

            itinerary.setBestType(snapshot2.getString("bestType"));
            // Set specific bestType


            itinerary.setItineraryId(itineraryDocRef.getId());


            ArrayList<Place> places = new ArrayList<>();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Place place = new Place();
                Place testPlace = document.toObject(Place.class); //Apparently this does all of the below?

                int cost = ((Number) Objects.requireNonNull(document.get("price"))).intValue();
                int ratingAmount = ((Number) Objects.requireNonNull(document.get("rating_amount"))).intValue();

                place.setPlaceId(document.getString("placeId"));
                place.setName(document.getString("name"));
                place.setRating(document.getDouble("rating"));
                place.setImages(document.getString("images"));
                place.setPrice(cost);
                place.setRating_amount(ratingAmount);

                ArrayList<Double> originDestinationList = (ArrayList<Double>) document.get("originLocation");

                if (originDestinationList != null && !originDestinationList.isEmpty()) {
                    place.setOriginLocation(originDestinationList);
                }
                places.add(place);
            }

            itinerary.setListOfDestinations(places);
            itineraries.add(itinerary);
        }
        return itineraries;
    }


    public List<Itinerary> fetchItineraries(User user) throws ExecutionException, InterruptedException {
        System.out.println("UID: " + user.getUid());

        List<Itinerary> itineraries = new ArrayList<>();

        CollectionReference itineraryRef = firestore.collection("users").document(user.getUid()).collection("itineraries");
        Iterable<DocumentReference> Docs = itineraryRef.listDocuments();

        for (DocumentReference doc : Docs) {
            Itinerary itinerary = new Itinerary();
            String docName = doc.getId();
            DocumentReference itineraryDocRef = itineraryRef.document(docName);

            CollectionReference destinationListRef = itineraryDocRef.collection("Destination List");

            //ordered by the set order during Algorithms.java(saveItineraries).
            ApiFuture<QuerySnapshot> future = destinationListRef.orderBy("order").get();

            QuerySnapshot snapshot = future.get();
            itinerary.setItineraryId(itineraryDocRef.getId());

            ArrayList<Place> places = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Place place = new Place();

                int cost = ((Number) Objects.requireNonNull(document.get("price"))).intValue();
                int ratingAmount = ((Number) Objects.requireNonNull(document.get("rating_amount"))).intValue();
                int order = ((Number) Objects.requireNonNull(document.get("order"))).intValue();

                place.setPlaceId(document.getString("placeId"));
                place.setName(document.getString("name"));
                place.setRating(document.getDouble("rating"));
                place.setImages(document.getString("images"));
                place.setPrice(cost);
                place.setRating_amount(ratingAmount);
                place.setOrder(order);

                ArrayList<Double> originDestinationList = (ArrayList<Double>) document.get("originLocation");
                if (originDestinationList != null && !originDestinationList.isEmpty()) {
                    place.setOriginLocation(originDestinationList);
                }
                places.add(place);
            }

            //order the listOfDestinations depending on place order

            itinerary.setListOfDestinations(places);

            ArrayList<Place> destinations = itinerary.getListOfDestinations();
            destinations.sort(Comparator.comparingInt(Place::getOrder));

            itinerary.setListOfDestinations(destinations);


            itineraries.add(itinerary);
        }
        return itineraries;
    }


    public Itinerary fetchItineraryById(User userId, String itineraryId) throws ExecutionException, InterruptedException {

        // Reference to the specific itinerary document by its ID
        DocumentReference itineraryDocRef = firestore.collection("users").document(userId.getUid()).collection("itineraries").document(itineraryId);
        ApiFuture<DocumentSnapshot> docSnapshotFuture = itineraryDocRef.get();
        DocumentSnapshot itineraryDocSnapshot = docSnapshotFuture.get();

        // If the document doesn't exist, return null
        if (!itineraryDocSnapshot.exists()) {
            return null;
        }
        Itinerary itinerary = new Itinerary();
        itinerary.setItineraryId(itineraryDocSnapshot.getId());

        CollectionReference destinationListRef = itineraryDocRef.collection("Destination List");

        ApiFuture<QuerySnapshot> future = destinationListRef.get();
        QuerySnapshot snapshot = future.get();
        ArrayList<Place> places = new ArrayList<>();

        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            Place place = new Place();

            int cost = ((Number) Objects.requireNonNull(document.get("price"))).intValue();
            int ratingAmount = ((Number) Objects.requireNonNull(document.get("rating_amount"))).intValue();
            int order =  ((Number) Objects.requireNonNull(document.get("order"))).intValue();

            place.setPlaceId(document.getString("placeId"));
            place.setName(document.getString("name"));
            place.setRating(document.getDouble("rating"));
            place.setImages(document.getString("images"));
            place.setPrice(cost);
            place.setRating_amount(ratingAmount);
            place.setLongitude(document.getDouble("longitude"));
            place.setLatitude(document.getDouble("latitude"));
            place.setOrder(order);


            ArrayList<Double> originDestinationList = (ArrayList<Double>) document.get("originLocation");
            if (originDestinationList != null && !originDestinationList.isEmpty()) {
                place.setOriginLocation(originDestinationList);
            }
            places.add(place);
        }


        itinerary.setListOfDestinations(places);

        ArrayList<Place> destinations = itinerary.getListOfDestinations();
        destinations.sort(Comparator.comparingInt(Place::getOrder));

        itinerary.setListOfDestinations(destinations);


        return itinerary;
    }


    public Itinerary fetchPopularItinerary(String itineraryId) throws ExecutionException, InterruptedException {
        DocumentReference itineraryDocRef = firestore.collection("popular_itineraries").document("ListOfItineraries").collection("itineraries").document(itineraryId);
        ApiFuture<DocumentSnapshot> docSnapshotFuture = itineraryDocRef.get();
        DocumentSnapshot itineraryDocSnapshot = docSnapshotFuture.get();

        if (!itineraryDocSnapshot.exists()) {
            return null;
        }
        Itinerary itinerary = new Itinerary();
        itinerary.setItineraryId(itineraryDocSnapshot.getId());

        CollectionReference destinationListRef = itineraryDocRef.collection("Destination List");

        ApiFuture<QuerySnapshot> future = destinationListRef.get();
        QuerySnapshot snapshot = future.get();
        ArrayList<Place> places = new ArrayList<>();

        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            Place place = new Place();

            int cost = ((Number) Objects.requireNonNull(document.get("price"))).intValue();
            int ratingAmount = ((Number) Objects.requireNonNull(document.get("rating_amount"))).intValue();
            int order =  ((Number) Objects.requireNonNull(document.get("order"))).intValue();


            place.setPlaceId(document.getString("placeId"));
            place.setName(document.getString("name"));
            place.setRating(document.getDouble("rating"));
            place.setImages(document.getString("images"));
            place.setPrice(cost);
            place.setRating_amount(ratingAmount);
            place.setLongitude(document.getDouble("longitude"));
            place.setLatitude(document.getDouble("latitude"));
            place.setOrder(order);

            ArrayList<Double> originDestinationList = (ArrayList<Double>) document.get("originLocation");
            if (originDestinationList != null && !originDestinationList.isEmpty()) {
                place.setOriginLocation(originDestinationList);
            }

            places.add(place);
        }

        itinerary.setListOfDestinations(places);

        ArrayList<Place> destinations = itinerary.getListOfDestinations();
        destinations.sort(Comparator.comparingInt(Place::getOrder));

        itinerary.setListOfDestinations(destinations);

        return itinerary;


    }


    public void addItineraryToPopularItineraries (User user, String itineraryId) throws ExecutionException, InterruptedException {
        // Make sure after selection and moving the itinerary, ALL data is moved over in the correct format. Right now only the itinerary is moved over. Need to get
        // each destination!

        String userId = user.getUid();

        // Fetch the selected itinerary
        DocumentReference selectedItinerary = firestore.collection("users").document(userId).collection("itineraries").document(itineraryId);
        DocumentReference newItinerary = firestore.collection("popular_itineraries").document("ListOfItineraries").collection("itineraries").document(itineraryId);
        DocumentSnapshot selectedSnapshot = selectedItinerary.get().get();

        if (selectedSnapshot.exists()) {

            // Copy the main document
            newItinerary.set(Objects.requireNonNull(selectedSnapshot.getData())).get();

            // Copy documents inside the 'Destination List' sub-collection
            CollectionReference sourceDestinationList = selectedItinerary.collection("Destination List");
            CollectionReference targetDestinationList = newItinerary.collection("Destination List");

            QuerySnapshot destinations = sourceDestinationList.get().get();
            for (DocumentSnapshot destination : destinations.getDocuments()) {
                targetDestinationList.document(destination.getId()).set(Objects.requireNonNull(destination.getData())).get();
            }
            System.out.println("added");
        }

    }


    public List<Itinerary> fetchPopularItineraries() throws ExecutionException, InterruptedException {
        List<Itinerary> itineraries = new ArrayList<>();

        CollectionReference itineraryRef = firestore.collection("popular_itineraries").document("ListOfItineraries").collection("itineraries");
        Iterable<DocumentReference> Docs = itineraryRef.listDocuments();
        for (DocumentReference doc : Docs) {

            Itinerary itinerary = new Itinerary();
            String docName = doc.getId();

            DocumentReference itineraryDocRef = itineraryRef.document(docName);

            ApiFuture<QuerySnapshot> future = itineraryRef.document(docName).collection("Destination List").get();
            QuerySnapshot snapshot = future.get();

            ApiFuture<DocumentSnapshot> docSnapshotFuture = itineraryDocRef.get();
            DocumentSnapshot docSnapshot = docSnapshotFuture.get();
            int userLikesValue = Objects.requireNonNull(docSnapshot.getLong("userLikes")).intValue();

            itinerary.setItineraryId(itineraryDocRef.getId());
            itinerary.setUserLikes(userLikesValue);

            ArrayList<Place> places = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                //document = Sweden
                //collection -> Destination List
                //document -> Destination: "placeId"

                Place place = new Place();

                int cost = ((Number) Objects.requireNonNull(document.get("price"))).intValue();
                int ratingAmount = ((Number) Objects.requireNonNull(document.get("rating_amount"))).intValue();
                int order =  ((Number) Objects.requireNonNull(document.get("order"))).intValue();


                place.setPlaceId(document.getString("placeId"));
                place.setName(document.getString("name"));
                place.setRating(document.getDouble("rating"));
                place.setImages(document.getString("images"));
                place.setPrice(cost);
                place.setRating_amount(ratingAmount);
                place.setOrder(order);

                ArrayList<Double> originDestinationList = (ArrayList<Double>) document.get("originLocation");
                if (originDestinationList != null && !originDestinationList.isEmpty()) {
                    place.setOriginLocation(originDestinationList);
                }
                places.add(place);
            }

            itinerary.setListOfDestinations(places);

            ArrayList<Place> destinations = itinerary.getListOfDestinations();
            destinations.sort(Comparator.comparingInt(Place::getOrder));

            itinerary.setListOfDestinations(destinations);

            itineraries.add(itinerary);
        }
        System.out.println("DId it");
        return itineraries;
    }


    public void changeLike(String itineraryId, int value) {
        CollectionReference itineraryRef = firestore.collection("popular_itineraries").document("ListOfItineraries").collection("itineraries");
        DocumentReference docRef = itineraryRef.document(itineraryId);

        docRef.update("userLikes", FieldValue.increment(value));

        System.out.println(docRef.getId() + value);
    }


    public void addNewItinerary(String textLocation, User user, String itineraryId, int index) {

        String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?fields=formatted_address,name,rating,user_ratings_total,opening_hours,geometry,user_ratings_total,place_id,photos,price_level&input=" +
                textLocation + "&inputtype=textquery&key=" + SuperSecretApiKey.getApiKey();

        System.out.println(textLocation);

        ResponseEntity<String> response = makeApiRequest(url);
        String responseBody;
        Place place = new Place();

        responseBody = response.getBody();
        System.out.println(responseBody);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = null;
            try {
                root = objectMapper.readTree(responseBody);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println("ended here");
            }

            JsonNode resultNode = root.get("candidates");
            System.out.println(resultNode);

            if (resultNode.isArray() && resultNode.size() > 0) {
                // Access the first candidate, candidate is an [].
                JsonNode candidate = resultNode.get(0);

                int price_level = candidate.has("price_level") ? candidate.get("price_level").asInt() : 0;
                String name = candidate.get("name").asText();
                String placeId = candidate.get("place_id").asText();
                Double placeLatitude = candidate.get("geometry").get("location").get("lat").asDouble();
                Double placeLongitude = candidate.get("geometry").get("location").get("lng").asDouble();

                // Since there's no 'editorial_summary' in your example JSON, I'm leaving this as is.
                String editorial = candidate.has("editorial_summary") ? candidate.get("editorial_summary").asText() : "Empty";

                String imagesRef = "";
                if (candidate.has("photos")) {
                    JsonNode photosNode = candidate.get("photos");
                    if (photosNode.isArray() && photosNode.size() > 0) {
                        imagesRef = photosNode.get(0).get("photo_reference").asText();
                    }
                }

                double rating = candidate.has("rating") ? candidate.get("rating").asDouble() : 0;
                int userRatings = candidate.has("user_ratings_total") ? candidate.get("user_ratings_total").asInt() : 0;

                List<Double> origin = new ArrayList<>();
                origin.add(placeLatitude);
                origin.add(placeLongitude);

                place.setName(name);
                place.setPlaceId(placeId);
                place.setLatitude(placeLatitude);
                place.setLongitude(placeLongitude);
                place.setImages(imagesRef);
                place.setRating(rating);
                place.setRating_amount(userRatings);
                place.setPrice(price_level);
                place.setEditorialSummary(editorial);
                place.setOriginLocation(origin);
                place.setOrder(index + 1);
            }
        }

        CollectionReference itineraryRef = firestore.collection("users").document(user.getUid()).collection("itineraries").document(itineraryId).collection("Destination List");
        String placesId = place.getPlaceId();

        //fetch all destinations with order value aboev index + 1
        ApiFuture<QuerySnapshot> futureDestinationsToUpdate = itineraryRef.whereGreaterThanOrEqualTo("order", index + 1).get();

        try {
            QuerySnapshot destinationsToUpdateSnapshot = futureDestinationsToUpdate.get();

            // Loop through each destination and increment the order
            for (QueryDocumentSnapshot destinationDocument : destinationsToUpdateSnapshot.getDocuments()) {
                int currentOrder = destinationDocument.getLong("order").intValue();
                DocumentReference destinationRef = destinationDocument.getReference();
                destinationRef.update("order", currentOrder + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ApiFuture<WriteResult> collectionApiFuture = itineraryRef
                .document("Destination: " + placesId)
                .set(place);
    }

    public void swapIndexOrder(String itineraryId, User user, int toGoDestination, int source) {
        CollectionReference itineraryRef = firestore.collection("users").document(user.getUid()).collection("itineraries").document(itineraryId).collection("Destination List");
        try {
            ApiFuture<QuerySnapshot> sourceToDestination = itineraryRef.whereEqualTo("order", source).get();
            ApiFuture<QuerySnapshot> destinationToSource = itineraryRef.whereEqualTo("order", toGoDestination).get();

            QueryDocumentSnapshot sourceDocument = sourceToDestination.get().getDocuments().get(0);
            QueryDocumentSnapshot destinationDocument = destinationToSource.get().getDocuments().get(0);

            DocumentReference sourceRef = sourceDocument.getReference();
            DocumentReference destinationRef = destinationDocument.getReference();

            // Swap the order values
            sourceRef.update("order", toGoDestination);
            destinationRef.update("order", source);

            System.out.println(toGoDestination);
            System.out.println(source);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

        private ResponseEntity<String> makeApiRequest (String url){
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        }

}

    // Add other CRUD operations here if necessary
