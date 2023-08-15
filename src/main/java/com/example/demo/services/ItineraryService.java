package com.example.demo.services;

import com.example.demo.SuperSecretApiKey;
import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class ItineraryService {

    private final RestTemplate restTemplate;

    @Autowired
    private Firestore firestore;  // Assuming you're using Firestore. Initialize this as needed.

    public ItineraryService(RestTemplate restTemplate, Firestore firestore) {
        this.restTemplate = restTemplate;
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
                if (!itinerary.getId().equals(itineraryId)) {// Ensure you don't delete the selected itinerary again
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

    public void deletePlaceFromItinerary(String userId, String itineraryId, String placeId) {
        DocumentReference itineraryRef = firestore.collection("users").document(userId).collection("itineraries")
                .document(itineraryId).collection("Destination List").document("Destination: " + placeId);
        // Assuming the 'Place' is stored as a subcollection or a map within the itinerary
        // Adjust this logic as per your Firestore schema
        itineraryRef.delete();
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

            ApiFuture<QuerySnapshot> future = destinationListRef.get();
            QuerySnapshot snapshot = future.get();
            itinerary.setItineraryId(itineraryDocRef.getId());

            ArrayList<Place> places = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Place place = new Place();

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

            ApiFuture<QuerySnapshot> future = destinationListRef.get();
            QuerySnapshot snapshot = future.get();
            itinerary.setItineraryId(itineraryDocRef.getId());

            ArrayList<Place> places = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Place place = new Place();

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

            place.setPlaceId(document.getString("placeId"));
            place.setName(document.getString("name"));
            place.setRating(document.getDouble("rating"));
            place.setImages(document.getString("images"));
            place.setPrice(cost);
            place.setRating_amount(ratingAmount);
            place.setLongitude(document.getDouble("longitude"));
            place.setLatitude(document.getDouble("latitude"));

            ArrayList<Double> originDestinationList = (ArrayList<Double>) document.get("originLocation");
            if (originDestinationList != null && !originDestinationList.isEmpty()) {
                place.setOriginLocation(originDestinationList);
            }

            places.add(place);
        }

        itinerary.setListOfDestinations(places);

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

            place.setPlaceId(document.getString("placeId"));
            place.setName(document.getString("name"));
            place.setRating(document.getDouble("rating"));
            place.setImages(document.getString("images"));
            place.setPrice(cost);
            place.setRating_amount(ratingAmount);
            place.setLongitude(document.getDouble("longitude"));
            place.setLatitude(document.getDouble("latitude"));

            ArrayList<Double> originDestinationList = (ArrayList<Double>) document.get("originLocation");
            if (originDestinationList != null && !originDestinationList.isEmpty()) {
                place.setOriginLocation(originDestinationList);
            }

            places.add(place);
        }

        itinerary.setListOfDestinations(places);

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
        System.out.println("DId it");
        return itineraries;
    }

    public void changeLike(String itineraryId, int value) {
        CollectionReference itineraryRef = firestore.collection("popular_itineraries").document("ListOfItineraries").collection("itineraries");
        DocumentReference docRef = itineraryRef.document(itineraryId);

        docRef.update("userLikes", FieldValue.increment(value));

        System.out.println(docRef.getId() + value);
    }



        private ResponseEntity<String> makeApiRequest (String url){
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        }
}

    // Add other CRUD operations here if necessary
