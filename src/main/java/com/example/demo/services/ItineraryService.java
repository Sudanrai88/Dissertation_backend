package com.example.demo.services;

import com.example.demo.model.Itinerary;
import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ItineraryService {

    @Autowired
    private Firestore firestore;  // Assuming you're using Firestore. Initialize this as needed.

    public void selectItinerary(String JWT, String itineraryId) throws FirebaseAuthException, ExecutionException, InterruptedException {

        // Assuming you can retrieve the user's UID from the JWT

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(JWT);
        String userId = decodedToken.getUid();

        // Fetch the selected itinerary
        DocumentReference selectedItinerary = firestore.collection("users").document(userId).collection("itineraries").document(itineraryId);
        DocumentSnapshot selectedSnapshot = selectedItinerary.get().get();

        if (selectedSnapshot.exists()) {
            // Move the selected itinerary to a new collection
            DocumentReference newItinerary = firestore.collection("users").document(userId).collection(itineraryId + "Itinerary").document(itineraryId);
            newItinerary.set(selectedSnapshot.getData());

            // Delete it from the original collection
            selectedItinerary.delete();

            // Delete all other itineraries for the user
            QuerySnapshot itineraries = firestore.collection("users").document(userId).collection("itineraries").get().get();
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

    public List<Itinerary> fetchItinerariesForUser(User user) {
        CollectionReference itineraryRef = firestore.collection("users").document(user.getUid()).collection("tempItineraries");
        List<Itinerary> tempItineraries = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = itineraryRef.get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                Itinerary itinerary = document.toObject(Itinerary.class);
                tempItineraries.add(itinerary);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // Handle the error accordingly, maybe log or throw a custom exception
        }

        return tempItineraries;

    }

    // Add other CRUD operations here if necessary
}
