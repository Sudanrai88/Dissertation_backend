package com.example.demo.services;

import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.fife.ui.rtextarea.RDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class ItineraryService {

    @Autowired
    private Firestore firestore;  // Assuming you're using Firestore. Initialize this as needed.

    public ItineraryService(Firestore firestore) {
        this.firestore = firestore;
    }


    public void selectItinerary(String JWT, String itineraryId, String itineraryName) throws FirebaseAuthException, ExecutionException, InterruptedException {

        // Make sure after selection and moving the itinerary, ALL data is moved over in the correct format. Right now only the itinerary is moved over. Need to get
        // each destination!
        System.out.println("Active");

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        // Fetch the selected itinerary
        DocumentReference selectedItinerary = firestore.collection("users").document(userId).collection("tempItineraries").document(itineraryId);
        DocumentSnapshot selectedSnapshot = selectedItinerary.get().get();

        if (selectedSnapshot.exists()) {
            // Move the selected itinerary to a new collection
            DocumentReference newItinerary = firestore.collection("users").document(userId).collection(itineraryName).document(itineraryId);
            newItinerary.set(Objects.requireNonNull(selectedSnapshot.getData()));

            // Delete it from the original collection
            selectedItinerary.delete();

            // Delete all other itineraries for the user
            QuerySnapshot itineraries = firestore.collection("users").document(userId).collection("tempItineraries").get().get();
            for (DocumentSnapshot itinerary : itineraries.getDocuments()) {
                itinerary.getReference().delete();
            }
        }
    }

    public void updatePlaceInItinerary(String userId, String itineraryId, Place updatedPlace) {
        DocumentReference itineraryRef = firestore.collection("users").document(userId).collection(itineraryId + "Itinerary").document(itineraryId);
        // Assuming the 'Place' is stored as a subcollection or a map within the itinerary
        // Adjust this logic as per your Firestore schema
        itineraryRef.update("places." + updatedPlace.getPlaceId(), updatedPlace); // If Place is stored as a map
    }

    public void deletePlaceFromItinerary(String userId, String itineraryId, String placeId) {
        DocumentReference itineraryRef = firestore.collection("users").document(userId).collection(itineraryId + "Itinerary").document(itineraryId);
        // Assuming the 'Place' is stored as a subcollection or a map within the itinerary
        // Adjust this logic as per your Firestore schema
        itineraryRef.update("places." + placeId, FieldValue.delete()); // If Place is stored as a map
    }

    public List<Itinerary> fetchItinerariesForUser(User user) throws ExecutionException, InterruptedException {
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
                // Assuming your Place class has methods to set properties like name, description, etc.
                // place.setName(document.getString("name"));
                // Add any other required mappings

                place.setPlaceId(document.getString("placeId"));
                place.setName(document.getString("name"));
                place.setRating(document.getDouble("rating"));


                place.setImages(document.getString("images"));

                places.add(place);
            }

            itinerary.setListOfDestinations(places);
            itineraries.add(itinerary);
        }

        return itineraries;
    }
}

    // Add other CRUD operations here if necessary
