package com.example.demo.services;

import com.example.demo.model.Place;
import com.example.demo.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class Algorithms {

    public List<Place> scoreDestinations(List<Place> places, int costScore, int accessibilityScore, int popularityScore) {

        // Implement scoring logic here to assign a fitness score to each place based on objective functions

        // Modify the Place objects in the 'places' list with the computed scores

        // Example scoring algorithm:

        return places;
    }

    public List<Place> geneticAlgorithm(List<Place> places) {
        //proceed with scoredDestination places
        //perform genetic algorithm steps

        //for every generation run scoreDestination ()
        //after generation finishes then make 50% children
        //delete 3 (cross-over approach)
        //keeps fittest population
        //

        return places;
    }



    public List<Place> top5(List<Place> places) {
        List<Place> newPlaces = places.subList(0,5);
        return newPlaces;
    }

    private static final String COLLECTION_NAME = "itineraries";

    public String saveItineraries(User user, Place place, String itineraryName, String destinationName) throws ExecutionException, InterruptedException {

        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(user.getUid())
                .collection(itineraryName)
                .document("Destination: " + destinationName)
                .set(place);

        return collectionApiFuture.get().getUpdateTime().toString();
    }

    //Find popularity score
    public double objectiveFunction1() {
        return 0;
    }

}

